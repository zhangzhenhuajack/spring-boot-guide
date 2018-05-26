package com.alo7.framework.logging.filter;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

//@WebFilter(urlPatterns = "/*")
//@Component
//@Order(1)
public class LoggingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String traceId = ThreadContext.get("traceId");
        if (StringUtils.isEmpty(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put("traceId", traceId);
        try {
            if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
                throw new ServletException("OncePerRequestFilter just supports HTTP requests");
            }
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

            // Proceed without invoking this filter...
            chain.doFilter(wrappedRequest, wrappedResponse);

            // header ignore capital	and	small	letter
            wrappedResponse.setHeader("TraceId", traceId);
            wrappedResponse.copyBodyToResponse();
        } finally {
            MDC.remove("traceId");
        }
    }

    @Override
    public void destroy() {

    }
}
