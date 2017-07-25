package com.cg.stateless.security;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class TokenAuthenticationService {

    private static final String Auth_Token = "token";

    private final TokenHandler tokenHandler;

    public TokenAuthenticationService(UserService userService) {
        tokenHandler = new TokenHandler(userService);
    }

    public String addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
        final User user = authentication.getDetails();
        String token = tokenHandler.createTokenForUser(user);
        //response.addHeader(Auth_Token, token);
        return token;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
         String token = request.getParameter(Auth_Token);
         if(token == null){
        	 token = request.getHeader(Auth_Token);
         }
        if (token != null) {
            final com.cg.user.bo.User user = tokenHandler.parseUserFromToken(token);
            if(user.getPassword() == null){
            	user.setPassword("demo");
            }
            User user2 = new User(user.getUsername(), user.getPassword(), new ArrayList<GrantedAuthority>());
            if (user2 != null) {
                return new UserAuthentication(user2,user);
            }
        }
       
        return null;
    }
}
