package com.cyberark.common;
import java.io.*;

import javax.net.ssl.*;

import com.cyberark.common.exceptions.*;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class HttpClient {

    private final static Boolean DEBUG=false;

    // ==========================================
    // void disableSSL()
    //   from: https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
    //
    public static void disableSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(KeyManagementException e) {
            e.printStackTrace();
        }

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    } // disableSSL

    private static ByteArrayInputStream getInputStreamFromString(String input) throws IOException {
        return new ByteArrayInputStream(input.getBytes());
    }

    public static SSLSocketFactory getSSLSocketFactory(String certificateContent) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException  {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(getInputStreamFromString(certificateContent));

            final KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null);
            ks.setCertificateEntry("conjurTlsCaPath", cert);
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext conjurSSLContext = SSLContext.getInstance("TLS");
            conjurSSLContext.init(null, tmf.getTrustManagers(), null);

            return conjurSSLContext.getSocketFactory();
    }


    public static HttpResponse get(String urlString, String authHeader, String certificateContent) {
        return request(urlString, "GET", authHeader, null, certificateContent);
    }

    public static HttpResponse post(String urlString, String authHeader, String body, String certificateContent) {
        return request(urlString, "POST", authHeader, body, certificateContent);
    }

    public static HttpResponse patch(String urlString, String authHeader, String body, String certificateContent) {
        return request(urlString, "PATCH", authHeader, body, certificateContent);
    }

    public static HttpResponse put(String urlString, String authHeader, String body, String certificateContent) {
        return request(urlString, "PUT", authHeader, body, certificateContent);
    }

    public static HttpResponse request(String urlString, String method, String authHeader, String body, String certificateContent) {
        String output = "";
        int statusCode = 0;
        try {
            // Create http connection with Authorization header and correct Content-Type
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            if (certificateContent != null) {
                conn.setSSLSocketFactory(getSSLSocketFactory(certificateContent));
            }

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", authHeader);

            // java doesn't allow some http verbs so you have to make the method POST and then create an override property
            if(!method.equals("GET") && !method.equals("POST") && !method.equals("DELETE") && !method.equals("PUT")) {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", method);
            } else {
                conn.setRequestMethod(method);
            }

            // Do not write body to request if body is empty or null or method is GET
            if(body != null && !method.equals("GET") && !body.equals("")) {
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes());
                os.flush();
            }

            statusCode = conn.getResponseCode();

            if (statusCode > 299) {
                // TODO, maybe throw an exception here with an status code larger than 299
                System.out.println("Failed : HTTP error code : " + statusCode);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String tmp;
            while ((tmp = br.readLine()) != null) {
                output = output + tmp;
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        if(HttpClient.DEBUG) {
            System.out.println("JavaREST.request() ========");
            System.out.println("Response:");
            System.out.println(output);
            System.out.println(statusCode);
            System.out.println("============================");
        }

        return new HttpResponse(output, statusCode);
    }



    // ===============================================================
    // String base64Encode() - base64 encodes argument and returns encoded string
    //
    public static String base64Encode(String input) {
        String encodedString = "";
        try {
            encodedString = Base64.getEncoder().encodeToString(input.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedString;
    } // base64Encode

}