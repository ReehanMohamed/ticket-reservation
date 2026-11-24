package com.example.ticketreservation.security; 
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException; 

@Component 
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint{
    private final ErrorResponseWriter writer;
    public JsonAuthenticationEntryPoint(ErrorResponseWriter w){
        writer=w;
    }
    
public void commence(
    HttpServletRequest r,
    HttpServletResponse s,
    AuthenticationException e
)
throws IOException,
ServletException{
    writer.write(
        r,s,HttpStatus.UNAUTHORIZED,"Authentication required or token invalid");
    }
}
