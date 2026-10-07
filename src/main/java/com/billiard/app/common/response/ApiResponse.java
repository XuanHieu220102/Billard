package com.billiard.app.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Unified response envelope for every API endpoint.
 *
 * <pre>{@code
 * { "success": true,  "data": {...}, "message": null }
 * { "success": false, "data": null,  "message": "...", "code": "TABLE_NOT_FOUND" }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(boolean success, T data, String message, String code) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, null, message, code);
    }
}
