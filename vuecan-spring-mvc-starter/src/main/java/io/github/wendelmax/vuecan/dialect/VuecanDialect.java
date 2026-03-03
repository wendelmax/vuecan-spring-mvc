package io.github.wendelmax.vuecan.dialect;

import io.github.wendelmax.vuecan.config.VuecanProperties;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom Thymeleaf dialect enabling the {@code <vue:component>} tag namespace.
 * <p>
 * This dialect registers the core execution environment for identifying and
 * transforming server-side
 * custom tags into DOM-mountable elements digestible by the Vue frontend
 * engine.
 * </p>
 *
 * @since 0.1.0
 */
public class VuecanDialect extends AbstractProcessorDialect {

    private static final String DIALECT_PREFIX = "vue";

    private final VuecanComponentResolver componentResolver;
    private final VuecanProperties properties;

    /**
     * Constructs the Vuecan dialect environment.
     *
     * @param componentResolver the path locator handling {@code .vue} script resolution.
     * @param properties        the globally injected Vuecan property configurations.
     */
    public VuecanDialect(VuecanComponentResolver componentResolver, VuecanProperties properties) {
        // "vuecan" is the logical name space, "vue" is the prefix in HTML tags
        // <vue:component>
        super("Vuecan", DIALECT_PREFIX, 1000);
        this.componentResolver = componentResolver;
        this.properties = properties;
    }

    /**
     * Wires standard processors mapping to the prefix.
     *
     * @param dialectPrefix the namespace prefix ("vue").
     * @return the set of active processors (e.g., VuecanComponentProcessor)
     */
    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<>();
        processors.add(new VuecanComponentProcessor(dialectPrefix, componentResolver, properties));
        return processors;
    }
}

