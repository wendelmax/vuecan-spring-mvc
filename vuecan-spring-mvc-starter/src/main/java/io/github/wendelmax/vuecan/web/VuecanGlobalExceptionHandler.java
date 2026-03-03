package io.github.wendelmax.vuecan.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for standardizing error responses.
 * <p>
 * This class captures validation exceptions (like {@code @Valid} failures) and
 * general unhandled server exceptions, wrapping them elegantly inside a
 * {@link VuecanResponse}.
 * </p>
 *
 * @since 0.1.0
 */
@ControllerAdvice(annotations = ResponseBody.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class VuecanGlobalExceptionHandler {

    /**
     * Intercepts Spring MVC method argument validation failures and extracts field
     * errors.
     *
     * @param ex the validation exception thrown by the framework.
     * @return a {@link VuecanResponse} containing a formatted list of validation
     *         errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public VuecanResponse<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<VuecanResponse.Error> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new VuecanResponse.Error("VALIDATION_ERROR", error.getField(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        return VuecanResponse.error(errors);
    }

    /**
     * Intercepts any unhandled, unexpected exceptions returning a generic bad
     * request wrapper.
     *
     * @param ex the caught generic exception.
     * @return a {@link VuecanResponse} containing the generic error message
     *         string.
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public VuecanResponse<Object> handleAllExceptions(Exception ex) {
        return VuecanResponse.error(ex.getMessage());
    }
}

