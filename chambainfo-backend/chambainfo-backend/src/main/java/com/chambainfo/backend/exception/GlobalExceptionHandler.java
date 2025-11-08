
package com.chambainfo.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Maneja la excepción cuando un usuario ya existe en el sistema.
     *
     * @param ex La excepción UserAlreadyExistsException.
     * @return Una respuesta HTTP con código 409 (CONFLICT) y el mensaje de error.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.error("Usuario ya existe: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Maneja la excepción cuando hay un error al consultar RENIEC.
     *
     * @param ex La excepción ReniecException.
     * @return Una respuesta HTTP con código 400 (BAD_REQUEST) y el mensaje de error.
     */
    @ExceptionHandler(ReniecException.class)
    public ResponseEntity<ErrorResponse> handleReniecException(ReniecException ex) {
        log.error("Error en RENIEC: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja la excepción cuando las credenciales de autenticación son incorrectas.
     *
     * @param ex La excepción BadCredentialsException.
     * @return Una respuesta HTTP con código 401 (UNAUTHORIZED) y el mensaje de error.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("Credenciales incorrectas: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Maneja las excepciones de validación de argumentos del método.
     *
     * @param ex La excepción MethodArgumentNotValidException.
     * @return Una respuesta HTTP con código 400 (BAD_REQUEST) y un mapa con los errores de validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Errores de validación: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    /**
     * Maneja la excepción cuando se pasa un argumento ilegal a un método.
     *
     * @param ex La excepción IllegalArgumentException.
     * @return Una respuesta HTTP con código 400 (BAD_REQUEST) y el mensaje de error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Argumento ilegal: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja cualquier excepción genérica no capturada por otros manejadores.
     *
     * @param ex La excepción genérica.
     * @return Una respuesta HTTP con código 500 (INTERNAL_SERVER_ERROR) y un mensaje de error genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor. Por favor intente nuevamente.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    // Clase interna para la respuesta de error
    public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
    ) {}
}