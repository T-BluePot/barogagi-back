package com.barogagi.batch.vo;

public class SendResult {
    private final boolean success;
    private final String errorMessage; // 실패 시에만 채워짐

    public SendResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
