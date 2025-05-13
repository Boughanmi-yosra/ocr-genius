package com.yobo.orcg.controller;

import com.yobo.orcg.model.DocumentInfo;
import com.yobo.orcg.service.DocumentAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("api/documents")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentAnalysisService documentService;

    public DocumentController(DocumentAnalysisService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentInfo> upload(@RequestParam("file") MultipartFile file) {
        logger.info("INFDOCCTRL001 Received file upload request: name='{}', size={} bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            DocumentInfo result = documentService.analyze(file);

            logger.info("INFDOCCTRL002 File analysis completed: extension='{}', contentType='{}', creationDate='{}'",
                    result.getExtension(), result.getContentType(), result.getCreationDate());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERRDOCCTRL001 File analysis failed for '{}'", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




}