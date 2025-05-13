package com.yobo.orcg.service;

import com.yobo.orcg.model.DocumentInfo;
import com.yobo.orcg.service.strategy.MetadataExtractionStrategy;
import com.yobo.orcg.service.strategy.MetadataExtractionStrategyFactory;
import com.yobo.orcg.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class DocumentAnalysisService {

    public DocumentInfo analyze(MultipartFile file) {
        try {
            // Convert MultipartFile to a regular File for metadata extraction
            File localFile = FileUtils.convertToFile(file);
            String extension = FileUtils.getExtension(file.getOriginalFilename());

            // Get the appropriate strategy for extracting metadata
            MetadataExtractionStrategy strategy = MetadataExtractionStrategyFactory.getStrategy(extension);

            // Use the strategy to extract metadata
            return strategy.extractMetadata(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze file", e);
        }
    }
}
