package main.model;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Equipment {

    private final Certificate certificate;       // Le certificat auto-signe.
    private final String host;            // Identite de l’equipment.
    private final String name;            // Identite de l’equipment.
    private final int port;              // Le numéro de port d’ecoute.
    private final RSAKeyPair rsaKeyPair;       // La paire de cle de l’equipment.

    List<CertificationAutority> listeCA;
    List<CertificationAutority> listeDA;


    public Equipment(String name, String host, int port) throws Exception {
        this.host = host;
        this.name = name;
        this.port = port;
        this.rsaKeyPair = new RSAKeyPair();
        this.certificate = new Certificate(name, name, this.rsaKeyPair.Private(), this.rsaKeyPair.Public().getEncoded(), 50);
        this.listeCA = new ArrayList<>();
        this.listeDA = new ArrayList<>();

        this.certificate.printCertificateInformations();
    }

    public boolean checkIfEquipmentInCA(String equipmentName) {
        for (CertificationAutority certificationAutority : listeCA) {
            if(certificationAutority.getId().equals(equipmentName)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfEquipmentInDA(String equipmentName) {
        for (CertificationAutority certificationAutority : listeDA) {
            if(certificationAutority.getId().equals(equipmentName)) {
                return true;
            }
        }
        return false;
    }

    public void affichage_da() {
        System.out.println(" Liste de DA pour l'équipement : " + this.name);
        for (CertificationAutority certificationAutority : listeDA) {
            printAutoriteCertification(certificationAutority);
        }
    }

    public void affichage_ca() {
        System.out.println(" Liste de CA pour l'équipement : " + this.name);
        for (CertificationAutority certificationAutority : listeCA) {
            printAutoriteCertification(certificationAutority);
        }
    }

    private void printAutoriteCertification(CertificationAutority certificationAutority) {
        System.out.println("ID : " + certificationAutority.getId());
        System.out.println("Certificats : ");
        certificationAutority.getCertificates().forEach(Certificate::printCertificateInformations);
    }

    public void affichage() {
        System.out.println("Nom : " + this.name);
        System.out.println("Port : " + this.port);
        System.out.println("Cle RSA: " + this.rsaKeyPair.Public() + "\n");

        this.certificate.printCertificateInformations();
    }

    //Getters
    public String getHost() {
        return this.host;
    }

    public PublicKey getPublicKey() {
        return this.rsaKeyPair.Public();
    }

    public PrivateKey getPrivateKey() {
        return this.rsaKeyPair.Private();
    }

    public byte[] publicKeyByte() {
        return this.getPublicKey().getEncoded();
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public int getPort() {
        return this.port;
    }

    public String getName() {
        return this.name;
    }
    public List<CertificationAutority> getListeCA() {
        return listeCA;
    }

    public List<CertificationAutority> getListeDA() {
        return listeDA;
    }

    public CertificationAutority getCertificationAutorityById(String id, List<CertificationAutority> liste) {
        for (CertificationAutority certificationAutority : liste) {
            if(certificationAutority.getId().equals(id)) {
                return certificationAutority;
            }
        }
        return null;
    }

    //Setters
    public void addElementToCA(CertificationAutority certificationAutority) {
        listeCA.add(certificationAutority);
    }

    public void addElementToDA(CertificationAutority certificationAutority) {
        listeDA.add(certificationAutority);
    }
}
