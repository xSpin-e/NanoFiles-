package es.um.redes.nanoFiles.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.message.PeerMessage;
import es.um.redes.nanoFiles.message.PeerMessageOps;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NFConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;

	public NFConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		/*
		 * TODO Se crea el socket a partir de la dirección del servidor (IP, puerto). La
		 * creación exitosa del socket significa que la conexión TCP ha sido
		 * establecida.
		 */
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort()); // Clase 23/3
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
		
		/*
		 * TODO Se crean los DataInputStream/DataOutputStream a partir de los streams de
		 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
		 * datos del servidor mediante los métodos readUTF y writeUTF (mensajes
		 * formateados como cadenas de caracteres codificadas en UTF8)
		 */
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	public DataInputStream getDis() {
		return this.dis;
	}

	/**
	 * Método que utiliza el Shell para ver si hay datos en el flujo de entrada.
	 * Permite "sondear" el socket con el fin evitar realizar una lectura bloqueante
	 * y así poder realizar otras tareas mientras no se ha recibido ningún mensaje.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr El hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escriben los datos
	 *                             descargados del servidor (contenido del fichero
	 *                             remoto)
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean download(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		/*
		 * DONE TODO: Construir objeto PeerMessage que modela un mensaje de solicitud de
		 * descarga de fichero (indicando el fichero a descargar), convertirlo a su
		 * codificación en String (mediante toEncodedString) y enviarlo al servidor.
		 */
		PeerMessage messageToServer = new PeerMessage(PeerMessageOps.OP_DOWNLOAD, targetFileHashSubstr);
		String dataToServer = messageToServer.toEncodedString();
		dos.writeUTF(dataToServer);
		PeerMessage messageFromServer = null;
		FileOutputStream fileoutputstream = new FileOutputStream(file);// Abrimos el stream de datos para recibir los
																		// datos del fichero descargado
		String dataFromServer = dis.readUTF();// Leemos el paquete enviado por el servidor
		messageFromServer = PeerMessage.fromString(dataFromServer);// Construimos el mensaje recibido
		while (!dataFromServer.equals(""))// Mientras no recibamos un mensaje vacío seguimos leyendo paquetes
		{
			/*
			 * DONE TODO: Recibir mensajes del servidor codificados como cadena de
			 * caracteres, convertirlos a PeerMessage (mediante "fromString"), y actuar en
			 * función del tipo de mensaje recibido.
			 */
			if (messageFromServer.getOperation().equals(PeerMessageOps.OP_DOWNLOAD_FAIL)) {
				fileoutputstream.close();
				return false;
			}
			// Comprobamos que sea un mensaje con datos del archivo
			if (messageFromServer.getOperation().equals(PeerMessageOps.OP_DOWNLOAD_OK)) {
				/*
				 * DONE TODO: Crear un FileOutputStream a partir de "file" para escribir cada
				 * fragmento recibido en el fichero. Cerrar el FileOutputStream una vez se han
				 * escrito todos los fragmentos.
				 */
				// Escribimos los bytes de los datos recibidos en el stream de datos del archivo a escribir
				fileoutputstream.write(messageFromServer.getFileData());
			}
			dataFromServer = dis.readUTF();// Leemos el paquete enviado por el servidor
			messageFromServer = PeerMessage.fromString(dataFromServer);// Construimos el mensaje recibido

		}
		fileoutputstream.close();// Como ya hemos recibido un paquete vacío eso nos indica que no hay mas
									// paquetes de datos del archivo descargado por leer.

		/*
		 * TODO: Comprobar la integridad del fichero creado, calculando su hash y
		 * comparándolo con el hash del fichero solicitado.
		 */
		downloaded = true;
		return downloaded;
	}
	
	public boolean queryFiles() throws IOException {
		PeerMessage messageToServer = new PeerMessage(PeerMessageOps.OP_QUERYFILES);
		String dataToServer = messageToServer.toEncodedString();
		dos.writeUTF(dataToServer);
		String dataFromServer = dis.readUTF();// Leemos el paquete enviado por el servidor
		PeerMessage messageFromServer = PeerMessage.fromString(dataFromServer);// Construimos el mensaje recibido
		String stringtoprocess = messageFromServer.getFileHash();
		String mensajeficheros = "Ficheros disponibles en este servidor:\n"; //Esta cadena es la que se devolverá
		String lineas[] = stringtoprocess.split(":");
		String campos[];
		for(String linea:lineas) {
			campos = linea.split(";");
			mensajeficheros = mensajeficheros + "Nombre: "+campos[1]+", Hash: "+campos[0]+", Tamaño: "+campos[2]+"\n";	
		}
		System.out.println(mensajeficheros);
		
		
		return true;
	}
}


