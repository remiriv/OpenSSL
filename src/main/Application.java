package main;

import main.model.*;
import main.socket.SocketClient;
import main.socket.SocketServer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        Scanner scanner = new Scanner(System.in);

        //1ere séance : Création d'un équipement, vérification que le certificat est auto signé, et certification clé publique
        // generationEtSignatureDeCertificats();


        //2ème séance : Ecran affichant les infos, et CA et DA et permettant de rajouter un équipement
        final EquipmentList equipmentList = new EquipmentList();

        final Equipment equipmentServeur = new Equipment("Frigo", "localhost", 8081);
        final Equipment equipmentClient = new Equipment("Télévision", "localhost", 8082);
        final Equipment equipmentClient2 = new Equipment("Four", "localhost", 8083);
        final Equipment equipmentClient3 = new Equipment("Radio", "localhost", 8084);

        equipmentList.addEquipement(equipmentClient);
        equipmentList.addEquipement(equipmentClient2);
        equipmentList.addEquipement(equipmentClient3);
        equipmentList.addEquipement(equipmentServeur);

//        echangeDeNomsClientServeur(equipmentServeur, equipmentClient);

        int equipementNumber = 0;
        while (equipementNumber != 999) {
            for (int i = 0; i < equipmentList.getEquipmentList().size(); i++) {
                System.out.println(i + 1 + " - " + equipmentList.getEquipmentList().get(i).getName());
            }
            System.out.println(equipmentList.getEquipmentList().size() + 1 + " - Ajouter un équipement " );
            System.out.println("500 - Supprimer un équipement ");
            System.out.println("1000 - Quitter l'application");

            System.out.println("Avec quel équipement voulez-vous interagir ?");
            try {
                equipementNumber = Integer.valueOf(scanner.next()) - 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            if (equipementNumber < equipmentList.getEquipmentList().size()) {
                new Thread(new InformationsEquipement(equipmentList, equipmentList.getEquipmentList().get(equipementNumber), scanner) {
                    @Override
                    public void run() {
                        equipmentList.addInformationsEquipement(this);
                    }
                }).start();
            }
            else if(equipementNumber == equipmentList.getEquipmentList().size()) {
                createNewEquipment(scanner, equipmentList);
            }

            else if (equipementNumber == 499) {
                removeEquipment(scanner, equipmentList);
            }
        }
        scanner.close();
    }

    private static void removeEquipment(Scanner scanner, EquipmentList equipmentList) {
        scanner.nextLine();
        System.out.println("Nom de l'équipement à supprimer : ");
        String name = scanner.nextLine();
        equipmentList.removeEquipmentByName(name);
    }

    private static void createNewEquipment(Scanner scanner, EquipmentList equipmentList) throws Exception {
        scanner.nextLine();
        System.out.println("Nom de l'équipement : ");
        String name = scanner.nextLine();
        System.out.println("Hôte sur lequel se connectera la socket associée à l'équipement : ");
        String host = scanner.nextLine();
        System.out.println("Port associé à l'équipement : ");
        int port=0;
        try {
            port = Integer.valueOf(scanner.nextLine());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        equipmentList.addEquipement(new Equipment(name, host, port));
    }

    private static void generationEtSignatureDeCertificats() throws Exception {
        final Equipment equipment1 = new Equipment("monEquipement", "localhost", 45);
        final boolean checkVerification = equipment1.getCertificate().verifCertif(equipment1.getPublicKey().getEncoded());
        System.out.println(checkVerification);

        RSAKeyPair pk1 = new RSAKeyPair();
        RSAKeyPair pk2 = new RSAKeyPair();

        Certificate certif2 = new Certificate(
                "issuer",
                "subject",
                pk2.Private(),
                pk1.Public().getEncoded(),
                45
        );
        final boolean certifyKey1 = certif2.verifCertif(pk2.Public().getEncoded());

        System.out.println(certifyKey1);
        certif2.printCertificateInformations();
    }

    private static void echangeDeNomsClientServeur(Equipment equipmentServeur, Equipment equipmentClient) {
        final Thread serverThread = new Thread(new SocketServer(equipmentServeur, equipmentServeur.getPort()) {
            @Override
            public void run() {
                this.createSocket();
                this.waitForConnections();
                final String clientName = this.getEquipmentNames().get(this.getEquipmentNames().size() - 1);
                final byte[] publicKey = (byte[]) this.getClientSockets().get(this.getClientSockets().size() - 1).receiveObject();

                this.getClientSockets().get(this.getClientSockets().size() - 1).sendMessage(new Certificate(equipmentServeur.getName(), clientName, equipmentServeur.getPrivateKey(), publicKey, 50).getCertificateAsPemString());
                this.getClientSockets().get(this.getClientSockets().size() - 1).sendObject(equipmentServeur.publicKeyByte());
            }
        });
        serverThread.start();

        final Thread clientThread = new Thread(new SocketClient(
                equipmentClient,
                equipmentServeur.getName(),
                equipmentServeur.getHost(),
                equipmentServeur.getPort()
        ) {
            @Override
            public void run() {
                this.createSocket();
                this.sendMessage(this.getEquipment().getName());
                this.sendObject(this.getEquipment().publicKeyByte());
                final Certificate certifServer = this.receiveCertificate();
                final byte[] publicKey = (byte[]) this.receiveObject();

                if (certifServer.verifCertif(publicKey)) {
                    System.out.println("Le certificat est valide");

                    final CertificationAutority certificationAutority = new CertificationAutority(
                            this.getServerName(),
                            equipmentServeur.publicKeyByte(),
                            new ArrayList<>(Arrays.asList(certifServer)),
                            null
                    );
                    if(!equipmentClient.getListeCA().contains(certificationAutority)) {
                        equipmentClient.addElementToCA(certificationAutority);
                    }
                    System.out.println("Affichage CA : ");
                    equipmentClient.affichage_ca();


                } else {
                    System.out.println("Echec");
                }
            }
        });
        clientThread.start();
    }
}
