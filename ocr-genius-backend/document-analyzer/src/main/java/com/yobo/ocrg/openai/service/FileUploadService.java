package com.yobo.ocrg.openai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class FileUploadService {

    @Value("${openai.api.key}")
    private String openaiApiKey ;

    /*public String uploadAndAnalyze(MultipartFile file) throws Exception {
        File tempFile = convertToFile(file);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost uploadRequest = new HttpPost("https://api.openai.com/v1/files");
            uploadRequest.setHeader("Authorization", "Bearer " + openaiApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);
            builder.setMode(HttpMultipartMode.STRICT);

            builder.addPart("file", new FileBody(tempFile));
            builder.addTextBody("purpose", "assistants");

            uploadRequest.setEntity(builder.build());

            ClassicHttpResponse response = httpClient.execute(uploadRequest);
            String json = EntityUtils.toString(response.getEntity());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root.toPrettyString(); // this contains file ID, etc.
        } finally {
            tempFile.delete(); // clean up
        }
    }*/

    // Upload the file to OpenAI and ask what the document is about
    public String uploadAndAnalyze(MultipartFile file) throws Exception {
        File tempFile = convertToFile(file);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // 1. Upload file
            HttpPost uploadRequest = new HttpPost("https://api.openai.com/v1/files");
            uploadRequest.setHeader("Authorization", "Bearer " + openaiApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);
            builder.setMode(HttpMultipartMode.STRICT);
            builder.addPart("file", new FileBody(tempFile));
            builder.addTextBody("purpose", "assistants"); // file is now usable by assistant

            uploadRequest.setEntity(builder.build());
            ClassicHttpResponse response = httpClient.execute(uploadRequest);

            String uploadJson = EntityUtils.toString(response.getEntity());
            JsonNode uploadNode = new ObjectMapper().readTree(uploadJson);
            System.out.println("uploadNode: " + uploadNode);
            String fileId = uploadNode.get("id").asText();
            System.out.println("✅ File uploaded to OpenAI: " + fileId);

            // 2. Ask GPT to analyze the file content
            return askOpenAiWhatIsThisDocument(fileId, httpClient);

        } finally {
            tempFile.delete(); // Clean up the temporary file
        }
    }

    /*private String askOpenAiWhatIsThisDocument(String fileId, CloseableHttpClient httpClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create the Assistant for analyzing documents
        HttpPost createAssistantRequest = new HttpPost("https://api.openai.com/v1/assistants");
        createAssistantRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
        createAssistantRequest.setHeader("Content-Type", "application/json");
        createAssistantRequest.setHeader("OpenAI-Beta", "assistants=v2");

        String assistantBody = "{" +
                "\"model\": \"gpt-4.1\"," +
                "\"name\": \"Document Analyzer\"," +
                "\"instructions\": \"You analyze documents and explain their content\"," +
                "\"tools\": [{\"type\": \"file_search\"}]" +
                "}";
        createAssistantRequest.setEntity(new StringEntity(assistantBody));
        ClassicHttpResponse assistantResponse = httpClient.execute(createAssistantRequest);
        String assistantJson = EntityUtils.toString(assistantResponse.getEntity());
        System.out.println("assistantJson : "+assistantJson);
        String assistantId = mapper.readTree(assistantJson).get("id").asText();

        // Step 4: Create a thread to analyze the file
        HttpPost createThreadRequest = new HttpPost("https://api.openai.com/v1/threads");
        createThreadRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
        createThreadRequest.setHeader("Content-Type", "application/json");
        createThreadRequest.setHeader("OpenAI-Beta", "assistants=v2");

        String threadBody = "{" +
                "\"messages\": [" +
                "   {" +
                "       \"role\": \"user\"," +
                "       \"content\": \"Based on the uploaded file, what is this document about? no explanation needed\"" +
                "   }" +
                "]" +
                "}";

        createThreadRequest.setEntity(new StringEntity(threadBody));
        ClassicHttpResponse threadResponse = httpClient.execute(createThreadRequest);
        String threadJson = EntityUtils.toString(threadResponse.getEntity());
        System.out.println("threadJson : "+threadJson);
        String threadId = mapper.readTree(threadJson).get("id").asText();


        HttpPost createVectorStoreRequest = new HttpPost("https://api.openai.com/v1/vector_stores");
        createVectorStoreRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
        createVectorStoreRequest.setHeader("Content-Type", "application/json");
        createVectorStoreRequest.setHeader("OpenAI-Beta", "assistants=v2");

        String vectorStoreBody = "{\"name\": \"My Document Vector Store\"}";
        createVectorStoreRequest.setEntity(new StringEntity(vectorStoreBody));

        ClassicHttpResponse vectorStoreResponse = httpClient.execute(createVectorStoreRequest);
        String vectorStoreJson = EntityUtils.toString(vectorStoreResponse.getEntity());
        System.out.println("vectorStoreJson : " + vectorStoreJson);

        String vectorStoreId = mapper.readTree(vectorStoreJson).get("id").asText();

        // Step 5: Run the assistant on the thread
        HttpPost runRequest = new HttpPost("https://api.openai.com/v1/threads/" + threadId + "/runs");
        runRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
        runRequest.setHeader("Content-Type", "application/json");
        runRequest.setHeader("OpenAI-Beta", "assistants=v2");

        String runBody = "{" +
                "\"assistant_id\": \"" + assistantId + "\"," +
                "\"tool_resources\": {" +
                "  \"file_search\": {" +
                "    \"vector_store_ids\": [\"" + vectorStoreId + "\"]" +
                "  }" +
                "}" +
                "}";
        runRequest.setEntity(new StringEntity(runBody));
        ClassicHttpResponse runResponse = httpClient.execute(runRequest);
        String runJson = EntityUtils.toString(runResponse.getEntity());
        System.out.println("runJson : "+runJson);
        String runId = mapper.readTree(runJson).get("id").asText();

        // Step 6: Poll the run until it's completed
        HttpGet getRunRequest = new HttpGet("https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId);
        getRunRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
        getRunRequest.setHeader("OpenAI-Beta", "assistants=v2");

        while (true) {
            Thread.sleep(1500); // Delay for a brief moment
            ClassicHttpResponse statusResponse = httpClient.execute(getRunRequest);
            String statusJson = EntityUtils.toString(statusResponse.getEntity());
            JsonNode statusNode = mapper.readTree(statusJson);
            System.out.println("statusNode : "+statusNode);
            if ("completed".equals(statusNode.get("status").asText())) {
                break;
            }
        }

        // Step 7: Retrieve the analysis result
        HttpGet messagesRequest = new HttpGet("https://api.openai.com/v1/threads/" + threadId + "/messages");
        messagesRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
        messagesRequest.setHeader("OpenAI-Beta", "assistants=v2");

        ClassicHttpResponse messagesResponse = httpClient.execute(messagesRequest);
        String messagesJson = EntityUtils.toString(messagesResponse.getEntity());
        System.out.println("messagesJson : "+messagesJson);
        JsonNode messagesNode = mapper.readTree(messagesJson);

        JsonNode messages = messagesNode.get("data");
        for (JsonNode message : messages) {
            if ("assistant".equals(message.get("role").asText())) {
                JsonNode contentArray = message.get("content");
                if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                    JsonNode textBlock = contentArray.get(0).get("text");
                    if (textBlock != null && textBlock.has("value")) {
                        return textBlock.get("value").asText();
                    }
                }
            }
        }
        return "No assistant response found.";
    }*/

    /*private String askOpenAiWhatIsThisDocument(String fileId, CloseableHttpClient httpClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 1. Create Vector Store
            HttpPost createVectorStoreRequest = new HttpPost("https://api.openai.com/v1/vector_stores");
            createVectorStoreRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            createVectorStoreRequest.setHeader("Content-Type", "application/json");
            createVectorStoreRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String vectorStoreBody = "{\"name\": \"Document Analysis Vector Store V1\"}";
            createVectorStoreRequest.setEntity(new StringEntity(vectorStoreBody));
            String vectorStoreJson = EntityUtils.toString(httpClient.execute(createVectorStoreRequest).getEntity());
            System.out.println("vectorStoreJson : " + vectorStoreJson);
            String vectorStoreId = mapper.readTree(vectorStoreJson).get("id").asText();
            System.out.println("Vector Store created: " + vectorStoreId);

            // 2. Attach file to vector store
            HttpPost attachFileRequest = new HttpPost("https://api.openai.com/v1/vector_stores/" + vectorStoreId + "/files");
            attachFileRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            attachFileRequest.setHeader("Content-Type", "application/json");
            attachFileRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String attachFileBody = "{ \"file_id\": \"" + fileId + "\" }";
            attachFileRequest.setEntity(new StringEntity(attachFileBody));
            String attachJson = EntityUtils.toString(httpClient.execute(attachFileRequest).getEntity());
            System.out.println("attachJson : " + attachJson);
            String fileBatchId = mapper.readTree(attachJson).get("id").asText();
            System.out.println("File attached: " + fileBatchId);

            // 2b. Wait until file is processed
            boolean fileProcessed = false;
            while (!fileProcessed) {
                Thread.sleep(2000);
                HttpGet fileStatusRequest = new HttpGet("https://api.openai.com/v1/vector_stores/" + vectorStoreId + "/files/" + fileBatchId);
                fileStatusRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
                fileStatusRequest.setHeader("OpenAI-Beta", "assistants=v2");

                String fileStatusJson = EntityUtils.toString(httpClient.execute(fileStatusRequest).getEntity());
                System.out.println("fileStatusJson : " + fileStatusJson);
                String status = mapper.readTree(fileStatusJson).get("status").asText();
                System.out.println("File status: " + status);
                fileProcessed = "completed".equals(status);
            }

            // 3. Create Assistant with file search tool
            HttpPost createAssistantRequest = new HttpPost("https://api.openai.com/v1/assistants");
            createAssistantRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            createAssistantRequest.setHeader("Content-Type", "application/json");
            createAssistantRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String assistantBody = "{" +
                    "\"model\": \"gpt-4.1\"," +
                    "\"name\": \"Document Analyzer Assistant V1\"," +
                    "\"instructions\": \"You are a document classification expert. When asked to analyze a document, examine its contents carefully and respond only with the most specific document type . Do not provide any explanations or additional text.\"," +
                    "\"tools\": [{\"type\": \"file_search\"}]," +  // Removed code_interpreter if not needed
                    "\"tool_resources\": {\"file_search\": {\"vector_store_ids\": [\"" + vectorStoreId + "\"]}}" +
                    "}";

            createAssistantRequest.setEntity(new StringEntity(assistantBody));
            String assistantJson = EntityUtils.toString(httpClient.execute(createAssistantRequest).getEntity());
            System.out.println("assistantJson : " + assistantJson);
            String assistantId = mapper.readTree(assistantJson).get("id").asText();
            System.out.println("Assistant created: " + assistantId);

            // 4. Create a new thread with user prompt (file is indirectly available via vector store)
            HttpPost createThreadRequest = new HttpPost("https://api.openai.com/v1/threads");
            createThreadRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            createThreadRequest.setHeader("Content-Type", "application/json");
            createThreadRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String threadBody = "{" +
                    "\"messages\": [" +
                    "   {" +
                    "       \"role\": \"user\"," +
                    "       \"content\": \"Analyze the attached document and tell me what type of document it is. Just respond with the document type.\"" +
                    "   }" +
                    "]," +
                    "\"tool_resources\": {" +
                    "   \"file_search\": {" +
                    "       \"vector_store_ids\": [\"" + vectorStoreId + "\"]" +
                    "   }" +
                    "}" +
                    "}";

            createThreadRequest.setEntity(new StringEntity(threadBody));
            String threadJson = EntityUtils.toString(httpClient.execute(createThreadRequest).getEntity());
            System.out.println("threadJson : " + threadJson);
            String threadId = mapper.readTree(threadJson).get("id").asText();


            // 5. Run the assistant on the thread
            HttpPost runRequest = new HttpPost("https://api.openai.com/v1/threads/" + threadId + "/runs");
            runRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            runRequest.setHeader("Content-Type", "application/json");
            runRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String runBody = "{\"assistant_id\": \"" + assistantId + "\"}";
            runRequest.setEntity(new StringEntity(runBody));
            String runJson = EntityUtils.toString(httpClient.execute(runRequest).getEntity());
            System.out.println("runJson : " + runJson);
            String runId = mapper.readTree(runJson).get("id").asText();
            System.out.println("Run started: " + runId);

            // 6. Poll until run is complete
            int attempts = 0;
            final int MAX_ATTEMPTS = 10;
            String runStatus;
            do {
                Thread.sleep(2000);
                HttpGet getRunRequest = new HttpGet("https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId);
                getRunRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
                getRunRequest.setHeader("OpenAI-Beta", "assistants=v2");

                String statusJson = EntityUtils.toString(httpClient.execute(getRunRequest).getEntity());
                System.out.println("statusJson : " + statusJson);
                runStatus = mapper.readTree(statusJson).get("status").asText();
                System.out.println("Run status: " + runStatus);
                attempts++;
            } while (!"completed".equals(runStatus) && attempts < MAX_ATTEMPTS);

            if (!"completed".equals(runStatus)) {
                throw new Exception("Run failed or timed out with status: " + runStatus);
            }

            // 7. Retrieve assistant's response
            HttpGet messagesRequest = new HttpGet("https://api.openai.com/v1/threads/" + threadId + "/messages");
            messagesRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            messagesRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String messagesJson = EntityUtils.toString(httpClient.execute(messagesRequest).getEntity());
            System.out.println("messagesJson : "+messagesJson);
            JsonNode messagesNode = mapper.readTree(messagesJson).get("data");

            for (JsonNode message : messagesNode) {
                if ("assistant".equals(message.get("role").asText())) {
                    JsonNode contentArray = message.get("content");
                    if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                        JsonNode textBlock = contentArray.get(0).get("text");
                        if (textBlock != null && textBlock.has("value")) {
                            return textBlock.get("value").asText();
                        }
                    }
                }
            }

            return "No assistant response found.";
        } finally {
            // Optional: Cleanup logic to delete temporary resources if needed
        }
    }*/


    private String askOpenAiWhatIsThisDocument(String fileId, CloseableHttpClient httpClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 1. Create Vector Store
            HttpPost createVectorStoreRequest = new HttpPost("https://api.openai.com/v1/vector_stores");
            createVectorStoreRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            createVectorStoreRequest.setHeader("Content-Type", "application/json");
            createVectorStoreRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String vectorStoreBody = "{\"name\": \"Document Analysis Vector Store\"}";
            createVectorStoreRequest.setEntity(new StringEntity(vectorStoreBody));
            String vectorStoreJson = EntityUtils.toString(httpClient.execute(createVectorStoreRequest).getEntity());
            System.out.println("vectorStoreJson : "+vectorStoreJson);
            String vectorStoreId = mapper.readTree(vectorStoreJson).get("id").asText();

            // 2. Attach file to vector store
            HttpPost attachFileRequest = new HttpPost("https://api.openai.com/v1/vector_stores/" + vectorStoreId + "/files");
            attachFileRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            attachFileRequest.setHeader("Content-Type", "application/json");
            attachFileRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String attachFileBody = "{ \"file_id\": \"" + fileId + "\" }";
            attachFileRequest.setEntity(new StringEntity(attachFileBody));
            String attachJson = EntityUtils.toString(httpClient.execute(attachFileRequest).getEntity());
            System.out.println("attachJson : "+attachJson);
            String fileBatchId = mapper.readTree(attachJson).get("id").asText();

            // Wait for file processing
            boolean fileProcessed = false;
            while (!fileProcessed) {
                Thread.sleep(2000);
                HttpGet fileStatusRequest = new HttpGet("https://api.openai.com/v1/vector_stores/" + vectorStoreId + "/files/" + fileBatchId);
                fileStatusRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
                fileStatusRequest.setHeader("OpenAI-Beta", "assistants=v2");

                String fileStatusJson = EntityUtils.toString(httpClient.execute(fileStatusRequest).getEntity());
                System.out.println("fileStatusJson : "+fileStatusJson);
                String status = mapper.readTree(fileStatusJson).get("status").asText();
                fileProcessed = "completed".equals(status);
            }

            // 3. Create Assistant with JUST file_search tool
            HttpPost createAssistantRequest = new HttpPost("https://api.openai.com/v1/assistants");
            createAssistantRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            createAssistantRequest.setHeader("Content-Type", "application/json");
            createAssistantRequest.setHeader("OpenAI-Beta", "assistants=v2");

            /*String assistantBody = "{" +
                    "\"model\": \"gpt-4.1\"," +
                    "\"name\": \"Document Analyzer\"," +
                    "\"instructions\": \"Analyze the provided document and respond ONLY with the document type. Examples: 'invoice', 'contract', 'resume'. No explanations.\"," +
                    "\"tools\": [{\"type\": \"file_search\"}]," +
                    "\"tool_resources\": {\"file_search\": {\"vector_store_ids\": [\"" + vectorStoreId + "\"]}}" +
                    "}";*/

            String assistantBody = "{" +
                    "\"model\": \"gpt-4o\"," +
                    "\"name\": \"Document Analyzer\"," +
                    "\"instructions\": \"Analyze the provided document and respond ONLY with the document type.\"," +
                    "\"tools\": [{\"type\": \"file_search\"}]," +
                    "\"tool_resources\": {\"file_search\": {\"vector_store_ids\": [\"" + vectorStoreId + "\"]}}" +
                    "}";
            createAssistantRequest.setEntity(new StringEntity(assistantBody));
            String assistantJson = EntityUtils.toString(httpClient.execute(createAssistantRequest).getEntity());
            System.out.println("assistantJson : "+assistantJson);
            String assistantId = mapper.readTree(assistantJson).get("id").asText();

            // 4. Create thread with EXPLICIT instruction to analyze THE document
            HttpPost createThreadRequest = new HttpPost("https://api.openai.com/v1/threads");
            createThreadRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            createThreadRequest.setHeader("Content-Type", "application/json");
            createThreadRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String threadBody = "{" +
                    "\"messages\": [" +
                    "   {" +
                    "       \"role\": \"user\"," +
                    "       \"content\": \"Analyze the attached document and tell me EXACTLY what type of document it is. Respond with ONLY the document type.\"," +
                    "       \"attachments\": [" +
                    "           {" +
                    "               \"file_id\": \"" + fileId + "\"," +
                    "               \"tools\": [{\"type\": \"file_search\"}]" +
                    "           }" +
                    "       ]" +
                    "   }" +
                    "]" +
                    "}";

            createThreadRequest.setEntity(new StringEntity(threadBody));
            String threadJson = EntityUtils.toString(httpClient.execute(createThreadRequest).getEntity());
            System.out.println("threadJson : "+threadJson);
            String threadId = mapper.readTree(threadJson).get("id").asText();

            // 5. Run the assistant
            HttpPost runRequest = new HttpPost("https://api.openai.com/v1/threads/" + threadId + "/runs");
            runRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            runRequest.setHeader("Content-Type", "application/json");
            runRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String runBody = "{" +
                    "\"assistant_id\": \"" + assistantId + "\"," +
                    "\"instructions\": \"Analyze the document and respond with JUST the document type\"" +
                    "}";
            runRequest.setEntity(new StringEntity(runBody));
            String runJson = EntityUtils.toString(httpClient.execute(runRequest).getEntity());
            System.out.println("runJson : "+runJson);
            String runId = mapper.readTree(runJson).get("id").asText();

            // 6. Poll for completion
            int attempts = 0;
            final int MAX_ATTEMPTS = 10;
            String runStatus;
            do {
                Thread.sleep(2000);
                HttpGet getRunRequest = new HttpGet("https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId);
                getRunRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
                getRunRequest.setHeader("OpenAI-Beta", "assistants=v2");

                String statusJson = EntityUtils.toString(httpClient.execute(getRunRequest).getEntity());
                System.out.println("statusJson : "+statusJson);
                runStatus = mapper.readTree(statusJson).get("status").asText();
                attempts++;
            } while (!"completed".equals(runStatus) && attempts < MAX_ATTEMPTS);

            if (!"completed".equals(runStatus)) {
                throw new Exception("Run failed or timed out with status: " + runStatus);
            }

            // 7. Get response
            HttpGet messagesRequest = new HttpGet("https://api.openai.com/v1/threads/" + threadId + "/messages");
            messagesRequest.setHeader("Authorization", "Bearer " + openaiApiKey);
            messagesRequest.setHeader("OpenAI-Beta", "assistants=v2");

            String messagesJson = EntityUtils.toString(httpClient.execute(messagesRequest).getEntity());
            System.out.println("messagesJson : "+messagesJson);
            JsonNode messagesNode = mapper.readTree(messagesJson).get("data");

            for (JsonNode message : messagesNode) {
                if ("assistant".equals(message.get("role").asText())) {
                    JsonNode contentArray = message.get("content");
                    if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                        JsonNode textBlock = contentArray.get(0).get("text");
                        if (textBlock != null && textBlock.has("value")) {
                            return textBlock.get("value").asText();
                        }
                    }
                }
            }

            return "No response found";
        } finally {
            // Cleanup code if needed
        }
    }


    private File convertToFile(MultipartFile multipartFile) throws Exception {
        File file = File.createTempFile("upload", multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }
}
