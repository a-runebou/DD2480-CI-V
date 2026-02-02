package com.ci.statuses;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.InsufficientResourcesException;

public class StatusPoster {
    private String repo;
    private String owner;
    private String token;
    
    public StatusPoster() throws InsufficientResourcesException {
        loadToken();
        System.out.println(repo + owner + token);
        if (repo == null || owner == null || token == null) {
            throw new InsufficientResourcesException("Missing values for either repo name, owner name, or token name");
        }
    }

    /**
     * Loads the information needed for HTTP requests.
     * @return true if the information were loaded, false otherwise
     */
    private boolean loadToken() {
        Properties prop = new Properties();
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("token.config");
            if (input == null) {
                System.out.println("Unable to find the token configuration file!");
                return false;
            }
            prop.load(input);
            this.token = prop.getProperty("token");
            this.owner = prop.getProperty("owner");
            this.repo = prop.getProperty("repo");
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
