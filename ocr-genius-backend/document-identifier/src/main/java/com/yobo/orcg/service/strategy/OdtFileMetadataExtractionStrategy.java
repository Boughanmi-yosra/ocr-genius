package com.yobo.orcg.service.strategy;

import com.yobo.orcg.model.DocumentInfo;
import com.yobo.orcg.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.odftoolkit.simple.TextDocument;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class OdtFileMetadataExtractionStrategy implements MetadataExtractionStrategy {
    @Override
    public DocumentInfo extractMetadata(MultipartFile multipartFile){
        try {
            // Convert MultipartFile to File
            File file = FileUtils.convertToFile(multipartFile);

            // Use Apache Tika to get content type dynamically
            Tika tika = new Tika();
            String contentType = tika.detect(file);  // Automatically detects the content type

            // Extract the extension from the filename
            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());

            // Load the ODT file using Apache ODF Toolkit
            TextDocument document = TextDocument.loadDocument(file);

            // Extract metadata from ODT file

            long size = file.length();
            String readableSize = FileUtils.formatFileSize(size);


            // Return the extracted information as a DocumentInfo object
            return new DocumentInfo(extension, contentType, "", size, readableSize, multipartFile.getOriginalFilename());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract metadata from ODT file", e);
        }

    }
}
