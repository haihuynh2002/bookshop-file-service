package com.bookshop.file_service.file.domain;

public class FileStorageException extends FileServiceException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
