package com.flyn.volcano;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUrlSSLSocketFactory extends SSLSocketFactory
{
    private SSLContext sslContext = SSLContext.getInstance("TLS");

    public HttpUrlSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super();
        KeyStore keyStore = Utils.getKeystore();
        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(keyStore, null);

        this.sslContext.init(kmf.getKeyManagers(), new TrustManager[] { new X509TrustManager()
        {

            @Override
            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {

            }
        } }, null);

    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException
    {
        return this.sslContext.getSocketFactory().createSocket(s, host, port, autoClose);
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        return null;
    }

    @Override
    public String[] getSupportedCipherSuites()
    {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException
    {
        return this.sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException
    {
        return this.sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException
    {
        return this.sslContext.getSocketFactory().createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
    {
        return this.sslContext.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

    public static SSLSocketFactory getFixedSocketFactory()
    {
        SSLSocketFactory socketFactory;
        try
        {
            socketFactory = new HttpUrlSSLSocketFactory();
        } catch (Throwable t)
        {
            t.printStackTrace();
            socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
        return socketFactory;
    }
}
