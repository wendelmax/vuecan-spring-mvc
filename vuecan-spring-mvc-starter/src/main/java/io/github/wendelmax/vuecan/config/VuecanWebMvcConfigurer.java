package io.github.wendelmax.vuecan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wendelmax.vuecan.web.VuecanResponseReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Global Web MVC configuration for the Vuecan system.
 * <p>
 * This class binds the specific CORS settings for the dynamically generated
 * API endpoints and establishes Vuecan's return value handler in priority
 * index 0.
 * </p>
 *
 * @since 0.1.0
 */
public class VuecanWebMvcConfigurer implements WebMvcConfigurer {

    private static final String API_PATH_PATTERN = "/api/**";
    private static final String[] ALLOWED_METHODS = { "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" };
    private static final String ALLOWED_HEADERS = "*";

    private final VuecanProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Creates a newly instantiated WebMvcConfigurer for Vuecan.
     *
     * @param properties   the application scoped Vuecan properties.
     * @param objectMapper the global Spring Boot provided ObjectMapper.
     */
    public VuecanWebMvcConfigurer(VuecanProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Automatically registers CORS configuration mapped by
     * {@code vuecan.api.cors-allowed-origins}.
     *
     * @param registry Spring's standard CORS mapping registry.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var allowedOrigins = properties.getApi().getCorsAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return;
        }
        registry.addMapping(API_PATH_PATTERN)
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .allowCredentials(true);
    }

    /**
     * Pushes the {@link VuecanResponseReturnValueHandler} into the Spring MVC
     * framework,
     * allowing it to process {@code VuecanResponse} controller returns
     * automatically.
     *
     * @param handlers the mutable list of return value handlers in Spring framework
     *                 context.
     */
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(0, new VuecanResponseReturnValueHandler(objectMapper));
    }
}

