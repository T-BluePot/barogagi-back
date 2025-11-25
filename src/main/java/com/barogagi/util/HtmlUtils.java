package com.barogagi.util;

import java.util.regex.Pattern;

public class HtmlUtils {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

    private HtmlUtils() {
        // util 클래스는 인스턴스 생성 방지
    }

    public static String stripHtml(String s) {
        if (s == null || s.isBlank()) return "";
        // 1) 태그 제거
        String t = HTML_TAG.matcher(s).replaceAll(" ");
        // 2) 엔티티 정리
        t = t.replace("&nbsp;", " ");
        // 3) 공백 정리
        return t.replaceAll("\\s+", " ").trim();
    }


    /** ```json ... ``` 형태 코드펜스/공백 제거 */
    public static String stripCodeFence(String s) {
        if (s == null) return null;
        // 앞쪽 ``` 또는 ```json, 뒤쪽 ``` 제거
        String t = s.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```(?:json)?\\s*", "");
        }
        if (t.endsWith("```")) {
            t = t.replaceFirst("```\\s*$", "");
        }
        return t.trim();
    }
}
