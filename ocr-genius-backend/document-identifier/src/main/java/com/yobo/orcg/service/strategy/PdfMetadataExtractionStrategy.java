package com.yobo.orcg.service.strategy;

import com.yobo.orcg.model.DocumentInfo;
import com.yobo.orcg.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class PdfMetadataExtractionStrategy implements MetadataExtractionStrategy{
    @Override
    public DocumentInfo extractMetadata(MultipartFile multipartFile) {
        try{
            File file = FileUtils.convertToFile(multipartFile);
            PDDocument document = PDDocument.load(file);
            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());;
            String contentType = multipartFile.getContentType();
            Calendar creationDate = document.getDocumentInformation().getCreationDate();
            String creationDateStr = (creationDate != null) ? creationDate.getTime().toString() : "Unknown";
            long size = file.length();
            String readableSize = FileUtils.formatFileSize(size);
            return new DocumentInfo(extension, contentType, creationDateStr, size, readableSize,multipartFile.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract PDF metadata", e);
        }
    }
}
