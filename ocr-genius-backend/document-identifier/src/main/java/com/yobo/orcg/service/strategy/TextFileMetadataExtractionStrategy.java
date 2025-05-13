package com.yobo.orcg.service.strategy;

import com.yobo.orcg.model.DocumentInfo;
import com.yobo.orcg.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class TextFileMetadataExtractionStrategy implements MetadataExtractionStrategy {
    @Override
    public DocumentInfo extractMetadata(MultipartFile multipartFile) {
        try {
            File file = FileUtils.convertToFile(multipartFile);
            XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());;
            String contentType = multipartFile.getContentType();


            POIXMLProperties.CoreProperties props = doc.getProperties().getCoreProperties();

            // Extract creation date
            Date creationDate = props.getCreated();
            String creationDateStr = (creationDate != null) ? creationDate.toString() : "Unknown";
            long size = file.length();
            String readableSize = FileUtils.formatFileSize(size);

            return new DocumentInfo(extension, contentType, creationDateStr, size, readableSize,multipartFile.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text file metadata", e);
        }
    }
}