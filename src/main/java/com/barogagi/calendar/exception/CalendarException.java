package com.barogagi.calendar.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CalendarException extends BusinessException {
    public CalendarException(ErrorCode errorCode) {
        super(errorCode);
    }
}
