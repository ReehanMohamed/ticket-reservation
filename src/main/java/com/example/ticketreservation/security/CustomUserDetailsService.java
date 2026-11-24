package com.example.ticketreservation.security; 
import com.example.ticketreservation.repository.UserRepository; 
import org.springframework.security.core.userdetails.*; 
import org.springframework.stereotype.Service; 

@Service 

public class CustomUserDetailsService implements UserDetailsService{ 
    private final UserRepository repo; 
    public CustomUserDetailsService(UserRepository r){repo=r;} 
    public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException{
        var u=repo.findByUsername(username).orElseThrow(
            ()->new UsernameNotFoundException("User not found")
        );
        return User.withUsername(u.getUsername()).password(u.getPassword()).roles(u.getRole().name()).build();
    } 

}
