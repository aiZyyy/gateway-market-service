package com.sixi.gateway.marketservice.exception;

/**
 * @Author: ZY
 * @Date: 2019/8/23 11:01
 * @Version 1.0
 * @Description: 错误码
 */
public class ErrorCode {

    private final int code;
    private final String message;
    private final String detailMessage;

    public ErrorCode(int code, String message, String detailMessage) {
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;
    }

    public ErrorCode(String message, String detailMessage) {
        this.code = 0;
        this.message = message;
        this.detailMessage = detailMessage;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetailMessage() {
        return detailMessage;
    }
}
