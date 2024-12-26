package com.fisherprinting.invoicecommissionservice.userlogin.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Configuration
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    static Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private JWTService jwtService;
    private MyUserDetailService myUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        final String authorizationHeader = request.getHeader("authorization");

        if(authorizationHeader != null) {
            System.out.println("TOKEN FOUND!");
            System.out.println(authorizationHeader);
        }else{
            System.out.println("TOKEN NOT FOUND!");
        }

        // Check if the Authorization key is available and if the authorization starts
        // with the string, 'Bearer'
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            try {
                String username = jwtService.extractUsernameFromToken(jwt);
                if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = myUserDetailService.loadUserByUsername(username);
                    if(userDetails != null && jwtService.validateToken(jwt)){
                        // After validating the user and that the JWT has not yet expired,
                        // let's generate an UsernamePasswordAuthenticationToken.
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, userDetails.getPassword(), userDetails.getAuthorities()
                        );

                        // Tracks who's logged-in in the system
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // With the token, we mark the context with the authentication token.
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            } catch (Exception e) {
                logger.error("doFilterInternal: "+e);
            }
        }

        // If proper conditions are not met . . .
        filterChain.doFilter(request, response);
    }

}
