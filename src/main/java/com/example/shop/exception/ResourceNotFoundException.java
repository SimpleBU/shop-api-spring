package com.example.shop.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resource;

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " '" + id + "' not found");
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
