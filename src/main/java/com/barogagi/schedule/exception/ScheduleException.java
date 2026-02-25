package com.barogagi.schedule.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ScheduleException extends BusinessException {

    public ScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
