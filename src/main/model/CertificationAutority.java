package main.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CertificationAutority implements Serializable {

    private final String id;

    private final byte[] publicKey;

    private final List<Certificate> certificates;

    private final List<String> certificatesAsPem;

    public CertificationAutority(String id, byte[] publicKey, List<Certificate> certificates, List<String> certificatesAsPem) {
        this.id = id;
        this.publicKey = publicKey;
        this.certificates = certificates;
        this.certificatesAsPem = certificatesAsPem;
    }

    //Getter
    public String getId() {
        return id;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public List<String> getCertificatesAsPem() {
        return certificatesAsPem;
    }

    public void addCertificate(Certificate certificate) {
        certificates.add(certificate);
    }

    public void addCertificateAsPem(String certificate) {
        certificatesAsPem.add(certificate);
    }

    public void printCertificationAutorityInformations() {

        System.out.println("ID : " + this.getId());
        this.getCertificates().stream().forEach(Certificate::printCertificateInformations);
    }
}
