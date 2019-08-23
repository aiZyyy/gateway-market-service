package com.sixi.gateway.marketservice.exception;


import org.springframework.http.HttpStatus;

/**
 * @Author: ZY
 * @Date: 2019/8/23 10:58
 * @Version 1.0
 * @Description:
 */
public class ServerException extends AbstractException {
    public ServerException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ServerException(ErrorCode errorCode) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorCode);
    }

    public ServerException(HttpStatus httpStatus, ErrorCode errorCode) {
        super(httpStatus, errorCode);
    }
}
