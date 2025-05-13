package com.yobo.orcg.service.strategy;

public class MetadataExtractionStrategyFactory {
    public static MetadataExtractionStrategy getStrategy(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf":
                return new PdfMetadataExtractionStrategy();
            case "jpg":
            case "jpeg":
            case "png":
                return new ImageMetadataExtractionStrategy();
            case "docx":
                return new TextFileMetadataExtractionStrategy();
            case "odt":
                return new OdtFileMetadataExtractionStrategy();
            default:
                throw new UnsupportedOperationException("Unsupported file type: " + extension);
        }
    }
}
