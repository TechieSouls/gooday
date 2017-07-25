package com.cg.stateless.security;

//import com.technicalrex.springsecurityjwt.support.validation.StringConditions;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

import org.springframework.security.core.userdetails.User;

public final class TokenHandler {

    //private final String secret;
    //@Autowired
    private  UserService userService;

    public TokenHandler(UserService userService) {
        //this.secret = secret;//StringConditions.checkNotBlank(secret);
        this.userService = userService ;//(UserRepository) Preconditions.checkNotNull(userRepository);
    }

    public com.cg.user.bo.User parseUserFromToken(String token) {
    	try{
    	String secret = token.substring(0,13);
    	token = token.substring(13);
    	System.out.println(secret+" = "+token);
        String username = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        
        return userService.findUserByUsername(username);
    	}catch(Exception e){
    		e.printStackTrace();return null;
    	}
    }

    public String createTokenForUser(User user) {
    	System.out.println(new Date() +"   "+(new Date(new Date().getTime()+(1000*60*60*24))));
    	String secret = new Date(new Date().getTime()+(1000*60*60*24)).getTime()+"";
        return secret+Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
