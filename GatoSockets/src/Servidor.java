import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
	
	protected String ip = "127.0.0.1"; //"localhost";
	protected int port = 8000;//22222;
	private Socket socket;
	private ServerSocket serverSocket;
	protected boolean accepted = false;
	
	protected void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			GatoCliente.dos = new DataOutputStream(socket.getOutputStream());
			GatoCliente.dis = new DataInputStream(socket.getInputStream());
			accepted = true;
			System.out.println("EL CLIENTE HA SOLICITADO UNIRSE, Y HEMOS ACEPTADO");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean connect() {
		try {
			socket = new Socket(ip, port);
			GatoCliente.dos = new DataOutputStream(socket.getOutputStream());
			GatoCliente.dis = new DataInputStream(socket.getInputStream());
			accepted = true;
		} catch (IOException e) {
			System.out.println("Incapaz de comunicarse a la direccion: " + ip + ":" + port + " | Iniciando un servidor");
			return false;
		}
		System.out.println("Se ha conectado exitosamente al servidor.");
		return true;
	}

	protected void initializeServer() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		GatoCliente.tuTurno = true;
		GatoCliente.oponente = false;
	}
}
	
	
