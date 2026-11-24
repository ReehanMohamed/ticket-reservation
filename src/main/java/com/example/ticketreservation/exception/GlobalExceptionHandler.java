package com.example.ticketreservation.exception; 
import com.example.ticketreservation.dto.ErrorResponse; 
import jakarta.servlet.http.HttpServletRequest; 
import org.springframework.http.*; 
import org.springframework.web.bind.annotation.*; 
import org.springframework.web.bind.MethodArgumentNotValidException; 
import java.time.LocalDateTime;

@RestControllerAdvice 
public class GlobalExceptionHandler { 
    private ResponseEntity<ErrorResponse> 
    make(HttpStatus s,String m,HttpServletRequest r){
        return ResponseEntity.status(s).body(
            new ErrorResponse(LocalDateTime.now(),s.value(),s.getReasonPhrase(),m,r.getRequestURI())
        );
    } 
    
    @ExceptionHandler(ApiException.class) 
    public ResponseEntity<ErrorResponse> 
    api(ApiException e,HttpServletRequest r){
        return make(e.getStatus(),e.getMessage(),r);
    } 
    
    @ExceptionHandler(MethodArgumentNotValidException.class) 
    
    public ResponseEntity<ErrorResponse> 
    validation(MethodArgumentNotValidException e,HttpServletRequest r){
        String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Validation failed");
        
        return make(HttpStatus.BAD_REQUEST,m,r);
    } 
    
    @ExceptionHandler(Exception.class) 
    
    public ResponseEntity<ErrorResponse> 
    
    other(Exception e,HttpServletRequest r){
        return make(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error",r);
    
    } 
}
