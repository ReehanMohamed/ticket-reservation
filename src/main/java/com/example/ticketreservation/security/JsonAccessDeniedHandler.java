package com.example.ticketreservation.security; 
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException; 

@Component 
public class JsonAccessDeniedHandler implements AccessDeniedHandler{
    private final ErrorResponseWriter writer;
    public JsonAccessDeniedHandler(ErrorResponseWriter w){
        writer=w;
    }
    
public void handle(
    HttpServletRequest r,
    HttpServletResponse s,
    AccessDeniedException e
)

throws IOException,ServletException{
    writer.write(r,s,HttpStatus.FORBIDDEN,"You do not have permission to access this resource");
}

}
