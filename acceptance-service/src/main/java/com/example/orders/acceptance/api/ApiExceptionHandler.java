package com.example.orders.acceptance.api;

import com.example.orders.acceptance.service.OrderNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException ex) { return problem(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException ex) {
        var detail = problem(HttpStatus.BAD_REQUEST, "Request validation failed");
        detail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream().map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage() == null ? "invalid" : e.getDefaultMessage())).toList());
        return detail;
    }
    private ProblemDetail problem(HttpStatus status, String message) {
        var p = ProblemDetail.forStatusAndDetail(status, message); p.setProperty("timestamp", Instant.now()); return p;
    }
}
