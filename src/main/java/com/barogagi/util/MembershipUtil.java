package com.barogagi.util;

import com.barogagi.config.resultCode.ResultCode;
import com.barogagi.util.exception.BasicException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MembershipUtil {

    public Map<String, Object> membershipNoService(HttpServletRequest request) {

        Map<String, Object> resultMap = new HashMap<>();

        String resultCode = "";
        String message = "";
        String membershipNo = "";

        try {
            Object membershipNoAttr = request.getAttribute("membershipNo");
            if(membershipNoAttr == null) {
                throw new BasicException(
                        ResultCode.NOT_EXIST_ACCESS_AUTH.getResultCode(),
                        ResultCode.NOT_EXIST_ACCESS_AUTH.getMessage()
                );
            }

            membershipNo = String.valueOf(membershipNoAttr);
            resultCode = ResultCode.EXIST_ACCESS_AUTH.getResultCode();
            message = ResultCode.EXIST_ACCESS_AUTH.getMessage();

        } catch (BasicException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();
        } finally {
            resultMap.put("resultCode", resultCode);
            resultMap.put("message", message);
            resultMap.put("membershipNo", membershipNo);
        }

        return resultMap;
    }
}
