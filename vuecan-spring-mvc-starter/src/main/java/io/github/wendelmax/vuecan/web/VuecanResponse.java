package io.github.wendelmax.vuecan.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

/**
 * A generalized response container representing either a payload payload
 * directed at an API consumer, or structured model attributes bound for a
 * Vue component renderer.
 * <p>
 * Modeled conceptually after Spring's native {@link ResponseEntity}, but
 * enriched heavily with validation maps, execution context metadata, generic
 * statuses, and explicit view targets.
 * </p>
 *
 * @param <T> the type of the expected data representation.
 * @since 0.1.0
 */
public record VuecanResponse<T>(
        ResponseEntity<T> entity,
        boolean success,
        String message,
        List<Error> errors,
        String view,
        java.util.Map<String, Object> context) {

    /**
     * Dto modeling validation constraint infractions returned natively from the
     * framework mapping.
     */
    public record Error(String code, String field, String message) {
    }

    /**
     * Instantiates an empty VuecanResponse holding success status but lacking
     * payloads.
     */
    public VuecanResponse() {
        this(ResponseEntity.ok().build(), true, null, null, null, java.util.Collections.emptyMap());
    }

    /**
     * Instantiates an empty VuecanResponse holding the payload object payload.
     *
     * @param <T>  the data representation type.
     * @param data the model value object.
     * @return 200 OK.
     */
    public static <T> VuecanResponse<T> ok(T data) {
        return new VuecanResponse<>(ResponseEntity.ok(data), true, null, null, null, java.util.Collections.emptyMap());
    }

    public static <T> VuecanResponse<T> ok() {
        return new VuecanResponse<>(ResponseEntity.ok().build(), true, null, null, null,
                java.util.Collections.emptyMap());
    }

    /**
     * Extracts payload from the wrapper if implicitly present, dropping to 404
     * if empty.
     *
     * @param <T>      the type encapsulated by the Optional.
     * @param optional the nullable container evaluation.
     * @return 200 OK with payload or 404.
     */
    public static <T> VuecanResponse<T> ok(java.util.Optional<T> optional) {
        return optional.map(VuecanResponse::ok).orElseGet(() -> VuecanResponse.notFound());
    }

    /**
     * Explicitly requests a specific frontend Vue component to be rendered as the
     * main layout view.
     *
     * @param <T>      the empty payload type.
     * @param viewName the script path mapped to a component (e.g. "auth/Login").
     * @return the constructed VuecanResponse holding the specific view name
     *         trigger.
     */
    public static <T> VuecanResponse<T> view(String viewName) {
        return new VuecanResponse<>(ResponseEntity.ok().build(), true, null, null, viewName,
                java.util.Collections.emptyMap());
    }

    public static <T> VuecanResponse<T> view(String viewName, T data) {
        return new VuecanResponse<>(ResponseEntity.ok(data), true, null, null, viewName,
                java.util.Collections.emptyMap());
    }

    /**
     * Yields a 400 Bad Request error containing a global string message.
     *
     * @param <T>     the empty payload type.
     * @param message human readable error.
     * @return the error response.
     */
    public static <T> VuecanResponse<T> error(String message) {
        return new VuecanResponse<>(ResponseEntity.badRequest().build(), false, message, null, null,
                java.util.Collections.emptyMap());
    }

    public static <T> VuecanResponse<T> error(List<Error> errors) {
        return new VuecanResponse<>(ResponseEntity.badRequest().build(), false, null, errors, null,
                java.util.Collections.emptyMap());
    }

    /**
     * Yields a 201 Created successfully executed response.
     *
     * @param <T>      the data type.
     * @param location the new resource URI location header.
     * @param data     the created representation.
     * @return the response object.
     */
    public static <T> VuecanResponse<T> created(URI location, T data) {
        return new VuecanResponse<>(ResponseEntity.created(location).body(data), true, null, null, null,
                java.util.Collections.emptyMap());
    }

    public static <T> VuecanResponse<T> notFound() {
        return new VuecanResponse<>(ResponseEntity.notFound().build(), false, "Resource not found", null, null,
                java.util.Collections.emptyMap());
    }

    /**
     * Yields a 302 Redirect operation commanding the browser to fetch a different
     * controller path.
     * Allows chained operations using Flash data via the {@code withContext()}
     * builders.
     *
     * @param <T> the empty type.
     * @param url the destination mapping URL.
     * @return the redirect instruction response.
     */
    public static <T> VuecanResponse<T> redirect(String url) {
        return new VuecanResponse<>(ResponseEntity.status(HttpStatus.FOUND).build(), true, null, null,
                "redirect:" + url, java.util.Collections.emptyMap());
    }

    public VuecanResponse<T> withData(T data) {
        ResponseEntity<T> newEntity = ResponseEntity.status(entity.getStatusCode()).headers(entity.getHeaders())
                .body(data);
        return new VuecanResponse<>(newEntity, success, message, errors, view, context);
    }

    /**
     * Binds Spring's BindingResult constraint violations onto the structural
     * response payload.
     * This turns the current response into a 400 Bad Request, extracting all
     * violations neatly.
     *
     * @param springErrors the evaluated Spring Validation {@code Errors} output.
     * @return a new mutated VuecanResponse loaded with parsed {@link Error} field
     *         data.
     */
    public VuecanResponse<T> withErrors(org.springframework.validation.Errors springErrors) {
        if (springErrors == null || !springErrors.hasErrors()) {
            return this;
        }

        java.util.List<Error> newErrors = new java.util.ArrayList<>();
        if (this.errors != null) {
            newErrors.addAll(this.errors);
        }

        springErrors.getFieldErrors()
                .forEach(e -> newErrors.add(new Error(e.getCode(), e.getField(), e.getDefaultMessage())));
        springErrors.getGlobalErrors().forEach(e -> newErrors.add(new Error(e.getCode(), null, e.getDefaultMessage())));

        ResponseEntity<T> badRequestEntity = ResponseEntity.badRequest().headers(entity.getHeaders())
                .body(entity.getBody());
        return new VuecanResponse<>(badRequestEntity, false, message, newErrors, view, context);
    }

    public VuecanResponse<T> withView(String viewName) {
        return new VuecanResponse<>(entity, success, message, errors, viewName, context);
    }

    public VuecanResponse<T> withRedirect(String url) {
        ResponseEntity<T> redirectEntity = ResponseEntity.status(HttpStatus.FOUND).headers(entity.getHeaders())
                .body(entity.getBody());
        return new VuecanResponse<>(redirectEntity, success, message, errors, "redirect:" + url, context);
    }

    /**
     * Injects a specific property map entry dynamically into the payload
     * representation body.
     * Works natively if the data is a Map context.
     *
     * @param name  json payload key.
     * @param value json payload value object.
     * @return copy yielding the updated payload model.
     */
    public VuecanResponse<Object> with(String name, Object value) {
        java.util.Map<String, Object> newData = extractBodyAsMap();
        newData.put(name, value);
        return new VuecanResponse<>(shallowCopyEntity(newData), success, message, errors, view, context);
    }

    public VuecanResponse<Object> withAll(java.util.Map<String, ?> attributes) {
        java.util.Map<String, Object> newData = extractBodyAsMap();
        newData.putAll(attributes);
        return new VuecanResponse<>(shallowCopyEntity(newData), success, message, errors, view, context);
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> extractBodyAsMap() {
        if (entity.getBody() instanceof java.util.Map) {
            return new java.util.HashMap<>((java.util.Map<String, Object>) entity.getBody());
        }
        java.util.Map<String, Object> newData = new java.util.HashMap<>();
        if (entity.getBody() != null) {
            newData.put("data", entity.getBody());
        }
        return newData;
    }

    private <R> ResponseEntity<R> shallowCopyEntity(R newBody) {
        return ResponseEntity.status(entity.getStatusCode()).headers(entity.getHeaders()).body(newBody);
    }

    /**
     * Binds Flash architectural data or generic runtime states into the window
     * context payload seamlessly.
     * Extremely useful when chaining Redirects.
     *
     * @param name  the name of the memory context key.
     * @param value the memory state object to embed.
     * @return a mutated copy with the added context payload.
     */
    public VuecanResponse<T> withContext(String name, Object value) {
        java.util.Map<String, Object> newContext = new java.util.HashMap<>(context);
        newContext.put(name, value);
        return new VuecanResponse<>(entity, success, message, errors, view, newContext);
    }

    public VuecanResponse<T> withStatus(HttpStatus status) {
        ResponseEntity<T> newEntity = ResponseEntity.status(status).headers(entity.getHeaders()).body(entity.getBody());
        return new VuecanResponse<>(newEntity, success, message, errors, view, context);
    }

    // JSON ignore these metadata fields when serializing to client if needed
    // But for now, let's keep it simple and see how it looks.
}

