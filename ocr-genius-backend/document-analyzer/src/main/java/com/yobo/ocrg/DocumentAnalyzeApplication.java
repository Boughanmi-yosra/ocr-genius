package com.yobo.ocrg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.SpringVersion;

@SpringBootApplication
public class DocumentAnalyzeApplication {
    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalyzeApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(DocumentAnalyzeApplication.class, args);
        logger.info("Spring core version : {} Spring boot version : {} ", SpringVersion.getVersion(), SpringBootVersion.getVersion());
    }
}
