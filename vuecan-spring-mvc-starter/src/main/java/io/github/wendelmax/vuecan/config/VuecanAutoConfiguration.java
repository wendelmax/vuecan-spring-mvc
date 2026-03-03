package io.github.wendelmax.vuecan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wendelmax.vuecan.web.VuecanGlobalExceptionHandler;
import io.github.wendelmax.vuecan.dialect.DefaultVuecanComponentResolver;
import io.github.wendelmax.vuecan.dialect.VuecanComponentResolver;
import io.github.wendelmax.vuecan.dialect.VuecanDialect;
import io.github.wendelmax.vuecan.registry.VuecanComponentRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Optional;

/**
 * Automatic Spring Boot configuration for Vuecan.
 * <p>
 * This configuration class registers the core components of Vuecan:
 * the component resolver, the Thymeleaf dialect, the global exception handler,
 * and the Spring Web MVC configuration required to support Vuecan's return
 * handlers.
 * </p>
 *
 * @since 0.1.0
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(SpringTemplateEngine.class)
@EnableConfigurationProperties(VuecanProperties.class)
public class VuecanAutoConfiguration {

    /**
     * Provides the default {@link VuecanComponentResolver} bean.
     *
     * @param properties the Vuecan properties configuration.
     * @param registry   an optional {@link VuecanComponentRegistry} to resolve
     *                   dynamic components.
     * @return the configured {@link VuecanComponentResolver}.
     */
    @Bean
    @ConditionalOnMissingBean
    public VuecanComponentResolver vuecanComponentResolver(VuecanProperties properties,
            Optional<VuecanComponentRegistry> registry) {
        return new DefaultVuecanComponentResolver(properties, registry.orElse(null));
    }

    /**
     * Provides the {@link VuecanDialect} bean for Thymeleaf.
     *
     * @param componentResolver the resolver used to map components to paths.
     * @param properties        the Vuecan properties configuration.
     * @return the {@link VuecanDialect} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public VuecanDialect vuecanDialect(VuecanComponentResolver componentResolver, VuecanProperties properties) {
        return new VuecanDialect(componentResolver, properties);
    }

    /**
     * Provides a global exception handler to standardise unhandled server errors.
     *
     * @return the {@link VuecanGlobalExceptionHandler} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public VuecanGlobalExceptionHandler vuecanGlobalExceptionHandler() {
        return new VuecanGlobalExceptionHandler();
    }

    /**
     * Registers Vuecan's custom return value handlers into Spring MVC framework.
     *
     * @param properties           the Vuecan properties configuration.
     * @param objectMapperProvider the Jackson {@link ObjectMapper} provider.
     * @return the configuration bean mapping handlers and CORS rules.
     */
    @Bean
    @ConditionalOnMissingBean(name = "vuecanWebMvcConfigurer")
    public WebMvcConfigurer vuecanWebMvcConfigurer(VuecanProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new VuecanWebMvcConfigurer(properties, objectMapper);
    }
}

