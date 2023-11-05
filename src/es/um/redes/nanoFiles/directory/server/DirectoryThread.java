package es.um.redes.nanoFiles.directory.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import es.um.redes.nanoFiles.directory.message.DirMessage;
import es.um.redes.nanoFiles.directory.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class DirectoryThread extends Thread {

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	protected DatagramSocket socket = null;

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	protected double messageDiscardProbability;

	/**
	 * Estructura para guardar los nicks de usuarios registrados, y la fecha/hora de
	 * registro
	 * 
	 */
	private HashMap<String, LocalDateTime> nicks;
	/**
	 * Estructura para guardar los usuarios servidores (nick, direcciones de socket
	 * TCP)
	 */
	// TCP)
	private HashMap<String, InetSocketAddress> servers;
	/**
	 * Estructura para guardar la lista de ficheros publicados por todos los peers
	 * servidores, cada fichero identificado por su hash
	 */
	private HashMap<String, FileInfo> files;
	
	private HashMap<String, List<String>> whoisserving;//Este hashmap sirve para almacenar qué usuarios están sirviendo cada archivo en cada momento

	public DirectoryThread(int directoryPort, double corruptionProbability) throws SocketException {
		/*
		 * TODO (done): Crear dirección de socket con el puerto en el que escucha el
		 * directorio
		 */
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);
		// TODO (done): Crear el socket UDP asociado a la dirección de socket anterior
		socket = new DatagramSocket(serverAddress);
		messageDiscardProbability = corruptionProbability;
		// Creamos el diccionario "nicks".
		nicks = new HashMap<String, LocalDateTime>();
		servers = new HashMap<String, InetSocketAddress>();
		files = new HashMap<String, FileInfo>();
		whoisserving = new HashMap<String, List<String>>();
	}

	public void run() {
		InetSocketAddress clientId = null;

		System.out.println("Directory starting...");

		while (true) {
			try {

				// TODO: Recibimos a través del socket el datagrama con mensaje de solicitud
				byte[] receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
				DatagramPacket requestPacket = new DatagramPacket(receptionBuffer, receptionBuffer.length);
				socket.receive(requestPacket);

				// TODO: Averiguamos quién es el cliente
				clientId = (InetSocketAddress) requestPacket.getSocketAddress();

				// Vemos si el mensaje debe ser descartado por la probabilidad de descarte

				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directory DISCARDED datagram from " + clientId);
					continue;
				}

				// Analizamos la solicitud y la procesamos

				if (requestPacket.getData().length > 0) {
					processRequestFromClient(requestPacket.getData(), clientId);
				} else {
					System.err.println("Directory received EMPTY datagram from " + clientId);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Directory received EMPTY datagram from " + clientId);
				break;
			}
		}
		// Cerrar el socket
		socket.close();
	}

	// Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir un objeto mensaje (DirMessage) a partir de los datos
		// recibidos
		DirMessage receiveMessage = DirMessage.buildMessageFromReceivedData(data);
		// TODO: Actualizar estado del directorio y enviar una respuesta en función del
		// tipo de mensaje recibido
		byte receiveOpcode = receiveMessage.getOpcode();
		switch (receiveOpcode) {
		case DirMessageOps.OPCODE_LOGIN:
			sendLoginOK(clientAddr);
			System.out.println("SIGNIN request from " + clientAddr.getHostName() + ":" + clientAddr.getPort());
			break;
		case DirMessageOps.OPCODE_REGISTER_USERNAME:
			if(nicks.containsKey(receiveMessage.getUserName())) { //Comprueba que el nick no esté en el Hashmap de los nicks
				sendRegisterFAIL(clientAddr, data); //Si está en el Hashmap entonces manda un mensaje de fail
			}
			else { //En el caso contrario (que no esté contenido)
				System.out.println("REGISTER_USERNAME request from " + clientAddr.getHostName() + ":" + clientAddr.getPort());
				sendRegisterOK(clientAddr, data); //Entonces manda un mensaje de ok con los datos necesarios
				nicks.put(receiveMessage.getUserName(), LocalDateTime.now());	//Y añade el nick al hashmap
			}
			break;
		case DirMessageOps.OPCODE_GETUSERS:
			System.out.println("GET_USERLIST request from " + clientAddr.getHostName() + ":" + clientAddr.getPort());
			sendUserList(clientAddr, data);
			break;
		case DirMessageOps.OPCODE_LOOKUP_USERNAME:
			if(servers.containsKey(receiveMessage.getField())) {
				sendLookUpUsernameFound(clientAddr, receiveMessage.getField());
			}
			else {
				sendLookUpUsernameNotFound(clientAddr, receiveMessage.getField());
			}
			break;
		case DirMessageOps.OPCODE_GETFILES:
			System.out.println("GET_FILELIST request from " + clientAddr.getHostName() + ":" + clientAddr.getPort());
			sendFileList(clientAddr, data);
			break;
		case DirMessageOps.OPCODE_SERVE_FILES:
			try {
			ByteBuffer buf = ByteBuffer.wrap(receiveMessage.getField().getBytes());//Creamos el buffer de bytes
			int port = buf.getInt();
			String nick = ""; //Creamos la variable donde guardaremos el nick
			char charRet = (char) buf.get(); //Leemos el primer caracter del buffer de bytes
			while(charRet != ":".charAt(0)) {//Hasta que nos encontremos con el primer caracter delimitador vamos guardando los caracteres del nick
				nick = nick + charRet; //Concatenando los caracteres del nick
				charRet = (char) buf.get(); //Cogiendo el siguiente caracter del buffer
			}
			charRet = (char) buf.get(); //Hacemos esto para descartar el último que fue un caracter delimitador
			String fileinfoiteratedstring = "";//Iniciamos la variable donde iremos almacenando cada fileinfo iterado en forma de string codificado
			FileInfo iteratedfileinfo = null; //Iniciamos la variable donde iremos almacenando cada objeto del tipo FileInfo iterado
			while(buf.hasRemaining()) {//Mientras el buffer todavía tenga bytes que leer seguimos leyendo
				if(charRet == ":".charAt(0)) { //Cuando llegamos a un caracter delimitador
					iteratedfileinfo = FileInfo.fromEncodedString(fileinfoiteratedstring); //Entonces recuperamos el objeto FileInfo decodificando la cadena que hemos obtenido
					if (files.containsKey(iteratedfileinfo.fileHash)) { //Registramos el archivo en la lista de archivos disponibles del directorio
						whoisserving.get(iteratedfileinfo.fileHash).add(nick);
					}
					else {
						List<String> listanueva = new ArrayList<String>();
						whoisserving.put(iteratedfileinfo.fileHash, listanueva);
						whoisserving.get(iteratedfileinfo.fileHash).add(nick);
						files.put(iteratedfileinfo.fileHash, iteratedfileinfo);
					} //Registramos el archivo en la lista de archivos disponibles del directorio
					fileinfoiteratedstring = ""; //Reiniciamos la variable string que vamos iterando
				}
				else {
					fileinfoiteratedstring = fileinfoiteratedstring + charRet;//Vamos concatenando los caracteres del buffer que pertenecen al fileinfo como string codificado
				}
				charRet = (char) buf.get();//Vamos leyendo caracteres del buffer
			}
			iteratedfileinfo = FileInfo.fromEncodedString(fileinfoiteratedstring); //Introducimos el último, que no termina con un caracter delimitador
			if (files.containsKey(iteratedfileinfo.fileHash)) {
				whoisserving.get(iteratedfileinfo.fileHash).add(nick);
			}
			else {
				List<String> listanueva = new ArrayList<String>();
				whoisserving.put(iteratedfileinfo.fileHash, listanueva);
				whoisserving.get(iteratedfileinfo.fileHash).add(nick);
				files.put(iteratedfileinfo.fileHash, iteratedfileinfo);
			}
			String pruebawhois = whoisserving.get(iteratedfileinfo.fileHash).get(0);
			InetSocketAddress clientServerAddr = new InetSocketAddress(socket.getInetAddress(), port);
			servers.put(nick, clientServerAddr);
			sendServeFilesOK(clientAddr);
			} catch (Exception e) {
				sendServeFilesFAIL(clientAddr);
				
			}
			break;
		case DirMessageOps.OPCODE_QUIT:
			if(!servers.containsKey(receiveMessage.getUserName())) { //Comprueba que el nick no esté en el Hashmap de los servidores para ver si se ha desconectado su servidor o el
				nicks.remove(receiveMessage.getUserName());
				removeUsernameFromMaps(receiveMessage.getUserName());
			}
			else { //En el caso de que si esté en este, entonces no se desloggea el, solo se desconecta su servidor, por lo que hay que mantener su nick en el hashmap de los nicks
				removeUsernameFromMaps(receiveMessage.getUserName());
			}
			break;
		default:
		}
	}

	// Método para enviar la confirmación del registro
	private void sendLoginOK(InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir el datagrama con la respuesta y enviarlo por el socket al
		// cliente
		int numServers = servers.size();
		byte[] responseData = DirMessage.buildLoginOKResponseMessage(numServers);
		DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(responsePacket);
	}
	
	private void sendRegisterOK(InetSocketAddress clientAddr, byte[] message) throws IOException {
		byte[] responseData = DirMessage.buildRegisterOKResponseMessage(message.toString());
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(packetToClient);
	}
	
	private void sendRegisterFAIL(InetSocketAddress clientAddr, byte[] message) throws IOException {
		byte[] responseData = DirMessage.buildRegisterFAILResponseMessage();
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(packetToClient);
	}

	private void sendUserList(InetSocketAddress clientAddr, byte[] message) throws IOException {
		String stringtosend = "";
		for (String key:nicks.keySet())//Recorremos todas las claves del hashmap
		{
			
			stringtosend = stringtosend + key; //Copiamos los bytes de cada clave al ByteBuffer
			if (servers.containsKey(key)) {
				stringtosend = stringtosend +"    LIVE";
			}
			stringtosend = stringtosend +":"; //Separamos los campos con el caracter :
		}
		ByteBuffer bb = ByteBuffer.wrap(stringtosend.getBytes()); //Creamos un ByteBuffer para ir guardando los nicks
		byte[] responseData = DirMessage.buildUserListResponse(bb.array()); //Mandamos el array de bytes del buffer al método que construye el mensaje de respuesta
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr); //Con el mensaje de respuesta de DirMessage creamos el paquete a enviar al cliente
		socket.send(packetToClient); //Enviamos el paquete al cliente
	}
	
	private void sendFileList(InetSocketAddress clientAddr, byte[] message) throws IOException {
		String stringtosend = "";
		for (String key:files.keySet())//Recorremos todas las claves del hashmap de archivos
		{
			stringtosend = stringtosend + "Name: "+files.get(key).fileName+", Hash: "+key+ ", Path: "+files.get(key).filePath+ ", Size: "+files.get(key).fileSize; //Copiamos los bytes de cada clave al ByteBuffer
			stringtosend = stringtosend+"\n  \\-This file is being uploaded by these servers: ";
			stringtosend = stringtosend + whoisserving.get(key).get(0);
			for(int i=1;i<whoisserving.get(key).size();i++) {
				stringtosend = stringtosend + ", ";
				stringtosend = stringtosend + whoisserving.get(key).get(i);
			}
			stringtosend = stringtosend + ".";
			stringtosend = stringtosend +";"; //Separamos los campos con el caracter :
		}
		ByteBuffer bb = ByteBuffer.wrap(stringtosend.getBytes()); //Creamos un ByteBuffer para ir guardando los nicks
		byte[] responseData = DirMessage.buildFileListResponse(bb.array()); //Mandamos el array de bytes del buffer al método que construye el mensaje de respuesta
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr); //Con el mensaje de respuesta de DirMessage creamos el paquete a enviar al cliente
		socket.send(packetToClient); //Enviamos el paquete al cliente
	}
	
	private void sendServeFilesOK(InetSocketAddress clientAddr) throws IOException {
		byte[] responseData = DirMessage.buildServeFilesOKResponseMessage();
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(packetToClient);
	}
	
	private void sendServeFilesFAIL(InetSocketAddress clientAddr) throws IOException {
		byte[] responseData = DirMessage.buildServeFilesFailResponseMessage();
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(packetToClient);
	}
	
	private void sendLookUpUsernameFound(InetSocketAddress clientAddr, String nick) throws IOException {
		String ipport = servers.get(nick).getAddress().toString()+":"+String.valueOf(servers.get(nick).getPort());
		byte[] responseData = DirMessage.buildLookUpUsernameFoundResponseMessage(ipport);
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(packetToClient);
	}
	
	private void sendLookUpUsernameNotFound(InetSocketAddress clientAddr, String nick) throws IOException {
		byte[] responseData = DirMessage.buildLookUpUsernameNotFoundResponseMessage();
		DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(packetToClient);
	}
	
	private void removeUsernameFromMaps(String nickname) {
		for(String key:servers.keySet()) {
			if (key.equals(nickname)) {
				servers.remove(key);
			}
		}
		for(String key:whoisserving.keySet()) {
			for(int i=0;i<whoisserving.get(key).size();i++) {
				if (whoisserving.get(key).get(i).equals(nickname)) {
					whoisserving.get(key).remove(i);
					if (whoisserving.get(key).isEmpty()) {
						files.remove(key);
					}
				}
			}
		}
	}
}
