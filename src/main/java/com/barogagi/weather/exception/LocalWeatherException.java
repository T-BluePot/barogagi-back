package com.barogagi.weather.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class LocalWeatherException extends BusinessException {
    public LocalWeatherException(ErrorCode errorCode) {
        super(errorCode);
    }
}
