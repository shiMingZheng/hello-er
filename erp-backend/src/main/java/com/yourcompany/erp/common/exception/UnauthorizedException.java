package com.yourcompany.erp.common.exception;

/**
 * 未授权异常（401）
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }

}
