package com.yobo.ocrg.ollama.controller;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
@RequestMapping("/api/ollama-analyze")
public class DocumentController {

        private static final String FASTAPI_URL = "http://127.0.0.1:8889/analyze"; // FastAPI endpoint

        @PostMapping("/upload")
        public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
            // Prepare RestTemplate for sending HTTP request to FastAPI
            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));

            // Create HttpHeaders with content-type for file upload
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "multipart/form-data");

            // Create a MultiValueMap to store the file data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new InputStreamResource(file.getInputStream()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            // Create HttpEntity with headers and body to send the request
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send the file to FastAPI using RestTemplate
            ResponseEntity<String> response = restTemplate.exchange(FASTAPI_URL, HttpMethod.POST, requestEntity, String.class);

            // Return the response received from FastAPI back to Postman
            return response;
        }
}
