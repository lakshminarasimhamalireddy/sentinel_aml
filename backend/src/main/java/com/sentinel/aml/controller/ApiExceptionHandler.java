package com.sentinel.aml.controller;

import com.sentinel.aml.dto.ApiError;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError notFound(NoSuchElementException e) {
        return new ApiError(Instant.now(), 404, e.getMessage());
    }

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError badRequest(Exception e) {
        return new ApiError(Instant.now(), 400, "Invalid request: "
                + (e instanceof MethodArgumentNotValidException ? "check required fields" : e.getMessage()));
    }
}
