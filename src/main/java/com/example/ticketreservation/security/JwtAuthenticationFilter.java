package com.example.ticketreservation.security; 
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 

public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtService jwt;
    private final CustomUserDetailsService users;
    public JwtAuthenticationFilter(
        JwtService j,
        CustomUserDetailsService u
    )
{
    jwt=j;
    users=u;
}

protected void doFilterInternal(
    HttpServletRequest r,
    HttpServletResponse s,
    FilterChain chain
)

throws ServletException,
    IOException{
        String h=r.getHeader("Authorization");
        if(h!=null&&h.startsWith("Bearer ")){
            String token=h.substring(7);
            try{String username=jwt.extractUsername(token);
                if(SecurityContextHolder.getContext().getAuthentication()==null){
                    UserDetails ud=users.loadUserByUsername(username);
                    if(jwt.valid(token)){
                        var auth=new UsernamePasswordAuthenticationToken(ud,null,ud.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(r));SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
            
    catch(Exception ignored){
        SecurityContextHolder.clearContext();
    }
}chain.doFilter(r,s);
}

}
