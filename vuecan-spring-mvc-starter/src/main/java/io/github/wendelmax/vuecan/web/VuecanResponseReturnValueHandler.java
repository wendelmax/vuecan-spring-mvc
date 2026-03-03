package io.github.wendelmax.vuecan.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Spring MVC Return Value Handler for methods evaluating to
 * {@link VuecanResponse}.
 * <p>
 * This class elegantly pivots between returning a standard Spring {@code ModelAndView}
 * holding proper Vuecan configuration models, or rendering direct RESTful JSON
 * payloads when intercepting specific mapping API paths.
 * </p>
 *
 * @since 0.1.0
 */
public class VuecanResponseReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

    @SuppressWarnings("removal")
	public VuecanResponseReturnValueHandler(ObjectMapper objectMapper) {
        this.messageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return VuecanResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) throws Exception {

        if (returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }

        VuecanResponse<?> vuecanResponse = (VuecanResponse<?>) returnValue;

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        // If the path maps to an API endpoint or the view implies a redirect/HTML
        // behavior override
        if (isApiRequest(request)) {
            handleApiResponse(vuecanResponse, mavContainer, webRequest);
        } else {
            handleViewResponse(vuecanResponse, returnType, mavContainer, webRequest);
        }
    }

    private void handleApiResponse(VuecanResponse<?> vuecanResponse, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (response == null) {
            return;
        }

        ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response);

        if (vuecanResponse.entity() != null && vuecanResponse.entity().getStatusCode() != null) {
            outputMessage.setStatusCode(vuecanResponse.entity().getStatusCode());
        }

        if (vuecanResponse.entity() != null && vuecanResponse.entity().getHeaders() != null) {
            outputMessage.getHeaders().putAll(vuecanResponse.entity().getHeaders());
        }

        // Write the VuecanResponse itself as the body
        MediaType contentType = outputMessage.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_JSON;
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(request);

        writeWithMessageConverter(vuecanResponse, vuecanResponse.getClass(), contentType, inputMessage,
                outputMessage);
    }

    @SuppressWarnings("unchecked")
    private void handleViewResponse(VuecanResponse<?> vuecanResponse, MethodParameter returnType,
            ModelAndViewContainer mavContainer, NativeWebRequest webRequest) {
        String viewName = vuecanResponse.view();

        if (viewName == null) {
            // Vuecan Convention: controllerPrefix/methodName
            String controllerName = returnType.getContainingClass().getSimpleName();
            if (controllerName.endsWith("Controller")) {
                controllerName = controllerName.substring(0, controllerName.length() - 10);
            }
            // CamelCase to kebab-case or just lowercase?
            // Let's go with simple lowercase for now as requested by user example
            // "basics/counter"
            controllerName = controllerName.toLowerCase();
            String methodName = returnType.getMethod().getName();
            viewName = controllerName + "/" + methodName;
        }

        mavContainer.setViewName(viewName);

        Object responseBody = vuecanResponse.entity() != null ? vuecanResponse.entity().getBody() : null;

        if (responseBody != null) {
            if (responseBody instanceof java.util.Map) {
                mavContainer.addAllAttributes((java.util.Map<String, ?>) responseBody);
            } else {
                mavContainer.addAttribute("data", responseBody);
            }
        }

        // Expose errors and success status
        if (vuecanResponse.errors() != null && !vuecanResponse.errors().isEmpty()) {
            mavContainer.addAttribute("errors", vuecanResponse.errors());
        }
        mavContainer.addAttribute("success", vuecanResponse.success());
        if (vuecanResponse.message() != null) {
            mavContainer.addAttribute("message", vuecanResponse.message());
        }

        java.util.Map<String, Object> combinedContext = new java.util.HashMap<>();

        // 1. Load Flash Attributes (TempData)
        HttpServletRequest request = ((NativeWebRequest) webRequest).getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            java.util.Map<String, ?> flashAttributes = org.springframework.web.servlet.support.RequestContextUtils
                    .getInputFlashMap(request);
            if (flashAttributes != null) {
                combinedContext.putAll(flashAttributes);
            }
        }

        // 2. Merge Vuecan Context (Current Request)
        if (vuecanResponse.context() != null) {
            combinedContext.putAll(vuecanResponse.context());
        }

        if (!combinedContext.isEmpty()) {
            mavContainer.addAttribute("__vuecan_context", combinedContext);
        }

        // Set status if possible
        if (vuecanResponse.entity() != null && vuecanResponse.entity().getStatusCode() != null) {
            if (vuecanResponse.entity().getStatusCode() instanceof org.springframework.http.HttpStatus s) {
                mavContainer.setStatus(s);
            }
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/api");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void writeWithMessageConverter(Object body, Class<?> valueType, MediaType contentType,
            ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws Exception {
        for (HttpMessageConverter converter : messageConverters) {
            if (converter.canWrite(valueType, contentType)) {
                converter.write(body, contentType, outputMessage);
                return;
            }
        }
        throw new IllegalStateException("No HttpMessageConverter for " + valueType + " and " + contentType);
    }
}

