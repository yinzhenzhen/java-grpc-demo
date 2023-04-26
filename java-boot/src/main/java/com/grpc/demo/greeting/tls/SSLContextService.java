package com.grpc.demo.greeting.tls;

import com.tencent.kona.crypto.CryptoInsts;
import com.tencent.kona.pkix.PKIXInsts;
import com.tencent.kona.ssl.SSLInsts;
import com.tencent.kona.sun.security.x509.SMCertificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class SSLContextService {

    private volatile SSLContext sslContext;

    private KeyManagerFactory kmf;

    private TrustManagerFactory tmf;
    private KeyStore trustStore;
    private SSLContextService() {
    }

    private static final SSLContextService SINGLETON = new SSLContextService();

    public static SSLContextService getInstance() {
        return SINGLETON;
    }

    public SSLContext initSslContext(List<String> caCerts, String signCert, String signKey, String encCert, String encKey) throws Exception {
        if (sslContext == null) {
            synchronized (SSLContext.class) {
                if (sslContext == null) {
                    sslContext = createContext(caCerts, signCert, null, signKey, encCert, null, encKey);
                }
            }
        }
        return sslContext;
    }

    private SSLContext createContext(List<String> caList, String signEeStr, String signEeId, String signEeKeyStr,
        String encEeStr, String encEeId, String encEeKeyStr) throws Exception {
        trustStore = createTrustStore(caList, null);
        tmf = SSLInsts.getTrustManagerFactory("PKIX");
        tmf.init(trustStore);

        KeyStore keyStore = createKeyStore(signEeStr, signEeId, signEeKeyStr, encEeStr, encEeId, encEeKeyStr);
        kmf = SSLInsts.getKeyManagerFactory("NewSunX509");
        kmf.init(keyStore, null);

        sslContext = SSLInsts.getSSLContext("TLCPv1.1");
//        sslContext = SSLInsts.getSSLContext("TLS"); // when http ecc
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private KeyStore createTrustStore(List<String> caList, String caId) throws Exception {
        trustStore = PKIXInsts.getKeyStore("PKCS12");
        trustStore.load(null, null);
        for (String ca : caList) {
            X509Certificate cert = loadCert(ca, caId);
            trustStore.setCertificateEntry("test", cert);
        }
        return trustStore;
    }

    private KeyStore createKeyStore(String signEeStr, String signEeId, String signEeKeyStr, String encEeStr,
        String encEeId, String encEeKeyStr) throws Exception {
        KeyStore keyStore = PKIXInsts.getKeyStore("PKCS12");
        keyStore.load(null, null);

        keyStore.setKeyEntry("tlcp-sign-ee-demo",
            loadPrivateKey(signEeKeyStr), null, new Certificate[] {loadCert(signEeStr, signEeId)});
        keyStore.setKeyEntry("tlcp-enc-ee-demo",
            loadPrivateKey(encEeKeyStr), null, new Certificate[] {loadCert(encEeStr, encEeId)});

        return keyStore;
    }

    private X509Certificate loadCert(String certPEM, String id) throws Exception {
        CertificateFactory certFactory = PKIXInsts.getCertificateFactory("X.509");
        X509Certificate x509Cert =
            (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(certPEM.getBytes()));

        if (id != null && !id.isEmpty()) {
            ((SMCertificate)x509Cert).setId(id.getBytes(StandardCharsets.UTF_8));
        }

        return x509Cert;
    }

    private PrivateKey loadPrivateKey(String keyPEM) throws Exception {
        if (keyPEM.startsWith("-----BEGIN PRIVATE KEY-----")) {
            keyPEM = keyPEM.substring(28, keyPEM.length() - 26);
        }
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(keyPEM));
        KeyFactory keyFactory = CryptoInsts.getKeyFactory("EC");
        return keyFactory.generatePrivate(privateKeySpec);
    }

}
