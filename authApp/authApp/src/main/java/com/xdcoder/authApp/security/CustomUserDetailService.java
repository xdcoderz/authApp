package com.xdcoder.authApp.security;

import com.xdcoder.authApp.entities.User;
import com.xdcoder.authApp.exceptions.ResourceNotFoundException;
import com.xdcoder.authApp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));


        return user;
    }

}
