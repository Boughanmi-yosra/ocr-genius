package com.yobo.orcg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.SpringVersion;

@SpringBootApplication
public class DocumentIdentifierApplication {
    private static final Logger logger = LoggerFactory.getLogger(DocumentIdentifierApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(DocumentIdentifierApplication.class, args);
        logger.info("Spring core version : {} Spring boot version : {} ", SpringVersion.getVersion(), SpringBootVersion.getVersion());
    }
}
