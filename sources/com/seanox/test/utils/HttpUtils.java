/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Seanox Test Utilities
 *  Copyright (C) 2017 Seanox Software Solutionss
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of version 2 of the GNU General Public License as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.seanox.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *  Utilities for HTTP(S) connections.
 */
public class HttpUtils {
    
    /** Selection of header fields */
    public static class HeaderField {
        
        /** constant for Last-Modified */
        public static final String LAST_MODIFIED = "Last-Modified";

        /** constant for Content-Length */
        public static final String CONTENT_LENGTH = "Content-Length";

        /** constant for Content-Type */
        public static final String CONTENT_TYPE = "Content-Type";

        /** constant for WWW-Authenticate */
        public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    }
    
    static String[] getResponseHeader(byte[] response, String... fields) {
        return HttpUtils.getResponseHeader(new String(response), fields);
    }
    
    static String[] getResponseHeader(String response, String... fields) {

        String pattern = "";
        if (fields == null)
            fields = new String[0];
        for (String field : fields)
            pattern += "|\\Q" + field.trim() + "\\E";
        if (!pattern.isEmpty())
            pattern = pattern.substring(1);
        
        String[] headerLines = response.replaceAll("(?s)^[\r\n]+(.*?)\r\n\r\n.*$", "$1").split("[\r\n]+");
        LinkedList<String> headerList = new LinkedList<>();
        for (String header : headerLines) {
            if (!pattern.isEmpty()
                    && !header.matches("(?i)^(" + pattern + ").*$"))
                continue;
            headerList.add(header);
        }
        
        return headerList.toArray(new String[0]);
    }

    /**
     *  Returns the value of a header field.
     *  If the field does not exist, these methods return {@code null}.
     *  @param  response
     *  @param  field
     *  @return the value of a header field, otherwise {@code null}
     */
    public static String getResponseHeaderValue(String response, String field) {
        
        String[] fields = HttpUtils.getResponseHeader(response, field);
        if (fields.length <= 0)
            return null;
        return fields[0].replaceAll("^[^:]+:\\s*(.*)\\s*$", "$1");
    }
    
    /**
     *  Checks exists one or a set of response header fields.
     *  @param  response
     *  @param  fields
     *  @return {@code true} if all passed fields exists
     */
    public static boolean exitsResponseHeader(String response, String... fields) {
        
        if (fields == null)
            fields = new String[0];
        if (fields.length <= 0)
            return false;
        for (String field : fields)
            if (HttpUtils.getResponseHeader(response, field).length <= 0)
                return false;
        return true;
    }
    
    /** Interface to implements a keystore for SSL connections. */
    public interface Keystore {
        
        /**
         *  Returns the keystore file.
         *  @return the keystore file
         */
        File getFile();

        /**
         *  Returns the keystore password.
         *  @return the keystore password
         */
        String getPassword();
    }
    
    /**
     *  Creates a socket for an HTTP(S) connection.
     *  The decision whether HTTP or HTTPS is made by specifying a keystore.
     *  HTTPS requires a keystore.
     *  @param  address
     *  @param  keystore
     *  @return the create socket
     *  @throws IOException
     *  @throws GeneralSecurityException 
     */
    private static Socket createSocket(String address, Keystore keystore)
            throws IOException, GeneralSecurityException {

        if (keystore == null) {
            Socket socket = new Socket(address.replaceAll(Pattern.NETWORK_CONNECTION, "$1"),
                    Integer.valueOf(address.replaceAll(Pattern.NETWORK_CONNECTION, "$2")).intValue());
            socket.setSoTimeout(65535);
            socket.setSoLinger(true, 65535);
            return socket;
        }
        
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(keystore.getFile()), keystore.getPassword().toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, ("changeIt").toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); 
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS"); 
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers(); 
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null); 
        
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory(); 
        SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket(address.replaceAll(Pattern.NETWORK_CONNECTION, "$1"),
                Integer.valueOf(address.replaceAll(Pattern.NETWORK_CONNECTION, "$2")).intValue());
        sslSocket.startHandshake();
        sslSocket.setSoTimeout(65535);
        sslSocket.setSoLinger(true, 65535);
        
        return sslSocket;
    }

    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */    
    public static byte[] sendRequest(String address)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, null, (Keystore)null);
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  keystore
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, Keystore keystore)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, null, keystore);
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  request
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, (Keystore)null);
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  request
     *  @param  data
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, InputStream data)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, data, null);
    }    

    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  request
     *  @param  keystore
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Keystore keystore)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, (InputStream)null, keystore);
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  request
     *  @param  data
     *  @param  keystore
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, InputStream data, Keystore keystore)
            throws IOException, GeneralSecurityException {
        
        if (!address.matches(Pattern.NETWORK_CONNECTION))
            throw new IllegalArgumentException("Invalid connection string: " + address + ", expected <host>:<port>");
    
        try (Socket socket = HttpUtils.createSocket(address, keystore)) {
            if (request != null) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output);
                writer.print(request);
                writer.flush();
                while (data != null
                        && StreamUtils.forward(data, output) >= 0)
                    continue;
            }
            return StreamUtils.read(socket.getInputStream());   
        }
    }
    
    /** Interface to implements RequestEvent. */
    public interface RequestEvent {
        
        /**
         *  The methods are called when the server sends a response.
         *  @param respones
         */
        void onResponse(byte[] respones);

        /**
         *  The methods are called when an error occurs during the connection.
         *  @param exception
         */
        void onException(Exception exception);
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  The received response is handelt as {@link RequestEvent}.
     *  @param address
     *  @param request
     *  @param event
     *  @param keystore
     */
    public static void sendRequest(String address, String request, HttpUtils.RequestEvent event) {
        HttpUtils.sendRequest(address, request, event, null);
    }

    /**
     *  Sends a HTTP request to a server.
     *  The received response is handelt as {@link RequestEvent}.
     *  @param address
     *  @param request
     *  @param event
     *  @param keystore
     */
    public static void sendRequest(String address, String request, HttpUtils.RequestEvent event, Keystore keystore) {
        
        new Thread() {
            @Override
            public void run() {
                try {
                    byte[] response = sendRequest(address, request, keystore);
                    if (event != null)
                        event.onResponse(response);
                } catch (Exception exception) {
                    if (event != null)
                        event.onException(exception);
                }
            }
        }.start();
    }
    
    /** Abstract class to implements a authentication. */
    public abstract static class Authentication {
        
        private String user;
        
        private String password;
        
        private Authentication(String user, String password) {
            
            this.user     = user;
            this.password = password;
        }

        /** Basic Authentication */
        public static class Basic extends Authentication {
            
            /** 
             *  Constructor, creates a new Basic Authentication.
             *  @param user
             *  @param password
             */
            public Basic(String user, String password) {
                super(user, password);
            }
        }

        /** Digest Authentication */
        public static class Digest extends Authentication {
            
            /** 
             *  Constructor, creates a new Digest Authentication.
             *  @param user
             *  @param password
             */
            public Digest(String user, String password) {
                super(user, password);
            }
        }
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  request
     *  @param  authentication
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Authentication authentication)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, authentication, null);
    }
    
    private static class DigestAuthenticate {
        
        private String realm;
        private String qop;
        private String nonce;
        private String opaque;
        private String nc;
        private String cnonce;
        
        private static DigestAuthenticate create(String header)
                throws NoSuchAlgorithmException {

            String authenticate = header.replaceAll("(?si)^.*\r\n(WWW-Authenticate: Digest [^\r\n]+).*$", "$1");
            String algorithm = authenticate.replaceAll("(?i).* algorithm=\"([^\"]+).*$", "$1");
            MessageDigest secure = MessageDigest.getInstance(algorithm);
            DigestAuthenticate digestAuthenticate = new DigestAuthenticate();
            digestAuthenticate.realm  = authenticate.replaceAll("(?i).* realm=\"([^\"]*).*$", "$1");
            digestAuthenticate.qop    = authenticate.replaceAll("(?i).* qop=\"([^\"]*).*$", "$1");
            digestAuthenticate.nonce  = authenticate.replaceAll("(?i).* nonce=\"([^\"]*).*$", "$1");
            digestAuthenticate.opaque = authenticate.replaceAll("(?i).* opaque=\"([^\"]*).*$", "$1");
            digestAuthenticate.nc     = "00000001";
            digestAuthenticate.cnonce = new Date().toString();
            digestAuthenticate.cnonce = Codec.encodeHex(secure.digest(digestAuthenticate.cnonce.getBytes()));            

            return digestAuthenticate;
        }
        
        /**
         *  Erstellt einen Authorization-Eintrag fuer den HTTP-Header zur
         *  Autorisierung per Digest.
         *  @param  header   HTTP-Header
         *  @param  method   HTTP-Methoden
         *  @param  uri      URI
         *  @param  user     Benutzer
         *  @param  password Passwort
         *  @return der erstellte Authorization-Eintrag fuer den HTTP-Header
         *  @throws NoSuchAlgorithmException 
         */
        private static String create(String header, String method, String uri, String user, String password)
                throws NoSuchAlgorithmException {
            
            DigestAuthenticate digestAuthenticate;
            MessageDigest      secure;
            String             s1;
            String             s2;
            String             s3;
            
            secure = MessageDigest.getInstance("md5");

            digestAuthenticate = DigestAuthenticate.create(header);

            s1 = user + ":" + digestAuthenticate.realm + ":" + password;
            s1 = Codec.encodeHex(secure.digest(s1.getBytes()));
            s2 = method + ":" + uri;
            s2 = Codec.encodeHex(secure.digest(s2.getBytes()));
            s3 = ":" + digestAuthenticate.nonce + ":" + digestAuthenticate.nc + ":" + digestAuthenticate.cnonce + ":" + digestAuthenticate.qop + ":";
            s3 = Codec.encodeHex(secure.digest((s1 + s3 + s2).getBytes()));

            return "Authorization: Digest username=\"" + user + "\""
                   + ", algorithm=\"MD5\""
                   + ", nc=\"00000001\""
                   + ", qop=\"auth\""
                   + ", uri=\"" + uri + "\""
                   + ", nonce=\"" + digestAuthenticate.nonce + "\""
                   + ", cnonce=\"" + digestAuthenticate.cnonce + "\""
                   + ", response=\"" + s3 + "\""
                   + ", opaque=\"" + digestAuthenticate.opaque + "\""
                   + "\r\n";
        }
    }
    
    /**
     *  Sends a HTTP request to a server.
     *  @param  address
     *  @param  request
     *  @param  authentication
     *  @param  keystore
     *  @return the received response
     *  @throws IOException
     *  @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Authentication authentication, Keystore keystore)
            throws IOException, GeneralSecurityException {
        
        String response;
        String header;
        
        byte[] responseData;
        
        if (authentication.user == null)
            authentication.user = "";
        if (authentication.password == null)
            authentication.password = "";
        
        if (authentication instanceof Authentication.Digest) {
         
            responseData = HttpUtils.sendRequest(address, request, keystore);
            response = new String(responseData);
            header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
            if (!header.matches("(?si)^.*\r\nWWW-Authenticate: Digest .*$"))
                return responseData;
            
            String method = request.split(" ")[0];
            String uri    = request.split(" ")[1];
            
            int index = request.indexOf("\r\n\r\n");
            request = request.substring(0, index +2)
                    + DigestAuthenticate.create(header, method, uri, authentication.user, authentication.password)
                    + request.substring(index);
            
        } else {

            int index = request.indexOf("\r\n\r\n");
            request = request.substring(0, index +2)
                    + "Authorization: Basic " + Codec.encodeBase64(authentication.user + ":" + authentication.password) + "\r\n"
                    + request.substring(index);
        }
        
        return HttpUtils.sendRequest(address, request, keystore);
    }
}