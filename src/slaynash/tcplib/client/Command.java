package slaynash.tcplib.client;

public abstract class Command {
	
	private Client client = null;
	private String outId = "";
	
	public abstract void handle(String parts);
	
	
	public void println(String string) {
		client.println(outId+" "+string);
	}
	
	public void printlnSecure(String string) {
		client.printlnSecure(outId+" "+string);
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
	
	public void remoteError(String string) {
		destroy();
	}
	
}
