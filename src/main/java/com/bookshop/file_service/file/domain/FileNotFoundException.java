package com.bookshop.file_service.file.domain;

public class FileNotFoundException extends FileServiceException{
    public FileNotFoundException(String message) {
        super(message);
    }
}
