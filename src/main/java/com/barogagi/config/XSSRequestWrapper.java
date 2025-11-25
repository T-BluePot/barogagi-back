package com.barogagi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return clean(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;

        for (int i = 0; i < values.length; i++) {
            values[i] = clean(values[i]);
        }
        return values;
    }

    private String clean(String value) {
        if (value == null) return null;
        // 간단한 정제 로직
        return value
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("'", "&#39;")
                .replaceAll("\"", "&quot;")
                .replaceAll("eval\\((.*)\\)", "")
                .replaceAll("script", "")
                .replaceAll("javascript:", "");
    }
}
