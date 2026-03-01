package com.xdcoder.authApp.services.Impl;

import com.xdcoder.authApp.dtos.UserDto;
import com.xdcoder.authApp.entities.Provider;
import com.xdcoder.authApp.entities.User;
import com.xdcoder.authApp.exceptions.ResourceNotFoundException;
import com.xdcoder.authApp.helpers.UserHelper;
import com.xdcoder.authApp.repositories.UserRepository;
import com.xdcoder.authApp.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
       if(userDto.getEmail() ==null || userDto.getEmail().isBlank()){
           throw new IllegalArgumentException("Email is required");
       }
       if(userRepository.existsByEmail(userDto.getEmail())){
           throw new IllegalArgumentException("Email already exists");
       }

       //Convert UserDto to User entity

       User user = modelMapper.map(userDto, User.class);
       user.setProvider(userDto.getProvider()!=null ? userDto.getProvider(): Provider.LOCAL);
       //using chatgpt I pushed this into this code
       user.setEnable(true);
       //role assign here to new user for authorization
        //TODO:
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);

    }

    @Override
    public UserDto getUserByEmail(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+email));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+userId));
        // Update fields
        //we are not going to change email id for this project
        if (userDto.getName() !=null) existingUser.setName(userDto.getName());
        //TODO: change the password updation logic...
        if (userDto.getPassword() !=null) existingUser.setPassword(userDto.getPassword());
        if (userDto.getImage() !=null) existingUser.setImage(userDto.getImage());
        if(userDto.getProvider()!=null) existingUser.setProvider(userDto.getProvider());
        existingUser.setEnable(userDto.getEnable());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);

        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+userId));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+userId));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
