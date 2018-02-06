package main.socket;

import main.model.Certificate;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;

public abstract class ISocket implements Runnable {


    Socket socket = null;
    private InputStream NativeIn = null;
    private ObjectInputStream ois = null;
    private OutputStream NativeOut = null;
    private ObjectOutputStream oos = null;

    // Creation des flux natifs et evolues
    public void createNativeFlux() {
        try {
            this.NativeOut = this.socket.getOutputStream();
            this.oos = new ObjectOutputStream(this.NativeOut);
            this.NativeIn = this.socket.getInputStream();
            this.ois = new ObjectInputStream(this.NativeIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Emission d’un String
    public void sendMessage(String message) {
        try {
            this.oos.writeObject(message);
            this.oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reception d’un String
    public String receiveMessage() {
        try {
            String res = (String) this.ois.readObject();
            //System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Emission d’un Objet
    public void sendObject(Object object) {
        try {
            this.oos.writeObject(object);
            this.oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reception d’un Objet
    public Object receiveObject() {
        try {
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }


    // Fermeture des flux evolues et natifs
    public void closeFlux() {
        try {
            this.ois.close();
            this.oos.close();
            this.NativeIn.close();
            this.NativeOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fermeture de la connexion
    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Certificate receiveCertificate() {
        Certificate serverCertificate = null;
        try {
            serverCertificate = new Certificate(this.receiveMessage());
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
        }
        return serverCertificate;
    }

    public Socket getSocket() {
        return socket;
    }
}
