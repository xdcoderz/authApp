package com.xdcoder.authApp.controllers;

import com.xdcoder.authApp.dtos.UserDto;
import com.xdcoder.authApp.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;


    //create user API
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }
    //get all user API
    @GetMapping
    public ResponseEntity<Iterable<UserDto>>getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //get user by email API
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    //delete user API
    // api/v1/users/{userId}
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") String userId){
        userService.deleteUser(userId);
    }

    //update user API
    // api/v1/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.updateUser(userDto, userId));
    }

    //get user by id API
    // api/v1/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

}
