package Client;

import Client.Entites.User;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import Server.Log;
public class ConnectionManager {
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private static ConnectionManager instance;
	private ChatWindow interf;
	
	public static ConnectionManager getInstance() {
		if(instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}
	public void serverListener() {
		inicializeTic();
		try {
			while(true) {
				String packet = receive();
				if(packet.startsWith("200")) {
					//Success
				}else if(packet.startsWith("400")) {
					JOptionPane.showMessageDialog(interf, packet.substring(4), "Disconnected", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}else if(packet.startsWith("500")) {
					JOptionPane.showMessageDialog(interf, packet.substring(4), "ERROR", JOptionPane.WARNING_MESSAGE);
				}else if(packet.startsWith("ROOM")) {
					String[] p = packet.split("[]");
					interf.setTitle("Room " + p[1] + "@" + socket.getInetAddress().getHostAddress());
				}else if(packet.startsWith("LIST")) {
					interf.cleanList();
					int count = packet.split("[]").length;
					for(int i = 1; i < count; i++) {
						User u = new User(packet.split("[ ]")[i], null);
						interf.addUser(u);
					}
				}else if(packet.isEmpty()) {
					interf.addMessage(packet);
				}
			}
		}catch (NullPointerException ex) {
			JOptionPane.showMessageDialog(interf, "The connection to the server has been lost", "Disconnected" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
	}
	public void setServer(String IP, int port) {
		try {
			socket = new Socket(IP, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
			
		}catch (IOException ex) {
			Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public void submit(String var) {
		if(!var.isEmpty()) {
			try {
				writer.write(var + "\n");
				writer.flush();
			}catch (IOException ex) {
				Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	public String receive() {
		String s = "";
		try {
			s = reader.readLine();
			
		}catch (IOException ex) {
	
		}
		return s;	
	}
	public void setInterfece(ChatWindow interf) {
		this.interf = interf;
	}
	private void inicializeTic() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				long tic = System.currentTimeMillis();
				while(true) {
					if(System.currentTimeMillis() - tic >= 5000) {
						submit("Tic " + System.currentTimeMillis());
						tic = System.currentTimeMillis();
					}
				}
			}
		}).start();
		
	}
}