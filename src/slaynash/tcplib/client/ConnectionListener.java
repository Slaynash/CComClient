package slaynash.tcplib.client;

public interface ConnectionListener {

	void connectionStarted();
	void waitingForConnection();
	void connecting();
	void connected();
	void connectionFailed(Throwable error);
	void disconnected(Throwable error);

}
