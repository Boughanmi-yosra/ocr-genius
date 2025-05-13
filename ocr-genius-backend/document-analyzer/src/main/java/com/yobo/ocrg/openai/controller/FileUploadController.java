package com.yobo.ocrg.openai.controller;


import com.yobo.ocrg.openai.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai-analyze")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> analyzeFile(@RequestParam("file") MultipartFile file) {
        try {
            String result = fileUploadService.uploadAndAnalyze(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error analyzing file: " + e.getMessage());
        }
    }
}
