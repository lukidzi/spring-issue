package com.spring.sec.error.springissue.config;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class RequestLoggingFilter extends OncePerRequestFilter implements Ordered {

    private final Logger logger;

    public RequestLoggingFilter(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isFirstRequest = !isAsyncDispatch(request);
        /**
         * This fragment is used to log both sync & async request and sync request's response body.
         * It will never be called during Async Dispatch phase that is only used to dispatch response of async request.
         */
        if (isFirstRequest) {
            ContentCachingRequestWrapper requestToUse = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseToUse = new ContentCachingResponseWrapper(response);

            try {
                filterChain.doFilter(requestToUse, responseToUse);
            } finally {
                logger.info(createRequestMessage(requestToUse));
                if (!isAsyncStarted(requestToUse)) {
                    logger.info(createResponseMessage(responseToUse));
                }
            }
        }

        /**
         * This fragment is used only to log response body of async request during Async Dispatch phase.
         * It will never be called for synchronous requests.
         */
        else {
            ContentCachingResponseWrapper responseToUse = response instanceof ContentCachingResponseWrapper ?
                    (ContentCachingResponseWrapper) response : new ContentCachingResponseWrapper(response);

            try {
                filterChain.doFilter(request, responseToUse);
            } finally {
                logger.info(createResponseMessage(responseToUse));
            }
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    private String createRequestMessage(ContentCachingRequestWrapper request) {
        StringBuilder msg = new StringBuilder();
        msg.append("HTTP Request:");

        logUri(request, msg);
        logClientInfo(request, msg);

        readRequestPayload(request).ifPresent(payload -> logPayload(payload, msg));

        return msg.toString();
    }

    private String createResponseMessage(ContentCachingResponseWrapper response) {
        StringBuilder msg = new StringBuilder();
        msg.append("HTTP Response:");
        msg.append("\nStatus: ").append(response.getStatus());

        readResponsePayload(response).ifPresent(payload -> logPayload(payload, msg));

        return msg.toString();
    }

    private void logUri(HttpServletRequest request, StringBuilder msg) {
        msg.append("\n").append(request.getMethod()).append(" ").append(request.getRequestURI());
        String queryString = request.getQueryString();
        if (queryString != null) {
            msg.append('?').append(queryString);
        }
    }

    private void logClientInfo(HttpServletRequest request, StringBuilder msg) {
        msg.append("\n");
        String client = request.getRemoteAddr();
        if (StringUtils.hasLength(client)) {
            msg.append("client=").append(client);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            msg.append(";session=").append(session.getId());
        }
        String user = request.getRemoteUser();
        if (user != null) {
            msg.append(";user=").append(user);
        }
    }

    private void logPayload(String payload, StringBuilder msg) {
        msg.append("\nPayload: ").append(payload);
    }

    private Optional<String> readRequestPayload(ContentCachingRequestWrapper request) {
        if (request != null) {
            request.getParameterMap();
            return payloadToTruncatedString(request.getContentAsByteArray(), request.getCharacterEncoding());
        }
        return Optional.empty();
    }

    private Optional<String> readResponsePayload(ContentCachingResponseWrapper response) {
        if (response != null) {
            try {
                byte[] contentAsByteArray = response.getContentAsByteArray();
                response.copyBodyToResponse();
                return payloadToTruncatedString(contentAsByteArray, response.getCharacterEncoding());
            } catch (IOException e) {
            }
        }
        return Optional.empty();
    }

    private Optional<String> payloadToTruncatedString(byte[] buf, String encoding) {
        try {
            String payload = new String(buf, encoding);
            return Optional.of(payload);
        } catch (IOException e) {
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

