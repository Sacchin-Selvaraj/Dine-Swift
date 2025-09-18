package com.dineswift.userservice.security.filter;

import com.dineswift.userservice.security.service.CustomUserDetailsService;
import com.dineswift.userservice.security.utilities.JWTUtilities;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTUtilities jwtUtilities;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtFilter(JWTUtilities jwtUtilities, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtilities = jwtUtilities;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

       String bearerToken=request.getHeader("Authorization");
       String token=null;
       String email=null;
       if (bearerToken!=null && bearerToken.startsWith("Bearer ")){
           token=bearerToken.substring(7);
           email=jwtUtilities.extractUsername(token);
       }
        UserDetails userDetails=null;
       if (email!=null){
           userDetails=customUserDetailsService.loadUserByUsername(email);
       }

       if (userDetails!=null && SecurityContextHolder.getContext().getAuthentication()==null){

            if (jwtUtilities.validateToken(userDetails,email,token)){
                UsernamePasswordAuthenticationToken authtoken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authtoken);
            }
        }
       filterChain.doFilter(request,response);
    }
}
