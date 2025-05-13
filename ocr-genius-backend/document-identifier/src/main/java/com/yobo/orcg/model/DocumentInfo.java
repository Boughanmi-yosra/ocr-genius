package com.yobo.orcg.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DocumentInfo {
    private String extension;
    private String contentType;
    private String creationDate;
    private long size; // in bytes
    private String readableSize;
    private String title ;
    public DocumentInfo(String extension, String contentType, String creationDate ,long size ,String readableSize,String title  ) {
        this.extension = extension;
        this.contentType =contentType;
        this.creationDate=creationDate;
        this.size = size;
        this.readableSize= readableSize;
        this.title = title;
    }


}
