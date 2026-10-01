package io.github.bayramsevim.reservationservice.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "İstek geçersiz");

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleValidation(IllegalStateException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleValidation(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
    }
}