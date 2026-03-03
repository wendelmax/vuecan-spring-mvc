package io.github.wendelmax.vuecan.dialect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.github.wendelmax.vuecan.config.VuecanProperties;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import jakarta.servlet.http.HttpServletRequest;

import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The core Thymeleaf element processor for the {@code <vue:component>} XML
 * tag.
 * <p>
 * This processor parses standard HTML elements annotated with Vuecan prefixes,
 * evaluates their dynamic properties,
 * serializes Vue props to JSON, analyzes slotted HTML content, and
 * orchestrates the DOM injection of the
 * Vite development client or production standard entry points.
 * </p>
 *
 * @since 0.1.0
 */
public class VuecanComponentProcessor extends AbstractElementModelProcessor {

    private static final String ELEMENT_NAME = "component";
    private static final String SLOT_ELEMENT = "slot";
    private static final int PRECEDENCE = 1000;

    private static final Set<String> HTML_OPTION_ATTRS = Set.of(
            "name", "class", "id", "tag", "loading", "lazy", "poll", "ignore", "stateful", "preload");

    static final String HEADER_PARTIAL_PROPS_ONLY = "X-Partial-Props";
    static final String HEADER_PARTIAL_PROPS_EXCEPT = "X-Partial-Props-Except";
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final VuecanComponentResolver componentResolver;
    private final ObjectMapper objectMapper;
    private final VuecanProperties properties;

    // We only want to inject the Vite client once per page
    private static final String VITE_CLIENT_INJECTED = "vuecan.vite_client_injected";

    /**
     * Instantiates the core component processor with its properties mapping.
     *
     * @param dialectPrefix     the XML namespace tag prefix (e.g., "vue").
     * @param componentResolver the active logic-to-URI script resolver.
     * @param properties        the Vuecan properties object containing dev/prod
     *                          base URLs.
     */
    public VuecanComponentProcessor(String dialectPrefix, VuecanComponentResolver componentResolver,
            VuecanProperties properties) {
        super(TemplateMode.HTML, dialectPrefix, ELEMENT_NAME, true, null, false, PRECEDENCE);
        this.componentResolver = componentResolver;
        this.objectMapper = new ObjectMapper();
        this.properties = properties;
    }

    /**
     * Bootstraps the Thymeleaf node parsing operation, extracting props,
     * interpolating variables,
     * and writing the equivalent mountable HTML nodes into the AST.
     *
     * @param context          the template runtime evaluation context.
     * @param model            the structural AST node model representing the
     *                         component tag block.
     * @param structureHandler mutator to modify the parsed HTML document output.
     */
    @Override
    protected void doProcess(ITemplateContext context, IModel model,
            IElementModelStructureHandler structureHandler) {

        IProcessableElementTag tag = null;
        if (model.size() == 1 && model.get(0) instanceof IStandaloneElementTag standalone) {
            tag = standalone;
        } else if (model.size() >= 2 && model.get(0) instanceof IProcessableElementTag open) {
            tag = open;
        }
        if (tag == null) {
            return;
        }

        String rawComponentName = getAttributeValue(tag, "name");
        if (rawComponentName == null || rawComponentName.isBlank()) {
            return;
        }
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(context.getConfiguration());
        Object evalName = evaluateExpression(context, parser, rawComponentName);
        String componentName = evalName != null ? evalName.toString() : rawComponentName;

        Map<String, Object> props = extractProps(context, tag);
        props = unwrapLazyProps(props);
        Set<String> lazyProps = parseLazyProps(tag);
        props = filterPartialProps(context, props);
        props = filterLazyProps(context, props, lazyProps);
        String loadingComponent = getAttributeValue(tag, "loading");
        String pollInterval = getAttributeValue(tag, "poll");
        String preloadUrl = getAttributeValue(tag, "preload");
        boolean ignoreHydration = isIgnoreHydration(tag);
        boolean stateful = isStateful(tag);
        Map<String, IModel> slots = model.size() >= 2 ? extractSlots(model, context) : Map.of();

        String rootId = "vue-root-" + componentName.toLowerCase() + "-" + ID_COUNTER.incrementAndGet();
        String wrapperTag = getAttributeValue(tag, "tag");
        if (wrapperTag == null || wrapperTag.isBlank()) {
            wrapperTag = "div";
        }
        String cssClass = getAttributeValue(tag, "class");
        String elemId = getAttributeValue(tag, "id");
        if (elemId != null && !elemId.isBlank()) {
            rootId = elemId;
        }

        if (loadingComponent != null && !loadingComponent.isBlank()) {
            props.put("loadingComponent", loadingComponent);
        }
        if (pollInterval != null && !pollInterval.isBlank()) {
            props.put("refetchInterval", parsePollInterval(pollInterval));
        }
        if (preloadUrl != null && !preloadUrl.isBlank()) {
            props.put("preloadUrl", preloadUrl);
        }

        // Vue slots implementation logic might differ subtly in the frontend
        // compared to React where they were merged as props, but keeping the HTML
        // generation similar here so the JS client handles it.
        putSlotProps(context, props, slots);

        // Automatically inject global Vuecan metadata if not already present in props
        injectMetadata(context, props);

        String propsJson = serializeProps(props);
        String scriptPath = componentResolver.resolveScriptPath(componentName);

        StringBuilder html = new StringBuilder();

        // Inject Vite Client if devMode is active and it hasn't been injected yet
        if (properties.isDevMode() && getHttpServletRequest() != null) {
            HttpServletRequest request = getHttpServletRequest();
            if (request.getAttribute(VITE_CLIENT_INJECTED) == null) {
                html.append("<script type=\"module\" src=\"").append(escapeHtml(properties.getDevBase()))
                        .append("/@vite/client\"></script>\n");
                request.setAttribute(VITE_CLIENT_INJECTED, true);
            }
        }

        html.append("<").append(wrapperTag).append(" id=\"").append(escapeHtml(rootId)).append("\" ");
        if (cssClass != null && !cssClass.isBlank()) {
            html.append("class=\"").append(escapeHtml(cssClass)).append("\" ");
        }
        html.append("data-vue-component=\"").append(escapeHtml(componentName)).append("\" ");
        if (ignoreHydration) {
            html.append("data-vue-ignore=\"true\" ");
        }
        if (stateful) {
            html.append("data-vue-stateful=\"true\" ");
        }
        if (preloadUrl != null && !preloadUrl.isBlank()) {
            html.append("data-vue-preload=\"").append(escapeHtml(preloadUrl)).append("\" ");
        }
        if (pollInterval != null && !pollInterval.isBlank()) {
            int interval = parsePollInterval(pollInterval);
            if (interval > 0) {
                html.append("data-vue-poll=\"").append(interval).append("\" ");
            }
        }
        html.append("data-vue-props='").append(escapeJsonForAttr(propsJson)).append("'></");
        html.append(wrapperTag).append(">");

        // Use full URL for script if dev mode is active
        String scriptHtmlPath = scriptPath;
        if (properties.isDevMode()) {
            scriptHtmlPath = properties.getDevBase() + (scriptPath.startsWith("/") ? scriptPath : "/" + scriptPath);
        }
        html.append("\n<script type=\"module\" src=\"").append(escapeHtml(scriptHtmlPath)).append("\"></script>");

        IModelFactory modelFactory = context.getModelFactory();
        IModel output = modelFactory.createModel(modelFactory.createText(html.toString()));
        model.reset();
        model.addModel(output);
    }

    private Map<String, IModel> extractSlots(IModel model, ITemplateContext context) {
        Map<String, IModel> slots = new LinkedHashMap<>();
        IModelFactory modelFactory = context.getModelFactory();
        Set<Integer> consumedIndices = new HashSet<>();
        int end = model.size() - 1;

        for (int i = 1; i < end; i++) {
            if (consumedIndices.contains(i))
                continue;
            ITemplateEvent event = model.get(i);
            if (event instanceof IOpenElementTag openTag && isVueSlot(openTag)) {
                String slotName = getSlotName(openTag, context);
                if (slotName != null) {
                    int closeIdx = findMatchingCloseTag(model, i, "slot");
                    if (closeIdx > i) {
                        IModel slotBody = modelFactory.createModel();
                        for (int j = i + 1; j < closeIdx; j++) {
                            slotBody.add(model.get(j));
                            consumedIndices.add(j);
                        }
                        consumedIndices.add(i);
                        consumedIndices.add(closeIdx);
                        slots.put(slotName, slotBody);
                        i = closeIdx;
                        continue;
                    }
                }
            }
        }

        IModel defaultSlotContent = modelFactory.createModel();
        for (int i = 1; i < end; i++) {
            if (!consumedIndices.contains(i) && !isVueSlotOpenOrClose(model.get(i))) {
                defaultSlotContent.add(model.get(i));
            }
        }
        if (defaultSlotContent.size() > 0) {
            slots.put("default", defaultSlotContent);
        }
        return slots;
    }

    private String getElementLocalName(IElementTag tag) {
        String full = tag.getElementCompleteName();
        return full != null && full.contains(":") ? full.substring(full.lastIndexOf(':') + 1) : full;
    }

    private boolean isVueSlot(IOpenElementTag tag) {
        return SLOT_ELEMENT.equalsIgnoreCase(getElementLocalName(tag));
    }

    private boolean isVueSlotOpenOrClose(ITemplateEvent event) {
        if (event instanceof IOpenElementTag open) {
            return SLOT_ELEMENT.equalsIgnoreCase(getElementLocalName(open));
        }
        if (event instanceof ICloseElementTag close) {
            return SLOT_ELEMENT.equalsIgnoreCase(getElementLocalName(close));
        }
        return false;
    }

    private String getSlotName(IOpenElementTag tag, ITemplateContext context) {
        if (tag instanceof IProcessableElementTag processable) {
            String name = processable.getAttributeValue("vue", "name");
            if (name == null) {
                name = processable.getAttributeValue("name");
            }
            if (name != null) {
                IStandardExpressionParser parser = StandardExpressions.getExpressionParser(context.getConfiguration());
                Object eval = evaluateExpression(context, parser, name);
                return eval != null ? eval.toString() : name;
            }
            return name;
        }
        return null;
    }

    private int findMatchingCloseTag(IModel model, int openIdx, String elementName) {
        int depth = 1;
        for (int i = openIdx + 1; i < model.size(); i++) {
            ITemplateEvent event = model.get(i);
            if (event instanceof IOpenElementTag open
                    && elementName.equalsIgnoreCase(getElementLocalName(open))) {
                depth++;
            } else if (event instanceof ICloseElementTag close
                    && elementName.equalsIgnoreCase(getElementLocalName(close))) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void putSlotProps(ITemplateContext context, Map<String, Object> props, Map<String, IModel> slots) {
        for (var entry : slots.entrySet()) {
            String key = "default".equals(entry.getKey()) ? "children" : "slot" + capitalize(entry.getKey());
            String html = modelToHtml(entry.getValue());
            props.put(key, html);
        }
    }

    private String modelToHtml(IModel slotModel) {
        try {
            StringWriter writer = new StringWriter();
            slotModel.write(writer);
            return writer.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void injectMetadata(ITemplateContext context, Map<String, Object> props) {
        // Success status
        if (!props.containsKey("success") && context.containsVariable("success")) {
            props.put("success", context.getVariable("success"));
        }

        // Validation errors
        if (!props.containsKey("errors") && context.containsVariable("errors")) {
            props.put("errors", context.getVariable("errors"));
        }

        // Global message
        if (!props.containsKey("message") && context.containsVariable("message")) {
            props.put("message", context.getVariable("message"));
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static HttpServletRequest getHttpServletRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private Map<String, Object> filterPartialProps(ITemplateContext context, Map<String, Object> props) {
        var request = getHttpServletRequest();
        if (request == null) {
            return props;
        }
        String only = request.getHeader(HEADER_PARTIAL_PROPS_ONLY);
        if (only != null && !only.isBlank()) {
            Set<String> include = new HashSet<>(Arrays.asList(only.trim().split("[,;\\s]+")));
            Map<String, Object> filtered = new LinkedHashMap<>();
            for (var entry : props.entrySet()) {
                if (include.contains(entry.getKey())) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            return filtered;
        }
        String except = request.getHeader(HEADER_PARTIAL_PROPS_EXCEPT);
        if (except != null && !except.isBlank()) {
            Set<String> exclude = new HashSet<>(Arrays.asList(except.trim().split("[,;\\s]+")));
            Map<String, Object> filtered = new LinkedHashMap<>();
            for (var entry : props.entrySet()) {
                if (!exclude.contains(entry.getKey())) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            return filtered;
        }
        return props;
    }

    private int parsePollInterval(String value) {
        try {
            int n = Integer.parseInt(value.trim());
            return n > 0 ? n : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Map<String, Object> unwrapLazyProps(Map<String, Object> props) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (var entry : props.entrySet()) {
            result.put(entry.getKey(), unwrapValue(entry.getValue()));
        }
        return result;
    }

    private Object unwrapValue(Object value) {
        if (value instanceof Supplier<?> supplier) {
            return unwrapValue(supplier.get());
        }
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private Map<String, Object> extractProps(ITemplateContext context, IProcessableElementTag tag) {
        Map<String, Object> props = new LinkedHashMap<>();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(context.getConfiguration());

        for (var attr : tag.getAllAttributes()) {
            String completeName = attr.getAttributeCompleteName();
            String localName = completeName.contains(":")
                    ? completeName.substring(completeName.lastIndexOf(':') + 1)
                    : completeName;
            if (HTML_OPTION_ATTRS.contains(localName.toLowerCase())) {
                continue;
            }
            String value = attr.getValue();
            if (value == null) {
                continue;
            }
            Object evaluated = evaluateExpression(context, parser, value);
            if (evaluated != null) {
                props.put(localName, evaluated);
            }
        }
        return props;
    }

    private Object evaluateExpression(ITemplateContext context,
            IStandardExpressionParser parser, String expressionValue) {
        try {
            IStandardExpression expression = parser.parseExpression(context, expressionValue);
            return expression.execute(context);
        } catch (Exception e) {
            return expressionValue;
        }
    }

    private Set<String> parseLazyProps(IProcessableElementTag tag) {
        String value = getAttributeValue(tag, "lazy");
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return new HashSet<>(Arrays.asList(value.trim().split("[,;\\s]+")));
    }

    private Map<String, Object> filterLazyProps(ITemplateContext context, Map<String, Object> props,
            Set<String> lazyProps) {
        if (lazyProps.isEmpty()) {
            return props;
        }
        var request = getHttpServletRequest();
        if (request != null && request.getHeader(HEADER_PARTIAL_PROPS_ONLY) != null) {
            return props;
        }
        Map<String, Object> filtered = new LinkedHashMap<>();
        for (var entry : props.entrySet()) {
            if (!lazyProps.contains(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private boolean isStateful(IProcessableElementTag tag) {
        String value = getAttributeValue(tag, "stateful");
        if (value == null || value.isBlank()) {
            return false;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private boolean isIgnoreHydration(IProcessableElementTag tag) {
        String value = getAttributeValue(tag, "ignore");
        if (value == null || value.isBlank()) {
            return false;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String getAttributeValue(IProcessableElementTag tag, String attrName) {
        String value = tag.getAttributeValue("vue", attrName);
        if (value == null) {
            value = tag.getAttributeValue(attrName);
        }
        return value;
    }

    private String serializeProps(Map<String, Object> props) {
        try {
            return objectMapper.writeValueAsString(props);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String escapeHtml(String value) {
        if (value == null)
            return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeJsonForAttr(String json) {
        if (json == null)
            return "{}";
        return json.replace("'", "&#39;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
