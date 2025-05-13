package com.yobo.orcg.service.strategy;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.yobo.orcg.model.DocumentInfo;
import com.yobo.orcg.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ImageMetadataExtractionStrategy implements MetadataExtractionStrategy{
    @Override
    public DocumentInfo extractMetadata(MultipartFile multipartFile) {
        try {
            File file = FileUtils.convertToFile(multipartFile);
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            String contentType = multipartFile.getContentType();
            Date creationDate = (directory != null) ? directory.getDateOriginal() : null;
            String creationDateStr = (creationDate != null) ? creationDate.toString() : "Unknown";
            long size = file.length();
            String readableSize = FileUtils.formatFileSize(size);
            return new DocumentInfo(extension, contentType, creationDateStr, size, readableSize,multipartFile.getOriginalFilename());
        } catch (IOException | ImageProcessingException e) {
            throw new RuntimeException("Failed to extract image metadata", e);
        }
    }
}
