package com.xdcoder.authApp.auth.payload;

public record LoginRequest(
        String email,
        String password

) {
}
