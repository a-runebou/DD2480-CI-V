package com.ci;

import java.io.IOException;

/**
 * The App class serves as the entry point for the CI server application.
 * It initializes and starts the HTTP server to listen for incoming requests.
 */
public class App {
    /**
     * The main method initializes the Server instance and starts it on port 2485.
     * @param args
     */
    public static void main( String[] args ) {
        Server server = new Server();
        try {
            server.start(2485);
        } catch (IOException ex) {
            System.getLogger(App.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        System.out.println( "Hello World!" );
    }
}
