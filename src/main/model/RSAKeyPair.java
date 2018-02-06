package main.model;

import java.security.*;

public class RSAKeyPair {

    private final KeyPair key;

    public RSAKeyPair() {
        this.key = generateKeyPair();
    }

    private KeyPair generateKeyPair() {

        SecureRandom rand = new SecureRandom();
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512, rand);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return kpg.generateKeyPair();
    }

    //Getters
    public PublicKey Public() {
        return this.key.getPublic();
    }

    public PrivateKey Private() {
        return this.key.getPrivate();
    }

}
