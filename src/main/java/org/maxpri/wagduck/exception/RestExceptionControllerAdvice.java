package org.maxpri.wagduck.exception;

import org.maxpri.wagduck.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionControllerAdvice {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> projectNotFoundException(ProjectNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(PrimaryKeyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> primaryKeyAlreadyExistsException(PrimaryKeyAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> duplicateEntityException(DuplicateEntityException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(DuplicateAttributeException.class)
    public ResponseEntity<ErrorResponse> duplicateAttributeException(DuplicateAttributeException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(NoEntitiesException.class)
    public ResponseEntity<ErrorResponse> noEntitiesException(NoEntitiesException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(e.getMessage())
                        .build());
    }
}
