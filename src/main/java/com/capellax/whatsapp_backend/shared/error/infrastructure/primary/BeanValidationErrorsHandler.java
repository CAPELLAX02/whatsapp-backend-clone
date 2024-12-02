package com.capellax.whatsapp_backend.shared.error.infrastructure.primary;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
class BeanValidationErrorsHandler {

    private static final Logger log = LoggerFactory.getLogger(BeanValidationErrorsHandler.class);
    private static final String ERRORS = "errors";
    private static final String DEFAULT_TITLE = "Bean validation error";
    private static final String DEFAULT_DETAIL = "One or more fields were invalid. See 'errors' for details.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.info("Handling MethodArgumentNotValidException: {}", exception.getMessage());
        return createProblemDetail(buildFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException exception) {
        log.info("Handling ConstraintViolationException: {}", exception.getMessage());
        return createProblemDetail(buildConstraintErrors(exception));
    }

    private ProblemDetail createProblemDetail(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, DEFAULT_DETAIL);
        problem.setTitle(DEFAULT_TITLE);
        problem.setProperty(ERRORS, errors != null ? errors : Collections.emptyMap());
        return problem;
    }

    private Map<String, String> buildFieldErrors(Iterable<FieldError> fieldErrors) {
        return StreamSupport.stream(fieldErrors.spliterator(), false)
                .collect(Collectors.toUnmodifiableMap(
                        FieldError::getField,
                        fieldError ->
                                Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value")
                        )
                );
    }

    private Map<String, String> buildConstraintErrors(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .collect(Collectors.toUnmodifiableMap(
                        error -> getLastSegment(error.getPropertyPath().toString()),
                        ConstraintViolation::getMessage));
    }

    private String getLastSegment(String propertyPath) {
        return Optional.ofNullable(propertyPath)
                .map(path -> path.substring(path.lastIndexOf('.') + 1))
                .orElse("");
    }

}
