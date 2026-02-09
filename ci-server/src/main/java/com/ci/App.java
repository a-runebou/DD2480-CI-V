package com.ci;

import java.io.IOException;

public class App {
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
