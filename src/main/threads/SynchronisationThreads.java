package main.threads;

import main.model.Certificate;
import main.model.CertificationAutority;
import main.model.Equipment;
import main.model.EquipmentList;
import main.socket.ISocket;
import main.socket.SocketClient;
import main.socket.SocketClientServer;
import main.socket.SocketServer;

import java.io.IOException;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SynchronisationThreads extends Thread {

    private final Equipment equipment;
    private final EquipmentList equipmentList;

    public SynchronisationThreads(Equipment equipment, EquipmentList equipmentList) {
        this.equipment = equipment;
        this.equipmentList = equipmentList;
    }

    public void initialiseSynchronisationAsServer() {
        final Thread serverThread = new Thread(new SocketServer(equipment, equipment.getPort()) {
            @Override
            public void run() {
                this.createSocket();
                equipmentList.synchronisationSocketServer = this.getServerSocket();
                do {
                    this.waitForConnections();
                    if (!this.getServerSocket().isClosed()) {
                        SocketClientServer socket = this.getClientSockets().get(this.getClientSockets().size() - 1);
                        final String clientName = socket.receiveMessage();

                        final List<CertificationAutority> listeCAClient = buildCertificationAutoritiesFromCertificatesAsPem((List<CertificationAutority>) socket.receiveObject());
                        final List<CertificationAutority> listeDAClient = buildCertificationAutoritiesFromCertificatesAsPem((List<CertificationAutority>) socket.receiveObject());

                        final List<CertificationAutority> listeAutoriteClient = buildListWithCAAndDA(listeCAClient, listeDAClient);

                        final List<CertificationAutority> listeAutoriteServeur = buildListWithCAAndDA(this.getEquipment().getListeCA(), this.getEquipment().getListeDA());

                        if (this.getEquipment().checkIfEquipmentInCA(clientName)) {
                            List<CertificationAutority> listOfDifferences = getListOfDifferencesInCAAndDA(listeAutoriteServeur, listeAutoriteClient);

                            final CertificationAutority clientCertification = this.getEquipment().getCertificationAutorityById(clientName, this.getEquipment().getListeCA());
                            listOfDifferences.forEach(autorite -> {
                                autorite.addCertificate(clientCertification.getCertificates().get(0));
                                if (!this.getEquipment().getListeDA().contains(autorite)) {
                                    this.getEquipment().addElementToDA(autorite);
                                }
                            });

                            listOfDifferences.forEach(autorite -> this.getEquipment().addElementToDA(autorite));

                        } else if (this.getEquipment().checkIfEquipmentInDA(clientName)) {
                            final CertificationAutority clientCertificationAutority = equipment
                                    .getCertificationAutorityById(
                                            clientName,
                                            equipment.getListeDA()
                                    );
                            socket.sendObject(getCertificationAutoritiesWithCertificatesAsPem(new ArrayList<>(Arrays.asList(clientCertificationAutority))).get(0));
                            final byte[] publicKey = (byte[]) socket.receiveObject();
                            socket.sendObject(this.getEquipment().publicKeyByte());
                            socket.sendMessage(new Certificate(
                                    this.getEquipment().getName(),
                                    clientName,
                                    this.getEquipment().getPrivateKey(),
                                    publicKey,
                                    50
                            ).getCertificateAsPemString());
                            final Certificate certifClient = socket.receiveCertificate();

                            verifyCertificateAndAddToCA(publicKey, certifClient, clientName, new ArrayList<>(Arrays.asList(certifClient)), "client", "server");

                        }

                        setSocketReadTimeout(socket);

                        final CertificationAutority receivedAutority = (CertificationAutority)socket.receiveObject();

                        if (receivedAutority != null) {
                            final CertificationAutority clientCertification = buildCertificationAutoritiesFromCertificatesAsPem(Arrays.asList(receivedAutority)).get(0);
                            socket.sendObject(this.getEquipment().publicKeyByte());
                            final byte[] publicKey = (byte[]) socket.receiveObject();
                            socket.sendMessage(new Certificate(
                                    this.getEquipment().getName(),
                                    clientName,
                                    this.getEquipment().getPrivateKey(),
                                    publicKey,
                                    50
                            ).getCertificateAsPemString());
                            final Certificate certifClient = socket.receiveCertificate();
                            verifyCertificateAndAddToCA(publicKey, certifClient, clientName, new ArrayList<>(Arrays.asList(certifClient)), "client", "server");
                        }
                    }
                } while (equipmentList.hasServerForynchronisation());
                this.stopServer();
            }
        });
        serverThread.start();
        this.equipmentList.setServerForSynchronisationExists(true);
        this.equipmentList.setServerSynchronisationEquipment(equipment);

    }

    public void initialiseSynchronisationAsClient() {
        final Thread clientThread = new Thread(new SocketClient(
                this.equipment,
                this.equipmentList.getServerSynchronisationEquipment().getName(),
                this.equipmentList.getServerSynchronisationEquipment().getHost(),
                this.equipmentList.getServerSynchronisationEquipment().getPort()) {
            @Override
            public void run() {
                this.createSocket();
                this.sendMessage(this.getEquipment().getName());

                final List<CertificationAutority> certificatesInCAAsPem = getCertificationAutoritiesWithCertificatesAsPem(this.getEquipment().getListeCA());
                final List<CertificationAutority> certificatesInDAAsPem = getCertificationAutoritiesWithCertificatesAsPem(this.getEquipment().getListeDA());

                this.sendObject(certificatesInCAAsPem);
                this.sendObject(certificatesInDAAsPem);

                final Equipment synchronisationServer = equipmentList.getServerSynchronisationEquipment();

                final List<CertificationAutority> listeAutoriteClient = buildListWithCAAndDA(this.getEquipment().getListeCA(), this.getEquipment().getListeDA());

                final List<CertificationAutority> listeAutoriteServeur = buildListWithCAAndDA(synchronisationServer.getListeCA(), synchronisationServer.getListeDA());

                if (this.getEquipment().checkIfEquipmentInCA(synchronisationServer.getName())) {
                    List<CertificationAutority> listOfDifferences = getListOfDifferencesInCAAndDA(listeAutoriteClient, listeAutoriteServeur);

                    final CertificationAutority serverCertification = this.getEquipment().getCertificationAutorityById(getServerName(), this.getEquipment().getListeCA());
                    listOfDifferences.forEach(autorite -> {
                        autorite.addCertificate(serverCertification.getCertificates().get(0));
                        if (!equipment.getListeDA().contains(autorite)) {
                            equipment.addElementToDA(autorite);
                        }

                    });

                } else if (this.getEquipment().checkIfEquipmentInDA(synchronisationServer.getName())) {
                    final CertificationAutority serverCertificationAutority = equipment
                            .getCertificationAutorityById(
                                    synchronisationServer.getName(),
                                    equipment.getListeDA()
                            );
                    this.sendObject(getCertificationAutoritiesWithCertificatesAsPem(new ArrayList<>(Arrays.asList(serverCertificationAutority))).get(0));
                    this.sendObject(this.getEquipment().publicKeyByte());
                    final byte[] publicKey = (byte[]) this.receiveObject();
                    this.sendMessage(new Certificate(
                            this.getEquipment().getName(),
                            this.getServerName(),
                            this.getEquipment().getPrivateKey(),
                            publicKey,
                            50
                    ).getCertificateAsPemString());
                    final Certificate certifServer = this.receiveCertificate();

                    verifyCertificateAndAddToCA(publicKey, certifServer, synchronisationServer.getName(), new ArrayList<>(Arrays.asList(certifServer)), "server", "client");
                }

                setSocketReadTimeout(this);

                final CertificationAutority receivedAutority = (CertificationAutority)this.receiveObject();

                if (receivedAutority != null) {
                    final CertificationAutority serverCertification = buildCertificationAutoritiesFromCertificatesAsPem(Arrays.asList(receivedAutority)).get(0);
                    this.sendObject(this.getEquipment().publicKeyByte());
                    final byte[] publicKey = (byte[]) this.receiveObject();
                    this.sendMessage(new Certificate(
                            this.getEquipment().getName(),
                            synchronisationServer.getName(),
                            this.getEquipment().getPrivateKey(),
                            publicKey,
                            50
                    ).getCertificateAsPemString());
                    final Certificate certifServer = this.receiveCertificate();
                    verifyCertificateAndAddToCA(publicKey, certifServer, synchronisationServer.getName(), new ArrayList<>(Arrays.asList(certifServer)), "server", "client");
                }
                this.closeFlux();
                this.closeSocket();
            }
        });
        clientThread.start();
    }

    private List<CertificationAutority> getCertificationAutoritiesWithCertificatesAsPem(List<CertificationAutority> autorities) {
        return autorities.stream()
                .map(autority ->
                        new CertificationAutority(
                                autority.getId(),
                                autority.getPublicKey(),
                                null,
                                autority.getCertificates()
                                        .stream()
                                        .map(Certificate::getCertificateAsPemString)
                                        .collect(Collectors.toList())
                        )
                )
                .collect(Collectors.toList());
    }

    private List<CertificationAutority> buildCertificationAutoritiesFromCertificatesAsPem(List<CertificationAutority> autorities) {
        return autorities.stream()
                .map(autority ->
                        new CertificationAutority(
                                autority.getId(),
                                autority.getPublicKey(),
                                autority.getCertificatesAsPem()
                                        .stream()
                                        .map(certificateAsPem -> {
                                            Certificate certificate = null;
                                            try {
                                                certificate = new Certificate(certificateAsPem);
                                            } catch (CertificateException | IOException e) {
                                                e.printStackTrace();
                                            }
                                            return certificate;
                                        })
                                        .collect(Collectors.toList()),
                                null)
                )
                .collect(Collectors.toList());
    }

    private void verifyCertificateAndAddToCA(byte[] publicKey, Certificate certif, String name, ArrayList<Certificate> certificates, String issuer, String subject) {
        if (certif.verifCertif(publicKey)) {
            System.out.println("Le certificat du " + issuer + " est valide");
            final CertificationAutority certificationAutority = new CertificationAutority(
                    name,
                    publicKey,
                    certificates,
                    null);
            if (!equipment.getListeCA().contains(certificationAutority)) {
                equipment.addElementToCA(certificationAutority);

                System.out.println("Element ajouté au CA du " + subject + " : ");
                certificationAutority.printCertificationAutorityInformations();
            }


        } else {
            System.out.println("Certificat " + issuer + " invalide, équipement non ajouté");
        }
    }

    private void setSocketReadTimeout(ISocket socket) {
        try {
            socket.getSocket().setSoTimeout(2000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private List<CertificationAutority> buildListWithCAAndDA(List<CertificationAutority> listeCA, List<CertificationAutority> listeDA) {
        final List<CertificationAutority> listeAutoriteClient = new ArrayList<>();
        listeAutoriteClient.addAll(listeCA);
        listeAutoriteClient.addAll(listeDA);
        return listeAutoriteClient;
    }

    private List<CertificationAutority> getListOfDifferencesInCAAndDA(List<CertificationAutority> listeAutoriteClient, List<CertificationAutority> listeAutoriteServeur) {
        Set<String> idAutoriteServeur =
                listeAutoriteServeur.stream()
                        .map(CertificationAutority::getId)
                        .collect(Collectors.toSet());

        return listeAutoriteClient.stream()
                .filter(e -> !idAutoriteServeur.contains(e.getId()))
                .collect(Collectors.toList());
    }

}
