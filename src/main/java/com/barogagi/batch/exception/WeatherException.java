package com.barogagi.batch.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class WeatherException extends BusinessException {
    public WeatherException(ErrorCode errorCode) {
        super(errorCode);
    }
}
