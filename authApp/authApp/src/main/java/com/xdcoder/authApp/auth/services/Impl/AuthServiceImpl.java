package com.xdcoder.authApp.auth.services.Impl;

import com.xdcoder.authApp.auth.payload.UserDto;
import com.xdcoder.authApp.auth.services.AuthService;
import com.xdcoder.authApp.auth.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDto registerUser(UserDto userDto){

        //logic when registering user can be added here
        //verify email format, password strength, etc.
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        UserDto userDto1 = userService.createUser(userDto);

        return userDto1;
    }
}
