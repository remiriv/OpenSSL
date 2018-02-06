package main;

import main.model.Equipment;
import main.model.EquipmentList;
import main.threads.InsertionThreads;
import main.threads.SynchronisationThreads;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

public class InformationsEquipement extends Thread {

    private final Equipment equipment;
    private final EquipmentList equipmentList;
    private final Scanner scanner;
    private final InsertionThreads insertion;
    private final SynchronisationThreads synchronisation;
    private boolean wannaquit;

    InformationsEquipement(EquipmentList equipmentList, Equipment equipment, Scanner scanner) {

        this.equipmentList = equipmentList;
        this.equipment = equipment;
        this.scanner = scanner;
        this.insertion = new InsertionThreads(equipment, equipmentList, scanner);
        this.synchronisation = new SynchronisationThreads(equipment, equipmentList);
        this.wannaquit = false;
        try {
            createInterface(equipment);
        } catch (OperatorCreationException | CertException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createInterface(Equipment equipment) throws CertException, OperatorCreationException, IOException {
        while (!wannaquit) {
            equipmentManagementMenu(equipment);
        }
    }

    private synchronized String nextLine() {
        return this.scanner.nextLine();
    }

    private void equipmentManagementMenu(Equipment equipment) throws IOException {
        printEquipmentMenu(equipment);

        System.out.print("Entrez votre choix : (Character): ");
        String input = this.nextLine();
        //System.out.println(input + " \n");

        switch (input) {
            case "i":
                equipment.affichage();
                this.retourMenu();
                break;
            case "ca":
                equipment.affichage_ca();
                this.retourMenu();
                break;
            case "da":
                equipment.affichage_da();
                this.retourMenu();
                break;
            case "is":
                if (this.equipmentList.hasServerForInsertion()) {
                    System.out.println("Un serveur pour l'insertion existe déjà");
                    this.retourMenu();
                } else if (this.equipmentList.hasServerForynchronisation()) {
                    System.out.println("Un serveur pour la synchronisation existe.");
                    this.retourMenu();
                } else {
                    insertion.initialiseInsertionAsServer();
                }
                break;
            case "ic":
                if (this.equipmentList.hasServerForInsertion() && !this.equipmentList.hasServerForynchronisation()) {
                    insertion.initialiseInsertionAsClient();
                } else {
                    System.out.println("Il n'y a aucun serveur auquel se connecter pour l'insertion.");
                }
                break;
            case "ss":
                if (this.equipmentList.hasServerForynchronisation()) {
                    System.out.println("Un serveur pour la synchronisation existe déjà.");
                    this.retourMenu();
                } else if (this.equipmentList.hasServerForInsertion()) {
                    System.out.println("Un serveur pour l'insertion existe.");
                    this.retourMenu();
                } else {
                    synchronisation.initialiseSynchronisationAsServer();
                }
                break;
            case "sc":
                if (!this.equipmentList.hasServerForInsertion() && this.equipmentList.hasServerForynchronisation()) {
                    synchronisation.initialiseSynchronisationAsClient();
                } else {
                    System.out.println("Il n'y a aucun serveur auquel se connecter pour la synchronisation.");
                }
                break;
            case "cis":
                this.equipmentList.setServerForInsertionExists(false);
                try {
                    this.equipmentList.insertionSocketServer.close();
                } catch (SocketException e) {
                    break;
                }
                break;
            case "css":
                this.equipmentList.setServerForSynchronisationExists(false);
                try {
                    this.equipmentList.synchronisationSocketServer.close();
                } catch (SocketException e) {
                    break;
                }
                break;
            case "q":
                this.wannaquit = true;
                break;
            default:
                System.out.println("La commande que vous demandez n'est pas disponible. Veuillez entrer une commande valide.");
                break;
        }
    }

    private void printEquipmentMenu(Equipment equipment) {

        System.out.println("Equipment : " + equipment.getName());
        System.out.println("i   => Informations concernant l'équipement");
        System.out.println("ca  => Liste des équipements de CA");
        System.out.println("da  => Liste des équipements de DA");
        System.out.println("is  => Initialisation de l'insertion (en tant que serveur)");
        System.out.println("ic  => Initialisation de l'insertion (en tant que client)");
        System.out.println("ss  => Initialisation de la synchronisation (en tant que serveur)");
        System.out.println("sc  => Initialisation de la synchronisation (en tant que client)");
        System.out.println("cis => Fermeture de la socket serveur pour l'insertion.");
        System.out.println("css => Fermeture de la socket serveur pour la synchronisation.");
        System.out.println("q   => Quitter \n");
    }

    private void retourMenu() {
        System.out.println("Entrez 'q' pour revenir à la liste des équipements, ou 'Entrée' pour revenir au menu");
        String input = this.nextLine();
        if (input.equals("q")) {
            this.wannaquit = true;
        }
    }

}
