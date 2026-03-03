package io.github.wendelmax.vuecan.registry;

import java.util.Optional;

public interface VuecanComponentRegistry {

    Optional<String> resolveScriptPath(String componentName);

    static VuecanComponentRegistry fromMap(java.util.Map<String, String> mappings) {
        return new MapBasedVuecanComponentRegistry(mappings);
    }

    class MapBasedVuecanComponentRegistry implements VuecanComponentRegistry {
        private final java.util.Map<String, String> mappings;

        public MapBasedVuecanComponentRegistry(java.util.Map<String, String> mappings) {
            this.mappings = mappings != null ? java.util.Map.copyOf(mappings) : java.util.Map.of();
        }

        @Override
        public Optional<String> resolveScriptPath(String componentName) {
            return Optional.ofNullable(mappings.get(componentName));
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private final java.util.Map<String, String> mappings = new java.util.LinkedHashMap<>();

        public Builder register(String componentName, String scriptPath) {
            mappings.put(componentName, scriptPath);
            return this;
        }

        public VuecanComponentRegistry build() {
            return new MapBasedVuecanComponentRegistry(mappings);
        }
    }
}

