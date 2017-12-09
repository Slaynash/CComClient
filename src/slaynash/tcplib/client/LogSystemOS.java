package slaynash.tcplib.client;

public class LogSystemOS {
	
	public LogSystemOS() {}

	public void println(Object out) {}

	public void printStackTrace(Exception e) {
		println(e.getClass().getName()+": "+e.getMessage());
		for(StackTraceElement ste:e.getStackTrace()) println("\tat "+ste.toString());
	}
}
