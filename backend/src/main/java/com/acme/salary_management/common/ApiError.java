package com.acme.salary_management.common;

import java.util.Map;

/** Consistent error body for every failed request. */
public record ApiError(int status, String message, Map<String, String> fieldErrors) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Map.of());
    }
}