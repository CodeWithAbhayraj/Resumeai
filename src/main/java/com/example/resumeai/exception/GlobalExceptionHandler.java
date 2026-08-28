package com.example.resumeai.exception;

import com.example.resumeai.service.GeminiServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================
    // GEMINI / AI SERVICE ERROR
    // ==========================================

    @ExceptionHandler(GeminiServiceException.class)
    public ResponseEntity<Map<String, String>> handleGeminiException(
            GeminiServiceException ex) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        Map.of(
                                "error", "AI_SERVICE_BUSY",
                                "message", ex.getMessage()
                        )
                );
    }


    // ==========================================
    // OTHER UNEXPECTED ERRORS
    // ==========================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(
            Exception ex) {

        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "error", "INTERNAL_SERVER_ERROR",
                                "message",
                                "Something went wrong. Please try again later."
                        )
                );
    }
}