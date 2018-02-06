package main.socket;

import main.model.Equipment;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer extends ISocket {

    private int port;
    private ServerSocket serverSocket = null;
    private Equipment equipment;
    private List<String> equipmentNames;
    private List<SocketClientServer> clientSockets;

    public SocketServer(Equipment equipment, int port) {

        this.equipment = equipment;
        this.port = port;
        this.equipmentNames = new ArrayList<>();
        this.clientSockets = new ArrayList<>();
    }

    @Override
    public void run() {

    }

    public void createSocket() {
        try {
            System.out.println("server creation");
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Attente de connections
    public boolean waitForConnections() {
        try {
            System.out.println("Waiting for clients to connect...");
            this.clientSockets.add(new SocketClientServer(serverSocket.accept()));
            System.out.println("Client connected successfully ! ");
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    // Arret du serveur
    public void stopServer() {
        try {
            for (SocketClientServer clientSocket : this.clientSockets) {
                clientSocket.closeFlux();
                clientSocket.closeSocket();
            }
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Getters
    protected ServerSocket getServerSocket() {
        return serverSocket;
    }

    protected Equipment getEquipment() {
        return equipment;
    }

    protected List<String> getEquipmentNames() {
        return equipmentNames;
    }

    protected List<SocketClientServer> getClientSockets() {
        return clientSockets;
    }

    //Setters
    protected void addEquipmentName(String equipmentName) {
        this.equipmentNames.add(equipmentName);
    }
}