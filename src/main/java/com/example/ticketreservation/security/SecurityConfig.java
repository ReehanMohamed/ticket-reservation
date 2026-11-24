package com.example.ticketreservation.security; 
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration 
@EnableMethodSecurity 
public class SecurityConfig{
    private final JwtAuthenticationFilter filter;
    private final JsonAuthenticationEntryPoint entry;
    private final JsonAccessDeniedHandler denied;
    public SecurityConfig(
        JwtAuthenticationFilter f,
        JsonAuthenticationEntryPoint e,
        JsonAccessDeniedHandler d
    ){
        filter=f;entry=e;denied=d;
    }
    
@Bean SecurityFilterChain chain(HttpSecurity http)throws Exception{
    return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(e->e.authenticationEntryPoint(entry).accessDeniedHandler(denied)).authorizeHttpRequests(a->a.requestMatchers("/api/auth/register","/api/auth/login").permitAll().anyRequest().authenticated()).addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();
}

@Bean PasswordEncoder encoder(){
    return new BCryptPasswordEncoder();
}

@Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{
    return c.getAuthenticationManager();
}

}
