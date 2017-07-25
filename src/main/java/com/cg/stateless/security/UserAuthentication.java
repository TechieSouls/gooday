package com.cg.stateless.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserAuthentication implements Authentication {

    private final User user;
    private boolean authenticated = true;
    private com.cg.user.bo.User loggedUser ;

    public UserAuthentication(User user,com.cg.user.bo.User loggedInUser) {
        this.user = user;
        this.loggedUser = loggedInUser;
    }

    
    public String getName() {
        return user.getUsername();
    }

    
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    
    public Object getCredentials() {
        return user.getPassword();
    }

    
    public User getDetails() {
        return user;
    }

    
    public Object getPrincipal() {
        return loggedUser;
    }

    
    public boolean isAuthenticated() {
        return authenticated;
    }

    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public com.cg.user.bo.User getLoginUser(){
    	return loggedUser;
    }
}
