package com.yobo.orcg.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static File convertToFile(MultipartFile multipartFile) throws IOException {
        File convFile = File.createTempFile("upload", multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(multipartFile.getBytes());
        }
        return convFile;
    }

    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) return sizeInBytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(sizeInBytes)) / 10;
        return String.format("%.1f %sB", (double) sizeInBytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
