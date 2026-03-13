package com.xdcoder.authApp.services.Impl;

import com.xdcoder.authApp.config.AppConstants;
import com.xdcoder.authApp.dtos.UserDto;
import com.xdcoder.authApp.entities.Role;
import com.xdcoder.authApp.repositories.RoleRepository;
import com.xdcoder.authApp.services.AuthService;
import com.xdcoder.authApp.services.UserService;
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
