package main.model;

import main.InformationsEquipement;
import main.socket.SocketServer;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class EquipmentList {

    private List<Equipment> equipmentList;

    private boolean serverForInsertionExists;

    private boolean serverForSynchronisationExists;

    private Equipment serverInsertionEquipment;

    public ServerSocket insertionSocketServer;

    private Equipment serverSynchronisationEquipment;

    public ServerSocket synchronisationSocketServer;

    private List<InformationsEquipement> informationsEquipementsList;

    public EquipmentList() {
        this.equipmentList = new ArrayList<>();
        this.serverForInsertionExists = false;
        this.serverInsertionEquipment = null;
        this.informationsEquipementsList = new ArrayList<>();
    }

    //Getters
    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public boolean hasServerForInsertion() {
        return serverForInsertionExists;
    }

    public boolean hasServerForynchronisation() {
        return serverForSynchronisationExists;
    }

    public Equipment getServerInsertionEquipment() {
        return serverInsertionEquipment;
    }

    public Equipment getServerSynchronisationEquipment() {
        return serverSynchronisationEquipment;
    }

    //Setters
    public void setServerInsertionEquipment(Equipment serverInsertionEquipment) {
        this.serverInsertionEquipment = serverInsertionEquipment;
    }

    public void setServerSynchronisationEquipment(Equipment serverSynchronisationEquipment) {
        this.serverSynchronisationEquipment = serverSynchronisationEquipment;
    }

    public void setServerForInsertionExists(boolean serverForInsertionExists) {
        this.serverForInsertionExists = serverForInsertionExists;
    }

    public void setServerForSynchronisationExists(boolean serverForSynchronisationExists) {
        this.serverForSynchronisationExists = serverForSynchronisationExists;
    }

    public void addEquipement(Equipment equipment) {
        equipmentList.add(equipment);
    }

    public void addInformationsEquipement(InformationsEquipement informationsEquipement) {
        informationsEquipementsList.add(informationsEquipement);
    }

    public void removeEquipmentByName(String name) {
        equipmentList.stream()
                .filter(x -> x.getName().equals(name))
                .findFirst()
                .ifPresent(equipment -> equipmentList.remove(equipment));
    }
}
