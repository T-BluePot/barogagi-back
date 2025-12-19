package com.barogagi.util;

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
                throw new BasicException("401", "접근 권한이 존재하지 않습니다.");
            }

            membershipNo = String.valueOf(membershipNoAttr);
            resultCode = "200";
            message = "회원 번호가 존재합니다.";

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
