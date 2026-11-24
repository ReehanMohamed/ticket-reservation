package com.example.ticketreservation.service; 
import com.example.ticketreservation.dto.*;
import com.example.ticketreservation.model.*;
import com.example.ticketreservation.repository.*;
import com.example.ticketreservation.security.JwtService;
import com.example.ticketreservation.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.*;
import java.util.List;

@Service 
public class AuthService{
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    private final JwtService jwt;
    public AuthService(
        UserRepository r,
        PasswordEncoder e,
        AuthenticationManager m,
        JwtService j
    ){
        repo=r;
        encoder=e;
        manager=m;
        jwt=j;
    }
    
public AuthResponse register(RegisterRequest q){
    if(repo.existsByUsername(q.username()))
        throw new ApiException(HttpStatus.CONFLICT,"Username already exists");
    
    AppUser u=repo.save(AppUser.builder().username(q.username()).password(encoder.encode(q.password())).fullName(q.fullName()).role(Role.CUSTOMER).build());
    return auth(u);
}

public AuthResponse login(LoginRequest q){
    try{manager.authenticate(new UsernamePasswordAuthenticationToken(q.username(),q.password()));

    }
    catch(Exception e){
        throw new ApiException(HttpStatus.UNAUTHORIZED,"Invalid username or password");
    }
    return auth(repo.findByUsername(q.username()).orElseThrow());

}

private AuthResponse auth(AppUser u){
    return new AuthResponse(jwt.generate(u.getUsername()),"Bearer",jwt.getExpiration(),response(u));
}

public UserResponse me(String username){
    return response(repo.findByUsername(username).orElseThrow());
}

public List<UserResponse> users(){
    return repo.findAll().stream().map(this::response).toList();
}

public UserResponse response(AppUser u){
    return new UserResponse(u.getId(),u.getUsername(),u.getFullName(),u.getRole());
}

}
