package com.grpc.demo.greeting.tls;

import com.tencent.kona.crypto.KonaCryptoProvider;
import com.tencent.kona.pkix.KonaPKIXProvider;
import com.tencent.kona.ssl.KonaSSLProvider;
import io.netty.handler.ssl.*;

import javax.net.ssl.SSLContext;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

public class TLSService {

    public JdkSslContext jdkSslContext;

    private static final String GM_CA = "-----BEGIN CERTIFICATE-----\n" + "MIICWzCCAgCgAwIBAgIUAO1n+b4zz4CXu61/0t015MIPjqgwDAYIKoEcz1UBg3UF\n" + "ADBVMQswCQYDVQQGEwJDTjERMA8GA1UECAwIQ2hlbmcgRFUxEDAOBgNVBAoMB0Rv\n" + "bWFpbkExDTALBgNVBAsMBG51bGwxEjAQBgNVBAMMCVNNMlJvb3RDYTAeFw0yMzAy\n" + "MDkwNjE0NDFaFw0yNDAyMDkwNjE0NDFaMFUxCzAJBgNVBAYTAkNOMREwDwYDVQQI\n" + "DAhDaGVuZyBEVTEQMA4GA1UECgwHRG9tYWluQTENMAsGA1UECwwEbnVsbDESMBAG\n" + "A1UEAwwJU00yUm9vdENhMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEc+0U1tDG\n" + "wwJ7mcwG7WH3UWUZbZqXazXBQMYXI341R+9RUx09ZY6e5kO8jQVff7mzQ4Wve2+A\n" + "YwC+l75xd8EawaOBqzCBqDAfBgNVHSMEGDAWgBQNK+P8Q2tzJ0OgTVPvZ/ndj0Ub\n" + "MTASBgNVHRMBAf8ECDAGAQH/AgEAMA4GA1UdDwEB/wQEAwIBhjBCBgNVHREEOzA5\n" + "gglsb2NhbGhvc3SHBH8AAAGHBMCoOoaHBH8AAAGCGnlpbnpoZW5kZU1hY0Jvb2st\n" + "UHJvLmxvY2FsMB0GA1UdDgQWBBQNK+P8Q2tzJ0OgTVPvZ/ndj0UbMTAMBggqgRzP\n" + "VQGDdQUAA0cAMEQCIFoGLRE4aTdqA6rYku0rN7PYnDCa9ocfDhodozAMF7RkAiBQ\n" + "iaGeSnXdAiIjSpKOkpBpRJpFDr9FKArR1f6CreJx/Q==\n" + "-----END CERTIFICATE-----";
    private static final String GM_CERT = "-----BEGIN CERTIFICATE-----\n" + "MIICczCCAhegAwIBAgITTjUG3hU3MEeAzFR7JMA+wh8UAjAMBggqgRzPVQGDdQUA\n" + "MFUxCzAJBgNVBAYTAkNOMREwDwYDVQQIDAhDaGVuZyBEVTEQMA4GA1UECgwHRG9t\n" + "YWluQTENMAsGA1UECwwEbnVsbDESMBAGA1UEAwwJU00yUm9vdENhMB4XDTIzMDIw\n" + "OTA2MTQ0MVoXDTI0MDEzMTE2MDAwMFowUTELMAkGA1UEBhMCQ04xETAPBgNVBAgM\n" + "CENoZW5nIERVMRAwDgYDVQQKDAdEb21haW5BMQ0wCwYDVQQLDARudWxsMQ4wDAYD\n" + "VQQDDAVub2RlYTBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABLg/fMkc7uFkNQym\n" + "pI3A/iZtE2Jb66/KcjHEpifAnYuaJOqhw4gSXillsGgD1FpoP2l5ogwgkAvETgk+\n" + "HVsCsSmjgccwgcQwHwYDVR0jBBgwFoAUDSvj/ENrcydDoE1T72f53Y9FGzEwDAYD\n" + "VR0TAQH/BAIwADAgBgNVHSUBAf8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDgYD\n" + "VR0PAQH/BAQDAgO4MEIGA1UdEQQ7MDmCCWxvY2FsaG9zdIcEfwAAAYcEwKg6hocE\n" + "fwAAAYIaeWluemhlbmRlTWFjQm9vay1Qcm8ubG9jYWwwHQYDVR0OBBYEFENy00Pv\n" + "EW1qJZRiX6ZoBzuKF1oqMAwGCCqBHM9VAYN1BQADSAAwRQIhAMooRn0ulYTMeoAE\n" + "ML7bs5LDz6Zzril/5zzNJ6B2zRUsAiAsD8fY0mqO2KzAWfwE4GlIYFgHXz2t4PXd\n" + "ftnRqlDe3g==\n" + "-----END CERTIFICATE-----";
    private static final String GM_KEY = "-----BEGIN PRIVATE KEY-----\n" + "ME0CAQAwEwYHKoZIzj0CAQYIKoEcz1UBgi0EMzAxAgEBBCCJOVbcPmgyaxj9QeiY\n" + "C+7jgwNzZTMi1zD7Lku0Dpfp8aAKBggqgRzPVQGCLQ==\n" + "-----END PRIVATE KEY-----";
    private static final String ECC_CA = "-----BEGIN CERTIFICATE-----\n" + "MIICQzCCAeqgAwIBAgIRAOTwS557pX0C4xj4+pou+NYwCgYIKoZIzj0EAwIwbDEL\n" + "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" + "cmFuY2lzY28xFDASBgNVBAoTC2V4YW1wbGUuY29tMRowGAYDVQQDExF0bHNjYS5l\n" + "eGFtcGxlLmNvbTAeFw0yMjAzMjEwNjAxMDBaFw0zMjAzMTgwNjAxMDBaMGwxCzAJ\n" + "BgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4gRnJh\n" + "bmNpc2NvMRQwEgYDVQQKEwtleGFtcGxlLmNvbTEaMBgGA1UEAxMRdGxzY2EuZXhh\n" + "bXBsZS5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQ5J3s3cYqno6dJKzrO\n" + "IQSq2sB0wWCGhvc+wwwtrGqr0B1hPZMAwpincGa4G8RsSXX0jdR/LgxYficXEnH7\n" + "KUido20wazAOBgNVHQ8BAf8EBAMCAaYwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsG\n" + "AQUFBwMBMA8GA1UdEwEB/wQFMAMBAf8wKQYDVR0OBCIEIDHe5mwqh4N9BtkOjJOP\n" + "Gz1utKEB+GKuzmHU5BYtEmA9MAoGCCqGSM49BAMCA0cAMEQCICFcvZIscpGeTsuS\n" + "BLaOhoFD2nSJsuuhlagbG04624nhAiATAeqGxTtWFN7yulh1k4hg/6RPZML+eXFJ\n" + "uJh3IM442w==\n" + "-----END CERTIFICATE-----\n";

    private static final String ECC_CERT = "-----BEGIN CERTIFICATE-----\n" + "MIICWzCCAgKgAwIBAgIQJ5Mym5AmWgaTifBvOlVg5jAKBggqhkjOPQQDAjBsMQsw\n" + "CQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\n" + "YW5jaXNjbzEUMBIGA1UEChMLZXhhbXBsZS5jb20xGjAYBgNVBAMTEXRsc2NhLmV4\n" + "YW1wbGUuY29tMB4XDTIyMDMyMTA2MDEwMFoXDTMyMDMxODA2MDEwMFowWTELMAkG\n" + "A1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBGcmFu\n" + "Y2lzY28xHTAbBgNVBAMTFG9yZGVyZXIwLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0C\n" + "AQYIKoZIzj0DAQcDQgAEB12INZGD4OGIcWZzUYiPofSkXyEsUGBru+Q0oLQBsagq\n" + "KeIOJeL5FYZ0VdBOVedaEm1OsURb4XgkWJQolJB0OqOBmDCBlTAOBgNVHQ8BAf8E\n" + "BAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwGA1UdEwEB/wQC\n" + "MAAwKwYDVR0jBCQwIoAgMd7mbCqHg30G2Q6Mk48bPW60oQH4Yq7OYdTkFi0SYD0w\n" + "KQYDVR0RBCIwIIIUb3JkZXJlcjAuZXhhbXBsZS5jb22CCG9yZGVyZXIwMAoGCCqG\n" + "SM49BAMCA0cAMEQCIGk4R50NbcTWTCEhvSa0E3pu3yuK/aexuyXqtBeuiT6LAiAr\n" + "HE+vqto6v0l9ne2JrQz/XX9CHQJRoK5duo2Cgh1p9A==\n" + "-----END CERTIFICATE-----\n";

    private static final String ECC_KEY = "-----BEGIN PRIVATE KEY-----\n" + "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgBxHqABfO0AZt4ewm\n" + "gzIL/IJb7BMSboQBD2Y4et/LhGahRANCAAQHXYg1kYPg4YhxZnNRiI+h9KRfISxQ\n" + "YGu75DSgtAGxqCop4g4l4vkVhnRV0E5V51oSbU6xRFvheCRYlCiUkHQ6\n" + "-----END PRIVATE KEY-----\n";


    static {
        Security.addProvider(new KonaCryptoProvider());
        Security.addProvider(new KonaPKIXProvider());
        Security.addProvider(new KonaSSLProvider());
//        Security.insertProviderAt(new KonaProvider(), 1);
    }

    public TLSService(boolean isClient, boolean isgm, List<String> tlsCaList) {
        setSslContext(isClient, isgm, tlsCaList);
    }

    private void setSslContext(boolean isClient, boolean isgm, List<String> tlsCaList) {
        ApplicationProtocolConfig alpn = new ApplicationProtocolConfig(
            ApplicationProtocolConfig.Protocol.ALPN,
            ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT,
            ApplicationProtocolConfig.SelectedListenerFailureBehavior.FATAL_ALERT,
            "h2", "HTTP/1.1");

        // load ca cert
        List<String> caCerts = new ArrayList<>();

        String caCert = "";
        String signCert = "";
        String signKey = "";
        String encCert = "";
        String encKey = "";

        if (isgm) {
            caCert = GM_CA;
            signCert = GM_CERT;
            signKey = GM_KEY;
            encCert = GM_CERT;
            encKey = GM_KEY;
        } else {
            caCert = ECC_CA;
            signCert = ECC_CERT;
            signKey = ECC_KEY;
            encCert = ECC_CERT;
            encKey = ECC_KEY;
        }
        caCerts.add(caCert);

        if (null != tlsCaList && !tlsCaList.isEmpty()) {
            caCerts.addAll(tlsCaList);
        }

        // get ssl context
        SSLContext sslContext;
        try {
            sslContext = SSLContextService.getInstance().initSslContext(caCerts, signCert, signKey, encCert, encKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (isgm) {
            jdkSslContext = new JdkSslContext(sslContext, isClient, List.of("TLCP_ECC_SM4_CBC_SM3", "TLCP_ECC_SM4_GCM_SM3"),
                new AllAllowedCipherSuiteFilter(), alpn, ClientAuth.REQUIRE, new String[] {"TLCPv1.1"}, false);
        } else {
            jdkSslContext = new JdkSslContext(sslContext, isClient, List.of("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA"),
                new AllAllowedCipherSuiteFilter(), alpn, ClientAuth.REQUIRE, new String[] {"TLSv1.2"}, false);
        }
    }

    private static class AllAllowedCipherSuiteFilter implements CipherSuiteFilter {
        @Override public String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers,
            Set<String> supportedCiphers) {
            return StreamSupport.stream(ciphers.spliterator(), false).toArray(String[]::new);
        }
    }

}
