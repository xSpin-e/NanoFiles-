package es.um.redes.nanoFiles.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private ServerSocket serverSocket = null;
	
	public NFServerSimple(int port) throws IOException {
		/*
		 * TODO: Crear una direción de socket a partir del puerto especificado
		 */
		InetSocketAddress FileServerSocketAddress = new InetSocketAddress(port);	//Clase 23/3
		/*
		 * TODO: Crear un socket servidor y ligarlo a la dirección de socket anterior
		 */
		serverSocket = new ServerSocket();				
		serverSocket.setSoTimeout(1000);
		serverSocket.bind(FileServerSocketAddress);   	
		serverSocket.setReuseAddress(true); 			
		/*
		 * TODO: (Opcional) Establecer un timeout para que el método accept no espere
		 * indefinidamente
		 */
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * @throws IOException 
	 * 
	 */
	public void run() throws IOException {
		/*
		 * TODO: Comprobar que el socket servidor está creado y ligado
		 */
		BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
		if(serverSocket == null || !serverSocket.isBound()) {		
			System.err.println("Failed to run FileServer, server socket not bound to any port");
			return;
		}
		else {
			System.out.println("NFServerSimple server running on "+ serverSocket.getLocalSocketAddress());
			System.out.println("Enter '" + STOP_SERVER_COMMAND + "' to stop the server");
		}

		boolean stopServer = false;
		boolean whoclosed = false;
		Socket socket = null;			
		while (!stopServer) {
			/*
			 * TODO: Usar el socket servidor para esperar conexiones de otros peers que
			 * soliciten descargar ficheros
			 */
			try {					
				socket = serverSocket.accept();
				System.out.println("New client connected from " + socket.getInetAddress().toString() + ":" + socket.getPort());
				
			} catch (SocketTimeoutException e) {
				if (standardInput.ready() && standardInput.readLine().equals(STOP_SERVER_COMMAND)) {
					stopServer = true;
				}
			} catch (Exception e) {
				System.err.println("There was a problem with local file server running");
				e.printStackTrace();
			}
			/*
			 * TODO: Al establecerse la conexión con un peer, la comunicación con dicho
			 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
			 * hay que pasarle el objeto Socket devuelto por accept (retorna un nuevo socket
			 * para hablar directamente con el nuevo cliente conectado)
			 */
			if(socket != null) {				
				if (!socket.isClosed()) {
					whoclosed = NFServerComm.serveFilesToClient(socket);
				}
			}
			if (whoclosed) {
				stopServer = true;
			}
			/*
			 * TODO: (Para poder detener el servidor y volver a aceptar comandos).
			 * Establecer un temporizador en el ServerSocket antes de ligarlo, para
			 * comprobar mediante standardInput.ready()) periódicamente si se ha tecleado el
			 * comando "fgstop", en cuyo caso se cierra el socket servidor y se sale del
			 * bucle
			 */
			
		}
		serverSocket.close();
		System.out.println("NFServerSimple stopped");

	}
}