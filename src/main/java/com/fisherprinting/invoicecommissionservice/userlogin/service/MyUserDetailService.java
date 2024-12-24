package com.fisherprinting.invoicecommissionservice.userlogin.service;

import com.fisherprinting.invoicecommissionservice.userlogin.dao.UserRepository;
import com.fisherprinting.invoicecommissionservice.userlogin.model.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class MyUserDetailService implements UserDetailsService {
    public final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userFound = userRepository.findByUserName(username);
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(userFound.getUsername())
                .password(userFound.getPassword())
                .build();
    }
}
