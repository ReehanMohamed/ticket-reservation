package com.example.ticketreservation.security; 
import com.fasterxml.jackson.databind.ObjectMapper; 
import jakarta.servlet.http.*; 
import org.springframework.http.HttpStatus; 
import org.springframework.stereotype.Component; 
import java.time.LocalDateTime; 
import com.example.ticketreservation.dto.ErrorResponse; 

@Component 

public class ErrorResponseWriter{
    private final ObjectMapper mapper;
    public ErrorResponseWriter(ObjectMapper m){
        mapper=m;
    } 
    
public void write(
    HttpServletRequest req,
    HttpServletResponse res,
    HttpStatus s,
    String msg
)

throws java.io.IOException{
    res.setStatus(s.value());
    res.setContentType("application/json");
    mapper.writeValue(
        res.getOutputStream(),
        
        new ErrorResponse(
            LocalDateTime.now(),
            s.value(),
            s.getReasonPhrase(),
            msg,
            req.getRequestURI())
        )
    ;}

}
