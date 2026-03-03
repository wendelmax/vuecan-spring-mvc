package io.github.wendelmax.vuecan.dialect;

/**
 * Abstract resolution strategy for locating the physical file path of a Vue
 * component.
 *
 * @since 0.1.0
 */
public interface VuecanComponentResolver {

    /**
     * Resolves the frontend script URI path for a given component name.
     *
     * @param componentName the logical name of the Vue component (e.g.,
     *                      "products/Catalog").
     * @return the complete URI path resolvable by the browser (e.g.,
     *         "/vue/products/Catalog.vue").
     */
    String resolveScriptPath(String componentName);
}

