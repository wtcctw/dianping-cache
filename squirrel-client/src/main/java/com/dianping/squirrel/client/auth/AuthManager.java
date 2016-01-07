package com.dianping.squirrel.client.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthManager {

    private static Logger logger = LoggerFactory.getLogger(AuthManager.class);
    
    private static AuthManager instance = new AuthManager();
    
    private AuthProvider authProvider;
    
    private AuthManager() {
        try {
            this.authProvider = new DefaultAuthProvider();
        } catch (Exception e) {
            logger.error("failed to init auth provider", e);
        }
    }
    
    public static AuthManager getInstance() {
        return instance;
    }
    
    public boolean isStrict(String resource) throws AuthException {
        return authProvider == null ? false : authProvider.isStrict(resource);
    }
    
    public boolean authorize(String client, String resource) throws AuthException {
        return authProvider == null ? false : authProvider.authorize(client, resource);
    }
    
}
