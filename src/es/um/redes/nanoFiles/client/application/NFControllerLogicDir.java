package es.um.redes.nanoFiles.client.application;

import java.io.IOException;
import es.um.redes.nanoFiles.directory.connector.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {
	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para conectar con el directorio y obtener el número de peers que están
	 * sirviendo ficheros
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 */
	boolean logIntoDirectory(String directoryHostname) {
		/*
		 * TODO: Debe crear un objeto DirectoryConnector a partir del parámetro
		 * directoryHostname y guardarlo en el atributo correspondiente. A continuación,
		 * utilizarlo para comunicarse con el directorio y realizar tratar de realizar
		 * el "login", informar por pantalla del éxito/fracaso y devolver dicho valor
		 */

		boolean result = false;
		boolean connectionOk = true;
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
		} catch (IOException e) {
			System.err.println("Fallo en la conexión con el directorio.");
			connectionOk = false;
		}
		if (connectionOk) {
			int numServers = 0;
			try {
				numServers = directoryConnector.logIntoDirectory();
			} catch (IOException e) {
				System.err.println("Failed to connect to the directory -directory not responding");
				connectionOk = false;
			}

			if (connectionOk) {
				System.out.println("Number of files servers " + Integer.toString(numServers));
				result = true;
			}
		}
		return result;
	}

	/**
	 * Método para registrar el nick del usuario en el directorio
	 * 
	 * @param nickname el nombre de usuario a registrar
	 * @return true si el nick es válido (no contiene ":") y se ha registrado
	 *         nickname correctamente con el directorio (no estaba duplicado), falso
	 *         en caso contrario.
	 */
	boolean registerNickInDirectory(String nickname) {
		/*
		 * TODO: Registrar un nick. Comunicarse con el directorio (a través del
		 * directoryConnector) para solicitar registrar un nick. Debe informar por
		 * pantalla si el registro fue exitoso o fallido, y devolver dicho valor
		 * booleano. Se debe comprobar antes que el nick no contiene el carácter ':'.
		 */
		boolean result = false;
		try {
			result = directoryConnector.registerNickname(nickname);
		} catch (IOException e) {
			System.err.println("* Error communicating with directory.");
		}	
		return result;
	}

	/**
	 * Método para obtener de entre los peer servidores registrados en el directorio
	 * la IP:puerto del peer con el nick especificado
	 * 
	 * @param nickname el nick del peer por cuya IP:puerto se pregunta
	 * @return La dirección de socket del peer identificado por dich nick, o null si
	 *         no se encuentra ningún peer con ese nick.
	 * @throws IOException 
	 */
	String lookupUserInDirectory(String nickname) throws IOException {
		/*
		 * TODO: Obtener IP:puerto asociada a nickname. Comunicarse con el directorio (a
		 * través del directoryConnector) para preguntar la dirección de socket en la
		 * que el peer con 'nickname' está sirviendo ficheros. Si no se obtiene una
		 * respuesta con IP:puerto válidos, se debe devolver null.
		 */
		String ipport = directoryConnector.lookUpUsername(nickname);
		if (ipport!="notfound") {
			return ipport;
		}
		else {
			return null;
		}
		
	}

	/**
	 * Método para publicar la lista de ficheros que este peer está compartiendo.
	 * 
	 * @param port     El puerto en el que este peer escucha solicitudes de conexión
	 *                 de otros peers.
	 * @param nickname El nick de este peer, que será asociado a lista de ficheros y
	 *                 su IP:port
	 * @throws IOException 
	 */
	boolean publishLocalFilesToDirectory(int port, String nickname) throws IOException {
		/*
		 * TODO: Enviar la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para enviar la lista de ficheros servidos por
		 * este peer con nick 'nickname' en el puerto 'port'. Los ficheros de la carpeta
		 * local compartida están disponibles en NanoFiles.db).
		 */
		FileInfo[] filearray = NanoFiles.db.getFiles();
		boolean responseData = directoryConnector.publishLocalFiles(port, nickname, filearray);
		return responseData;
	}

	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 */
	void getUserListFromDirectory() {
		/*
		 * TODO: Obtener la lista de usuarios registrados. Comunicarse con el directorio
		 * (a través del directoryConnector) para obtener la lista de nicks registrados
		 * e imprimirla por pantalla.
		 */
		String responseData;
		
		try {
			responseData = directoryConnector.getUserList();
		} catch (IOException e) {
			responseData = null;
		}
		System.out.println(responseData);
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	void getFileListFromDirectory() {
		/*
		 * TODO: Obtener la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para obtener la lista de ficheros e imprimirla
		 * por pantalla.
		 */
		String responseData;
		
		try {
			responseData = directoryConnector.getFileList();
		} catch (IOException e) {
			responseData = null;
		}
		System.out.println(responseData);
	}

	/**
	 * Método para desconectarse del directorio (cerrar sesión)
	 * @throws IOException 
	 */
	public void logout(String nickname) throws IOException {
		/*
		 * TODO: Dar de baja el nickname. Al salir del programa, se debe dar de baja el
		 * nick registrado con el directorio y cerrar el socket usado por el
		 * directoryConnector.
		 */
		directoryConnector.disconnectFromDirectory(nickname);
		directoryConnector.getSocket().close();
	}
	
	public void removeServerFromDirectory(String nickname) throws IOException {
		directoryConnector.disconnectFromDirectory(nickname);
	}
}
