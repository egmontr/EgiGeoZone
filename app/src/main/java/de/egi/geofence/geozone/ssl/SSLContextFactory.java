/*
* Copyright 2014 - 2015 Egmont R. (egmontr@gmail.com)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/		

package de.egi.geofence.geozone.ssl;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * A factory for SSLContexts.
 * Builds an SSLContext with custom KeyStore and TrustStore, to work with a client cert signed by a self-signed CA cert.
 */
public class SSLContextFactory {

    private static SSLContextFactory theInstance = null;

    private SSLContextFactory() {
    }

    public static SSLContextFactory getInstance() {
        if(theInstance == null) {
            theInstance = new SSLContextFactory();
        }
        return theInstance;
    }

    /**
     * Creates an SSLContext with the client and server certificates
     * @param clientCertFile A File containing the client certificate
     * @param clientCertPassword Password for the client certificate
     * @param caCertString A String containing the server certificate
     * @return An initialized SSLContext
     */
    public SSLContext makeContext(File clientCertFile, String clientCertPassword, String caCertString) throws Exception {
        final KeyStore keyStore = loadPKCS12KeyStore(clientCertFile, clientCertPassword == null ? "" : clientCertPassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        if (keyStore == null){
        	kmf.init(null, null);
        }else{
        	kmf.init(keyStore, clientCertPassword == null ? "".toCharArray() : clientCertPassword.toCharArray());
        }
        KeyManager[] keyManagers = kmf.getKeyManagers();

        // Trustmanager
        TrustManager[] trustManagers;
        final KeyStore trustStore = loadPEMTrustStore(caCertString);
        if (trustStore == null){
        	trustManagers = new TrustManager[] {new CustomFakeTrustManager()};
        }else{
        	trustManagers = new TrustManager[] {new CustomTrustManager(trustStore)};
        }
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);

        return sslContext;
    }

    /**
     * Produces a KeyStore from a String containing a PEM certificate (typically, the server's CA certificate)
     * @param certificateString A String containing the PEM-encoded certificate
     * @return a KeyStore (to be used as a trust store) that contains the certificate
     */
    private KeyStore loadPEMTrustStore(String certificateString) throws Exception {

    	if (certificateString == null) return null;
    	
        byte[] der = loadPemCertificate(new ByteArrayInputStream(certificateString.getBytes()));
        ByteArrayInputStream derInputStream = new ByteArrayInputStream(der);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);

        return trustStore;
    }

    /**
     * Produces a KeyStore from a PKCS12 (.p12) certificate file, typically the client certificate
     * @param certificateFile A file containing the client certificate
     * @param clientCertPassword Password for the certificate
     * @return A KeyStore containing the certificate from the certificateFile
     */
    private KeyStore loadPKCS12KeyStore(File certificateFile, String clientCertPassword) throws Exception {

    	if (certificateFile == null) return null;
    	
    	KeyStore keyStore;
        FileInputStream fis = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            fis = new FileInputStream(certificateFile);
            keyStore.load(fis, clientCertPassword.toCharArray());
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch(IOException ex) {
                // ignore
            }
        }
        return keyStore;
    }

    /**
     * Reads and decodes a base-64 encoded DER certificate (a .pem certificate), typically the server's CA cert.
     * @param certificateStream an InputStream from which to read the cert
     * @return a byte[] containing the decoded certificate
     */
    private byte[] loadPemCertificate(InputStream certificateStream) throws IOException {

        byte[] der;
        BufferedReader br = null;

        try {
            StringBuilder buf = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(certificateStream));

            String line = br.readLine();
            while(line != null) {
                if(!line.startsWith("--")){
                    buf.append(line);
                }
                line = br.readLine();
            }

            String pem = buf.toString();
            der = Base64.decode(pem, Base64.DEFAULT);

        } finally {
           if(br != null) {
               br.close();
           }
        }

        return der;
    }
}
