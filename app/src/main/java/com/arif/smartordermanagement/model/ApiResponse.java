package com.arif.smartordermanagement.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    @SerializedName("status")
    private Status status;

    @SerializedName("data")
    private T data;

    @SerializedName("message")
    private String message;

    public ApiResponse() {
        // Required by serializers that instantiate response models reflectively.
    }

    public ApiResponse(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Status.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(Status.ERROR, data, message);
    }

    public static <T> ApiResponse<T> loading(T data) {
        return new ApiResponse<>(Status.LOADING, data, null);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String safeMessage() {
        return message == null || message.trim().isEmpty()
                ? "Something went wrong. Please try again."
                : message;
    }
}
