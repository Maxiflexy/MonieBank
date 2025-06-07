//package com.maxiflexy.transaction_service.exception;
//
//import com.maxiflexy.transaction_service.dto.ApiResponse;
//import org.hibernate.exception.ConstraintViolationException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.client.HttpClientErrorException;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@ControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
//        ApiResponse response = new ApiResponse(false, ex.getMessage());
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(InsufficientFundsException.class)
//    public ResponseEntity<ApiResponse> handleInsufficientFundsException(InsufficientFundsException ex) {
//        ApiResponse response = new ApiResponse(false, ex.getMessage());
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
//        String errorMessage = "Data integrity violation";
//
//        // Try to extract more specific error message
//        if (ex.getCause() instanceof ConstraintViolationException) {
//            ConstraintViolationException cause = (ConstraintViolationException) ex.getCause();
//            if (cause.getSQLException() != null && cause.getSQLException().getMessage().contains("unique")) {
//                errorMessage = "A record with this information already exists";
//            }
//        }
//
//        log.error("Data integrity violation: {}", ex.getMessage());
//        ApiResponse response = new ApiResponse(false, errorMessage);
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(HttpClientErrorException.class)
//    public ResponseEntity<ApiResponse> handleHttpClientErrorException(HttpClientErrorException ex) {
//        log.error("HTTP Client error: {}", ex.getMessage());
//        ApiResponse response = new ApiResponse(false, "Error communicating with external service: " + ex.getStatusText());
//        return new ResponseEntity<>(response, ex.getStatusCode());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
//        ApiResponse response = new ApiResponse(false, "An unexpected error occurred: " + ex.getMessage());
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}
//
