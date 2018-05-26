package com.alo7.framework.logging.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * 日志拦截器
 */
@Setter
@Slf4j
public class RequestLoggingInterceptor extends HandlerInterceptorAdapter {
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private static final String START_TIME_KEY = "__START_TIME";

    public static final String DEFAULT_BEFORE_MESSAGE_PREFIX = "Before request [";

    public static final String DEFAULT_BEFORE_MESSAGE_SUFFIX = "]";

    public static final String DEFAULT_AFTER_MESSAGE_PREFIX = "After request [";

    public static final String DEFAULT_AFTER_MESSAGE_SUFFIX = "]";

    private String beforeMessagePrefix = DEFAULT_BEFORE_MESSAGE_PREFIX;

    private String beforeMessageSuffix = DEFAULT_BEFORE_MESSAGE_SUFFIX;

    private String afterMessagePrefix = DEFAULT_AFTER_MESSAGE_PREFIX;

    private String afterMessageSuffix = DEFAULT_AFTER_MESSAGE_SUFFIX;

    /**
     * 是否打印日志，如果有需要以后可以扩展
     *
     * @return
     */
    private boolean shouldLog() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (shouldLog()) {
            request.setAttribute(START_TIME_KEY, System.currentTimeMillis());
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }

            log.debug("{}RequestUri=[{}];Parameter=[{}];RequestHeader=[{}];RequestBody=[{}]{}",
                    beforeMessagePrefix,
                    request.getRequestURI(),
                    objectMapper.writeValueAsString(request.getParameterMap()),
                    objectMapper.writeValueAsString(new ServletServerHttpRequest(request).getHeaders()),
                    getRequestMessagePayload(request),
                    beforeMessageSuffix);
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (shouldLog()) {
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }

            //开始时间
            long startTime = (Long) request.getAttribute(START_TIME_KEY);
            //时间用时
            long executionTime = System.currentTimeMillis() - startTime;
            //参考相应时间
            long standardTime = 3000L;

            if (executionTime > standardTime) {
                log.warn("URI {} ;ExecuteTimeMillis Warning {}/{}", request.getRequestURI(), executionTime, standardTime);
            }

            if (ex != null) {
                log.warn(ex.getMessage(), ex);
            }

            HttpHeaders httpHeaders = null;
            try (ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response)) {
                httpHeaders = httpResponse.getHeaders();
            }

            log.debug("{}RequestUri=[{}];Parameter=[{}];RequestHeader=[{}];RequestBody=[{}];ResponseHeader=[{}];;ResponseBody=[{}];ExecuteTimeMillis=[{}];Exception=[{}]{}",
                    afterMessagePrefix,
                    request.getRequestURI(),
                    objectMapper.writeValueAsString(request.getParameterMap()),
                    objectMapper.writeValueAsString(new ServletServerHttpRequest(request).getHeaders()),
                    getRequestMessagePayload(request),
                    objectMapper.writeValueAsString(httpHeaders),
                    getResponseMessagePayload(response),
                    String.valueOf(executionTime),
                    ex,
                    afterMessageSuffix);
        }

        super.afterCompletion(request, response, handler, ex);
    }

    /**
     * 获得请求的请求体
     *
     * @param request
     * @return
     */
    protected String getRequestMessagePayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            return getContentString(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
        }
        return "Request Warp Null";
    }

    private String getContentString(byte[] buf, String charsetName) {
        int limitLength = 1000;
        if (buf.length > 0) {
            int maxLength = buf.length > limitLength ? limitLength : buf.length;
            try {
                StringBuilder body = new StringBuilder(new String(buf, 0, maxLength, charsetName));
                return buf.length > limitLength ? body.append("......").toString() : body.toString();
            } catch (UnsupportedEncodingException ex) {
                log.warn(ex.getMessage(), ex);
                return "[unknown]";
            }
        }
        return "[null content]";
    }

    /**
     * 获得请求的请求体
     *
     * @param response
     * @return
     */
    protected String getResponseMessagePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            return getContentString(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
        }
        return "Response Warp Null";
    }

}
