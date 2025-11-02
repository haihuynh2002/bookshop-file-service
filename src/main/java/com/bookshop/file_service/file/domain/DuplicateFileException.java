package com.bookshop.file_service.file.domain;

public class DuplicateFileException extends FileServiceException {
    public DuplicateFileException(String message) {
        super(message);
    }
}