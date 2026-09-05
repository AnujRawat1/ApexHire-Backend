package com.ApexHire.common.exception;

import com.ApexHire.resume.exception.PythonServiceException;
import com.ApexHire.resume.exception.ResumeAnalysisException;
import com.ApexHire.resume.exception.ResumeFileException;
import com.ApexHire.resume.exception.ResumeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRefreshToken(InvalidRefreshTokenException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "status", 401,
                        "error", "Unauthorized",
                        "message", exception.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResumeNotFoundException(ResumeNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", exception.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(ResumeFileException.class)
    public ResponseEntity<Map<String, Object>> handleResumeFileException(ResumeFileException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", exception.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(ResumeAnalysisException.class)
    public ResponseEntity<Map<String, Object>> handleResumeAnalysisException(ResumeAnalysisException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", exception.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(PythonServiceException.class)
    public ResponseEntity<Map<String, Object>> handlePythonServiceException(PythonServiceException exception) {
        if (exception.getMessage().contains("temporarily unavailable")) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", 503,
                            "error", "Service Unavailable",
                            "message", exception.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", exception.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", exception.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }
}