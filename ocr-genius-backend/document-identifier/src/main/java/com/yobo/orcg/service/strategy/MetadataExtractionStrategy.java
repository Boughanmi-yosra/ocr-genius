package com.yobo.orcg.service.strategy;

import com.yobo.orcg.model.DocumentInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface MetadataExtractionStrategy {
    DocumentInfo extractMetadata(MultipartFile file);
}
