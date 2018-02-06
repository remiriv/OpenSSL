package main.threads;

import main.model.Certificate;
import main.model.CertificationAutority;
import main.model.Equipment;
import main.model.EquipmentList;
import main.socket.SocketClient;
import main.socket.SocketClientServer;
import main.socket.SocketServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class InsertionThreads extends Thread {

    private final Equipment equipment;
    private final EquipmentList equipmentList;
    private final Scanner scanner;

    public InsertionThreads(Equipment equipment, EquipmentList equipmentList, Scanner scanner) {
        this.equipment = equipment;
        this.equipmentList = equipmentList;
        this.scanner = scanner;
    }

    public void initialiseInsertionAsServer() {
        final Thread serverThread = new Thread(new SocketServer(equipment, equipment.getPort()) {
            @Override
            public void run() {
                this.createSocket();
                equipmentList.insertionSocketServer = this.getServerSocket();
                do {
                    this.waitForConnections();
                    if (!this.getServerSocket().isClosed()) {
                        SocketClientServer socket = this.getClientSockets().get(this.getClientSockets().size() - 1);
                        final String clientName = socket.receiveMessage();
                        //if (authorizeInsertion(clientName)) {
                        System.out.println("Autorisation donnée au serveur");

                        this.addEquipmentName(clientName);
                        final byte[] publicKey = (byte[]) socket.receiveObject();
                        socket.sendMessage(new Certificate(
                                this.getEquipment().getName(),
                                clientName,
                                this.getEquipment().getPrivateKey(),
                                publicKey,
                                50
                        ).getCertificateAsPemString());

                        socket.sendObject(this.getEquipment().publicKeyByte());
                        final Certificate certifClient = socket.receiveCertificate();

                        if (certifClient.verifCertif(publicKey)) {
                            System.out.println("Le certificat du client est valide");
                            final CertificationAutority certificationAutority = new CertificationAutority(
                                    clientName,
                                    publicKey,
                                    new ArrayList<>(Arrays.asList(certifClient)),
                                    null);
                            if (!equipment.getListeCA().contains(certificationAutority)) {
                                equipment.addElementToCA(certificationAutority);

                                System.out.println("Element ajouté au CA du serveur : ");
                                certificationAutority.printCertificationAutorityInformations();
                            }


                        } else {
                            System.out.println("Certificate client invalide, équipement non ajouté");
                        }
                        //					} else { //Envoi des messages génériques pour débloquer le client
                        //						socket.receiveObject();
                        //						socket.sendMessage("Faux certificat");
                        //						socket.sendObject("Fausse clé publique");
                        //						socket.receiveCertificate();
                        //					}
                    }
                } while (equipmentList.hasServerForInsertion());
                this.stopServer();
            }
        });
        serverThread.start();

        this.equipmentList.setServerForInsertionExists(true);
        this.equipmentList.setServerInsertionEquipment(equipment);
    }

    public void initialiseInsertionAsClient() {

        final Thread clientThread = new Thread(new SocketClient(
                this.equipment,
                this.equipmentList.getServerInsertionEquipment().getName(),
                this.equipmentList.getServerInsertionEquipment().getHost(),
                this.equipmentList.getServerInsertionEquipment().getPort()
        ) {
            @Override
            public void run() {
                this.createSocket();
                this.sendMessage(this.getEquipment().getName());
                this.sendObject(this.getEquipment().publicKeyByte());
                System.out.println("Client inséré");

                final Equipment insertionServer = equipmentList.getServerInsertionEquipment();

                //if (authorizeInsertion(equipmentList.getServerInsertionEquipment().getName())) {
                System.out.println("Autorisation donnée au client");
                Certificate certifServer = this.receiveCertificate();
                final byte[] publicKey = (byte[]) this.receiveObject();

                if (certifServer.verifCertif(publicKey)) {

                    System.out.println("Le certificat du serveur est valide");
                    final CertificationAutority certificationAutority = new CertificationAutority(
                            this.getServerName(),
                            publicKey,
                            new ArrayList<>(Arrays.asList(certifServer)),
                            null);

                    if (!equipment.getListeCA().contains(certificationAutority)) {
                        equipment.addElementToCA(certificationAutority);
                        System.out.println("Element ajouté au CA du client : " + this.getEquipment().getName());
                        certificationAutority.printCertificationAutorityInformations();
                    }

                    for (CertificationAutority autoriteCertifCA : insertionServer.getListeCA()) {
                        autoriteCertifCA.addCertificate(
                                equipment.getCertificationAutorityById(insertionServer.getName(), equipment.getListeCA())
                                        .getCertificates()
                                        .get(0));
                        if (!equipment.getListeDA().contains(autoriteCertifCA)) {
                            equipment.addElementToDA(autoriteCertifCA);

                            System.out.println("Element ajouté au DA du client : " + this.getEquipment().getName());
                            autoriteCertifCA.printCertificationAutorityInformations();
                        }
                    }
                    for (CertificationAutority autoriteCertifDA : insertionServer.getListeDA()) {
                        autoriteCertifDA.addCertificate(
                                equipment.getCertificationAutorityById(insertionServer.getName(), equipment.getListeCA())
                                        .getCertificates()
                                        .get(0));
                        if (!equipment.getListeDA().contains(autoriteCertifDA)) {
                            equipment.addElementToDA(autoriteCertifDA);

                            System.out.println("Element ajouté au DA du client : " + this.getEquipment().getName());
                            autoriteCertifDA.printCertificationAutorityInformations();
                        }
                    }
                    this.sendMessage(new Certificate(
                            this.getEquipment().getName(),
                            this.getServerName(),
                            this.getEquipment().getPrivateKey(),
                            publicKey,
                            50
                    ).getCertificateAsPemString());
                } else {
                    System.out.println("Certificate serveur invalide, équipement non ajouté");
                }
                //					} else {//Envoi des messages génériques pour débloquer le serveur
                //						this.receiveCertificate();
                //						this.receiveObject();
                //						this.sendMessage("Faux Certificate");
                //					}
                this.closeFlux();
                this.closeSocket();
            }
        });
        clientThread.start();
    }

    //TODO : Problème avec le scanner, c'est le scanner de la classe qui reprend la main sur celui du thread
    //TODO : donc ça renvoie une erreur et le thread ne s'arrête pas pour laisser l'utilisateur rentrer une valeur.
    private synchronized boolean authorizeInsertion(String nom) {
        scanner.nextLine();
        System.out.println("Insérer l'équipement " + nom + " (Oui/Non) ?");
        String authorization = this.scanner.nextLine();
        return authorization.equals("oui") || authorization.equals("o") || authorization.equals("Oui");
    }

}
