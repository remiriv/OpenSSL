package main.socket;

import main.model.Equipment;

import java.net.Socket;

public class SocketClient extends ISocket {

    private String serverHost;
    private int serverPort;
    private Equipment equipment;
    private String serverName;

    public SocketClient(Equipment equipment, String serverName, String serverHost, int serverPort) {
        this.equipment = equipment;
        this.serverName = serverName;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {

    }

    public void createSocket() {
        // Creation de socket (TCP)
        try {
            this.socket = new Socket(this.serverHost, this.serverPort);
            this.createNativeFlux();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Started client");

    }

    //Getter
    public String getServerName() {
        return serverName;
    }

    public Equipment getEquipment() {
        return equipment;
    }
}