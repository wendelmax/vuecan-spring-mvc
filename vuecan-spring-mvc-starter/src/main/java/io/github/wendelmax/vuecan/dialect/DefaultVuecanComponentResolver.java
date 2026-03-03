package io.github.wendelmax.vuecan.dialect;

import io.github.wendelmax.vuecan.config.VuecanProperties;
import io.github.wendelmax.vuecan.registry.VuecanComponentRegistry;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * The default implementation of {@link VuecanComponentResolver}.
 * <p>
 * This class uses the globally defined {@link VuecanProperties} to figure out
 * mapping strategies,
 * or falls back to standard configurations if strict mappings are provided.
 * </p>
 *
 * @since 0.1.0
 */
public class DefaultVuecanComponentResolver implements VuecanComponentResolver {

    private final VuecanProperties properties;
    private final VuecanComponentRegistry registry;
    private final String fixedBasePath;
    private final String fixedExtension;

    /**
     * Creates a resolver using fixed explicit mapping values.
     *
     * @param basePath  the fixed base path.
     * @param extension the fixed file extension.
     */
    public DefaultVuecanComponentResolver(String basePath, String extension) {
        this.properties = null;
        this.registry = null;
        this.fixedBasePath = basePath != null ? basePath : "/vue";
        this.fixedExtension = StringUtils.hasText(extension) ? extension : ".vue";
    }

    /**
     * Creates a resolver inheriting strictly from Spring Boot configuration
     * properties.
     *
     * @param properties the autowired configuration map.
     */
    public DefaultVuecanComponentResolver(VuecanProperties properties) {
        this(properties, null);
    }

    /**
     * Creates a resolver using Spring Boot configuration properties and an
     * auxiliary dynamic registry.
     *
     * @param properties the Vuecan properties.
     * @param registry   a registry for programmatic path injection overrides.
     */
    public DefaultVuecanComponentResolver(VuecanProperties properties, VuecanComponentRegistry registry) {
        this.properties = properties;
        this.registry = registry;
        this.fixedBasePath = null;
        this.fixedExtension = null;
    }

    /**
     * Resolves the component URI by parsing dynamic registries or falling back to
     * environment properties.
     *
     * @param componentName logic component name bound in the Thymeleaf template.
     * @return the resolved URI path.
     */
    @Override
    public String resolveScriptPath(String componentName) {
        if (registry != null) {
            Optional<String> path = registry.resolveScriptPath(componentName);
            if (path.isPresent()) {
                return path.get();
            }
        }
        String basePath = fixedBasePath != null ? fixedBasePath : resolveBasePath();
        String extension = fixedExtension != null ? fixedExtension : resolveExtension();
        String path = basePath.endsWith("/") ? basePath : basePath + "/";
        String ext = extension.startsWith(".") ? extension : "." + extension;
        return path + componentName + ext;
    }

    private String resolveBasePath() {
        if (properties == null) {
            return "/vue";
        }
        if (properties.isDevMode() && StringUtils.hasText(properties.getDevBase())) {
            return properties.getDevBase();
        }
        if (!properties.isDevMode() && StringUtils.hasText(properties.getProdBase())) {
            return properties.getProdBase();
        }
        return StringUtils.hasText(properties.getComponentBasePath()) ? properties.getComponentBasePath() : "/vue";
    }

    private String resolveExtension() {
        if (properties == null) {
            return ".vue";
        }
        return StringUtils.hasText(properties.getComponentExtension()) ? properties.getComponentExtension() : ".vue";
    }
}

