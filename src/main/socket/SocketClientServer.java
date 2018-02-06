package main.socket;

import java.net.Socket;

public class SocketClientServer extends ISocket {

    public SocketClientServer(Socket socket) {
        this.socket = socket;
        System.out.println("Client connected, server attempting to create flux. ");
        this.createNativeFlux();
    }

    @Override
    public void run() {
    }

}
