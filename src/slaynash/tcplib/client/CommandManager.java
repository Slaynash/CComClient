package slaynash.tcplib.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
public class CommandManager {
	
	private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis() * 1000);
	
	private static Map<String, Class<? extends Command>> commands = new HashMap<String, Class<? extends Command>>();
	private static Map<String, HashMap<String, Command>> runningCommands = new HashMap<String, HashMap<String, Command>>();

	public static void runCommand(String line, Client client) throws IOException {
		Command command = null;
		final String[] parts = line.split(" ", 3);
		HashMap<String, Command> commandContainer = null;
		if((commandContainer = runningCommands.get(parts[0])) != null && (command = commandContainer.get(parts[1])) != null) {
			if(parts[2].startsWith("ERROR")) command.remoteError(parts[2].split(" ", 2)[1]);
			else command.handle(parts[2]);
		}
		else {
			Class<? extends Command> commandClass = null;
			if((commandClass = commands.get(parts[0])) != null) {
				try {
					command = commandClass.newInstance();
					command.setClient(client);
					command.setOutId(parts[0]+" "+parts[1]);
					commandContainer = runningCommands.get(parts[0]);
					if(commandContainer == null) {
						commandContainer = new HashMap<String, Command>();
						runningCommands.put(parts[0], commandContainer);
					}
					commandContainer.put(parts[1], command);
					final Command commandHandled = command;
					Thread commandThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							if(commandHandled != null) try {
								commandHandled.handle(parts[2]);
							}catch(Exception e) {commandHandled.println("ERROR "+e.getMessage().toUpperCase());}
						}
					}, "COMMAND_"+parts[0]+"_"+parts[1]);
					commandThread.start();
					
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			else {
				client.println(parts[0]+" "+parts[1]+" ERROR COMMAND_NOT_FOUND");
			}
		}
		
	}

	public static void registerCommand(String name, Class<? extends Command> command) {
		if(commands.get(name) == null) commands.put(name, command);
		else LogSystem.err.println("[CCOM] Trying to register a command twice ("+name+")");
	}
	
	public static void remove(Command command) {
		HashMap<String, Command> commandContainer = null;
		String[] parts = command.getOutId().split(" ", 2);
		if((commandContainer = runningCommands.get(parts[0])) != null && (commandContainer.get(parts[1])) != null) {
			commandContainer.remove(parts[1]);
		}
	}
	
	public static Command createInstance(String className, Client client){
		
		Command command = null;
		Class<? extends Command> commandClass = null;
		if((commandClass = commands.get(className)) != null) {
			try {
				command = commandClass.newInstance();
				long outId = counter.getAndIncrement();
				command.setClient(client);
				command.setOutId(className+" "+outId);
				HashMap<String, Command> commandContainer = runningCommands.get(className);
				if(commandContainer == null) {
					commandContainer = new HashMap<String, Command>();
					runningCommands.put(className, commandContainer);
				}
				commandContainer.put(""+outId, command);
				return command;
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
