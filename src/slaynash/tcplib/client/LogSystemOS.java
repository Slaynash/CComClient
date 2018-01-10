package slaynash.tcplib.client;

public class LogSystemOS {
	
	public LogSystemOS() {}

	public void println(Object out) {}

	public void printStackTrace(Throwable e) {
		println(e.toString());
		for(StackTraceElement ste:e.getStackTrace()) println("\tat "+ste.toString());
		if(e.getCause() != null) printStackTrace(e.getCause());
	}
}
