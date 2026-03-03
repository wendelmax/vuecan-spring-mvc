import { createApp, ref, provide, inject, reactive, defineComponent, h } from 'vue';

const VuecanContextSymbol = Symbol('VuecanContext');

/**
 * Hook to access the Vuecan Context passed from the Spring Controller.
 */
export function useVuecanContext() {
    const context = inject(VuecanContextSymbol);
    if (!context) {
        console.warn('useVuecanContext() must be used inside a Vuecan component');
        return reactive({});
    }
    return context;
}

/**
 * Hook to access validation errors passed from Spring's BindingResult.
 */
export function useVuecanErrors() {
    const context = useVuecanContext();
    return context.errors || [];
}

/**
 * Hook to access the success status and global message.
 */
export function useVuecanStatus() {
    const context = useVuecanContext();
    return { success: context.success, message: context.message };
}

/**
 * Helper to render/hydrate a Vuecan component with the necessary providers.
 */
export function renderVuecanComponent(Component, rootElement) {
    const propsAttr = rootElement.getAttribute('data-vue-props');
    const props = propsAttr ? JSON.parse(propsAttr) : {};

    const globalContext = window.__VUECAN_CONTEXT__ || {};

    // Create a reactive state so updates can flow down
    const contextState = reactive({
        props,
        context: globalContext,
        errors: props.errors || [],
        success: props.success !== undefined ? props.success : true,
        message: props.message || null
    });

    // Listen for custom events if the backend pushes new state without reload
    window.addEventListener('vuecan-context-updated', () => {
        if (window.__VUECAN_CONTEXT__) {
            Object.assign(contextState.context, window.__VUECAN_CONTEXT__);
        }
    });

    const WrapperComponent = defineComponent({
        setup() {
            provide('VuecanContext', contextState);
            return () => h(Component, props);
        }
    });

    const app = createApp(WrapperComponent);
    app.mount(rootElement);
}

// Auto-mount script when loaded as ES module
const boot = async () => {
    const components = document.querySelectorAll('[data-vue-component]');
    if (components.length > 0) {
        for (const root of components) {
            const isIgnore = root.getAttribute('data-vue-ignore');
            if (isIgnore === 'true') continue;

            // Assume the module is the default export
            const componentName = root.getAttribute('data-vue-component');
            const ElementModule = await import(`/vue/components/${componentName}.js`);
            renderVuecanComponent(ElementModule.default, root);
        }
    }
};

document.addEventListener('DOMContentLoaded', boot);

