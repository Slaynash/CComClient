package slaynash.tcplib.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Client {
	
	private SSLSocket socket = null;

	private boolean listen = true;
	
	private BufferedReader inputStream;
	private PrintWriter outputStream;
	
	private Thread thread;

	private SSLContext sslContext;
	private static final String PASSPHRASE = "clientpw";
	
	private ConnectionListener connectionListener = null;
	
	private boolean autoReconnect = false;

	private String address;
	private int port;
	private String clientVersion = "0.0";

	public Client(String address, int port, String clientVersion) {
		this.address = address;
		this.port = port;
		this.clientVersion = clientVersion;
		
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				clientThread();
				while(autoReconnect) {
					try{Thread.sleep(2000);} catch(InterruptedException ie) {}
					clientThread();
				}
			}
		});
		thread.setName("LUM Thread");
		thread.setDaemon(true);
	}
	
	public void startConnection() {
		thread.start();
	}
	
	private void clientThread() {
		try {
			if(connectionListener != null) connectionListener.connectionStarted();
			LogSystem.info.println("[LUM] Connecting to server...");
			setupSSL();
			SSLSocketFactory sf = sslContext.getSocketFactory();
			socket = (SSLSocket)sf.createSocket(address, port);
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
			String ln;
			if(connectionListener != null) connectionListener.waitingForConnection();
			LogSystem.info.println("[LUM] Waiting for connection...");
			while((ln = readln()) != null && !ln.equals("READY"));
			if(connectionListener != null) connectionListener.connecting();
			LogSystem.info.println("[LUM] Connecting...");
			println("TCPLIB_"+clientVersion);
			if((ln = readln()) == null || !ln.equals("OK")) {
				throw new Exception("Connection aborted");
			}
			if(connectionListener != null) connectionListener.connected();
			LogSystem.info.println("[LUM] Connected.");
		} catch (Exception e) {
			LogSystem.err.printStackTrace(e);
			if(connectionListener != null) connectionListener.connectionFailed(e);
			return;
		}
		try {
			listen();
		} catch (Exception e) {
			LogSystem.err.printStackTrace(e);
			if(connectionListener != null) connectionListener.disconnected(e);
		}
		
	}
	
	private void setupSSL() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyManagementException, UnrecoverableKeyException {
		SecureRandom secureRandom = new SecureRandom();
	    secureRandom.nextInt();
		
		KeyStore serverKeyStore = KeyStore.getInstance( "JKS" );
		serverKeyStore.load( Client.class.getClassLoader().getResourceAsStream( "server.public" ), "public".toCharArray() );
		KeyStore clientKeyStore = KeyStore.getInstance( "JKS" );
		clientKeyStore.load( Client.class.getClassLoader().getResourceAsStream( "client.private" ), PASSPHRASE.toCharArray() );
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init( serverKeyStore );
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		kmf.init( clientKeyStore, PASSPHRASE.toCharArray() );
		
		sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom );
	}

	public String readln() throws IOException {
		String in = inputStream.readLine();
		if(in != null) LogSystem.info.println("<<< "+in);
		return in;
	}

	public String readlnSecure() throws IOException {
		String in = inputStream.readLine();
		if(in != null) LogSystem.info.println("<<< *****************");
		return in;
	}

	public void println(String out) {
		LogSystem.info.println(">>> "+out);
		outputStream.println(out);
		outputStream.flush();
	}
	
	public void printlnSecure(String out) {
		LogSystem.info.println(">>> *****************");
		outputStream.println(out);
		outputStream.flush();
	}

	private void listen() throws IOException {
		String input = "";
		while(listen && (input = readln()) != null) {
			CommandManager.runCommand(input, this);
		}
	}

	public void setConnectionListener(ConnectionListener lumConnectionEventListener) {
		connectionListener = lumConnectionEventListener;
	}

	public void setAutoReconnect(boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
	}
}
