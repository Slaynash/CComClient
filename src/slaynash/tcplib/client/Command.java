package slaynash.tcplib.client;

import java.io.IOException;

public abstract class Command {
	
	private Client client = null;
	private String outId = "";
	
	public abstract void handle(String parts);
	
	
	public void println(String string) {
		try {
			client.println(outId+" "+string);
		} catch (IOException e) {
			LogSystem.err.printStackTrace(e);
			remoteError(e.getMessage());
		}
	}
	
	public void printlnSecure(String string) {
		try {
			client.printlnSecure(outId+" "+string);
		} catch (IOException e) {
			LogSystem.err.printStackTrace(e);
			remoteError(e.getMessage());
		}
	}
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	protected Client getClient() {
		return client;
	}
	
	protected void destroy() {
		CommandManager.remove(this);
	}

	public void setOutId(String outId) {
		this.outId = outId;
	}
	
	public String getOutId() {
		return outId;
	}
	
	public void remoteError(String error) {
		destroy();
	}
	
}
