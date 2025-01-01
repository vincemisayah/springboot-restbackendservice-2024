package com.fisherprinting.invoicecommissionservice.userlogin.controller;

import com.fisherprinting.invoicecommissionservice.userlogin.dtos.DTOs;
import com.fisherprinting.invoicecommissionservice.userlogin.service.JWTService;
import com.fisherprinting.invoicecommissionservice.userlogin.service.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import com.fisherprinting.invoicecommissionservice.userlogin.model.User;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
public class LoginController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailService myUserDetailService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody DTOs.LoginForm loginForm) {
        ResponseEntity<?> response = null;
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginForm.username(), loginForm.password()
            ));

            if(authentication.isAuthenticated()) {
                User user = myUserDetailService.getUserByUserName(loginForm.username());
                response = ResponseEntity.ok().body(Map.of("Fullname", user.getFullname(),
                        "UserID", user.getId(),
                        "GeneratedToken", jwtService.generateToken(myUserDetailService.loadUserByUsername(loginForm.username()))));
            }
        }catch (Exception e){
            response = ResponseEntity.notFound().build();
        }
        return response;
    }
}
