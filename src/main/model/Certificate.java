package main.model;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class Certificate {

    static private BigInteger seqnum = BigInteger.ZERO;
    public X509Certificate x509;

    //Constructeurs
    public Certificate(String issuerName, String subjectName, PrivateKey privKey, byte[] pubKey, int validityDays) {
            this.x509 = generateAndSignCertificate(issuerName, subjectName, privKey, pubKey, validityDays);
    }

    public Certificate(String pemCertificat) throws CertificateException, IOException {
        this.x509 = this.convertToX509Cert(pemCertificat);
    }

    //Vérification de la signature du certificat
    public boolean verifCertif(byte[] pubKey) {
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pubKey);
        ContentVerifierProvider verifier = null;
        try {
            verifier = new JcaContentVerifierProviderBuilder().setProvider("BC").build(subPubKeyInfo);
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }

        return verifySignature(verifier);
    }

    private boolean verifySignature(ContentVerifierProvider verifier) {
        try {
            X509CertificateHolder holder = new X509CertificateHolder(x509.getEncoded());
            if (!holder.isSignatureValid(verifier)) {
                System.err.println("Signature invalid");
                return false;
            }
        } catch (IOException | CertificateEncodingException | CertException e) {
            e.printStackTrace();
        }
        return true;
    }

    //Generation et signature du certificat
    private X509Certificate generateAndSignCertificate(String issuerName, String subjectName, PrivateKey privKey, byte[] pubKey, int validityDays) {

        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pubKey);
        X509CertificateHolder x509Holder = buildCertificateHolder(issuerName, subjectName, privKey, subPubKeyInfo, validityDays);
        return getCertificateFromHolder(x509Holder);
    }

    private X509CertificateHolder buildCertificateHolder(String issuerName, String subjectName, PrivateKey privKey, SubjectPublicKeyInfo subPubKeyInfo, int validityDays) {
        ContentSigner sigGen = null;
        try {
            sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privKey);
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }

        X500Name issuer = new X500Name("CN=" + issuerName);
        X500Name subject = new X500Name("CN=" + subjectName);
        seqnum = seqnum.add(BigInteger.ONE);
        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + validityDays * 24 * 60 * 60 * 1000);

        X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
                issuer,
                seqnum,
                startDate,
                endDate,
                subject,
                subPubKeyInfo
        );

        return v1CertGen.build(sigGen);
    }

    public void printCertificateInformations() {
        System.out.println("Certificate informations : ");
        System.out.println("Serial Number : " + this.x509.getSerialNumber());
        System.out.println("Issuer du certificat : " + this.x509.getIssuerDN());
        System.out.println("Début de validité du certificat : " + this.x509.getNotBefore());
        System.out.println("Fin de validité du certificat : " + this.x509.getNotAfter());
        System.out.println("Subject du certificat : " + this.x509.getSubjectDN());
        System.out.println("PublicKey : " + this.x509.getPublicKey());
        System.out.println("Signature algorithm : " + this.x509.getSigAlgName());
        byte[] sig = this.x509.getSignature();
        System.out.println("Signature : " + new BigInteger(sig).toString(16) + "\n");

    }

    public String getCertificateAsPemString() {

        final OutputStream output = new ByteArrayOutputStream();
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            encoder.encodeBuffer(x509.getEncoded(), output);
        } catch (CertificateEncodingException | IOException e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    private X509Certificate convertToX509Cert(String certificateString) {
        X509CertificateHolder certificateHolder = null;
        BASE64Decoder decoder = new BASE64Decoder();
        if (certificateString != null && !certificateString.trim().isEmpty()) {
            certificateString = certificateString.replace("-----BEGIN CERTIFICATE-----\n", "")
                    .replace("-----END CERTIFICATE-----", ""); // NEED FOR PEM FORMAT CERT STRING
            try {
                byte[] certificateData = decoder.decodeBuffer(certificateString);
                certificateHolder = new X509CertificateHolder(certificateData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getCertificateFromHolder(certificateHolder);
    }

    private X509Certificate getCertificateFromHolder(X509CertificateHolder holder) {
        X509Certificate certificate = null;
        try {
            certificate = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(holder);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return certificate;
    }
}