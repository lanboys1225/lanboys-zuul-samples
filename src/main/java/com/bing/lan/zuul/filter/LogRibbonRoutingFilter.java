package com.bing.lan.zuul.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.LOAD_BALANCER_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.RETRYABLE_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

/**
 * Created by lb on 2020/5/10.
 */
public class LogRibbonRoutingFilter extends RibbonRoutingFilter {

    private static final Log log = LogFactory.getLog(LogRibbonRoutingFilter.class);

    private boolean useServlet31 = true;

    public LogRibbonRoutingFilter(ProxyRequestHelper helper, RibbonCommandFactory<?> ribbonCommandFactory,
            List<RibbonRequestCustomizer> requestCustomizers) {
        super(helper, ribbonCommandFactory, requestCustomizers);
        checkServletVersion();
    }

    protected void checkServletVersion() {
        // To support Servlet API 3.1 we need to check if getContentLengthLong exists
        // Spring 5 minimum support is 3.0, so this stays
        try {
            HttpServletRequest.class.getMethod("getContentLengthLong");
            useServlet31 = true;
        } catch (NoSuchMethodException e) {
            useServlet31 = false;
        }
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        this.helper.addIgnoredHeaders();
        try {
            HttpServletRequest request = context.getRequest();
            MultiValueMap<String, String> headers = this.helper
                    .buildZuulRequestHeaders(request);
            MultiValueMap<String, String> params = this.helper
                    .buildZuulRequestQueryParams(request);
            String verb = getVerb(request);
            InputStream requestEntity = getRequestBody(request);
            if (request.getContentLength() < 0 && !verb.equalsIgnoreCase("GET")) {
                context.setChunkedRequestBody();
            }

            String serviceId = (String) context.get(SERVICE_ID_KEY);
            Boolean retryable = (Boolean) context.get(RETRYABLE_KEY);
            Object loadBalancerKey = context.get(LOAD_BALANCER_KEY);

            String uri = this.helper.buildZuulRequestURI(request);

            // remove double slashes
            uri = uri.replace("//", "/");

            long contentLength = useServlet31 ? request.getContentLengthLong()
                    : request.getContentLength();

            RibbonCommandContext ribbonCommandContext = new RibbonCommandContext(serviceId, verb, uri, retryable, headers, params,
                    requestEntity, this.requestCustomizers, contentLength, loadBalancerKey);
            ClientHttpResponse response = forward(ribbonCommandContext);

            byte[] contentData = log(request, headers, params, response);

            RequestContext.getCurrentContext().set("zuulResponse", response);
            this.helper.setResponse(response.getRawStatusCode(),
                    contentData == null ? null : new ByteArrayInputStream(contentData), response.getHeaders());
            return response;
        } catch (ZuulException ex) {
            throw new ZuulRuntimeException(ex);
        } catch (Exception ex) {
            throw new ZuulRuntimeException(ex);
        }
    }

    private byte[] log(HttpServletRequest request, MultiValueMap<String, String> headers,
            MultiValueMap<String, String> params, ClientHttpResponse response) throws IOException {
        long bufferStartTime = System.nanoTime();
        try {
            BufferedReader reader = null;
            try {
                reader = request.getReader();
            } catch (IOException e) {
                log.error("error during getReader", e);
            }

            log.info("requestBody : " + getHttpBody(reader));
            log.info("headers : " + headers);
            log.info("params : " + params);

            InputStream entity = response.getBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(entity, baos);
            byte[] contentData = baos.toByteArray();
            log.info("responseBody : " + getHttpBody(new ByteArrayInputStream(contentData)));
            return contentData;
        } catch (Throwable e) {
            log.error("error during log", e);
        } finally {
            log.info("log time : " + (System.nanoTime() - bufferStartTime));
        }
        return null;
    }

    public static String getHttpBody(InputStream entity) {
        if (entity == null) {
            return null;
        }
        try {
            return getHttpBody(new BufferedReader(new InputStreamReader(entity, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("error during getHttpBody", e);
        }
        return null;
    }

    public static String getHttpBody(BufferedReader reader) {
        if (reader == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        String value = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            value = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error during getHttpBody", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("error during getHttpBody close", e);
            }
        }
        return value;
    }
}
