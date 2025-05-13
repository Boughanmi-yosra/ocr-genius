package com.yobo.ocrg.ollama.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
public class DocumentAnalyzerClient {

    private static final String FASTAPI_URL = "http://127.0.0.1:8889";  // Your FastAPI URL

    public static void main(String[] args) {
        // Call the FastAPI endpoint
        String document = "path/to/your/document.pdf";  // replace with your document path
        JSONObject jsonResponse = sendRequestToFastAPI(document);
        System.out.println("Response from FastAPI: " + jsonResponse);
    }

    public static JSONObject sendRequestToFastAPI(String documentPath) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Create a POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FASTAPI_URL + "/analyze"))  // replace with your FastAPI endpoint
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"document\":\"" + documentPath + "\"}"))
                    .build();

            // Send the request and receive the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response into a JSONObject
            JSONObject jsonResponse = new JSONObject(response.body());

            return jsonResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

