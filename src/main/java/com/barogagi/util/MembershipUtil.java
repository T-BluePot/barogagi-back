package com.barogagi.util;

import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
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
                throw new BasicException(ErrorCode.NOT_EXIST_ACCESS_AUTH);
            }

            membershipNo = String.valueOf(membershipNoAttr);
            resultCode = ErrorCode.EXIST_ACCESS_AUTH.getCode();
            message = ErrorCode.EXIST_ACCESS_AUTH.getMessage();

        } catch (BasicException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();
        } finally {
            resultMap.put("resultCode", resultCode);
            resultMap.put("message", message);
            resultMap.put("membershipNo", membershipNo);
        }

        return resultMap;
    }

    public String selectMembershipNo(HttpServletRequest request) {

        Object membershipNoAttr = request.getAttribute("membershipNo");
        if(null == membershipNoAttr) {
            throw new BasicException(ErrorCode.NOT_EXIST_ACCESS_AUTH);
        }

        return String.valueOf(membershipNoAttr);
    }
}
