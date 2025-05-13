package com.yobo.orcg.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class FileMetadataExtractor {
    public static Date extractCreationDate(File file, String extension) throws IOException, ImageProcessingException {
        switch (extension.toLowerCase()) {
            case "pdf":
                return getPdfCreationDate(file);
            case "jpg":
            case "jpeg":
            case "png":
                return getImageCreationDate(file);
            default:
                return null;
        }
    }

    private static Date getPdfCreationDate(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            Calendar cal = document.getDocumentInformation().getCreationDate();
            return cal != null ? cal.getTime() : null;
        }
    }

    private static Date getImageCreationDate(File file) throws IOException, ImageProcessingException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        return directory != null ? directory.getDateOriginal() : null;
    }
}
