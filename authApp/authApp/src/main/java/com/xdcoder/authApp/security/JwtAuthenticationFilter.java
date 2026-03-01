package com.xdcoder.authApp.security;

import com.xdcoder.authApp.helpers.UserHelper;
import com.xdcoder.authApp.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        logger.info("Authorization header: {}", header);


        if(header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            // Here you can add logic to validate the token and set authentication in the security context
            try{

                Jws<Claims> parse = jwtService.parse(token);



                Claims payLoad = parse.getPayload();

                //check for access token
                if(!jwtService.isAccessToken(token)){
                    //message pass karna hai
                    filterChain.doFilter(request, response);
                    return;
                }

                String userId = payLoad.getSubject();
                UUID userUuiD = UserHelper.parseUUID(userId);
                userRepository.findById(userUuiD)

                        .ifPresent(user ->{
                            //check for user enable or not
                            if(user.isEnable()){


                                //user mil chuka hai database se, ab hume uske roles nikalne hai aur authentication object create karna hai
                                List<GrantedAuthority> authorities = user.getRoles() == null ? List.of(): user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authorities
                                );

                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                //final line: set the authentication in the security context
                                if(SecurityContextHolder.getContext().getAuthentication() == null)
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                            }





            });


            }catch(ExpiredJwtException e){
                request.setAttribute("error", "Token Expired");
                //e.printStackTrace();

            } catch (Exception e){
                request.setAttribute("error", "Invalid Token");
                //e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }

    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        return request.getRequestURI().startsWith("/api/v1/auth/login") || request.getRequestURI().startsWith("/api/v1/auth/register");
    }

}
