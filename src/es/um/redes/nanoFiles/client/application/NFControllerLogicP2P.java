package es.um.redes.nanoFiles.client.application;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

import es.um.redes.nanoFiles.client.comm.NFConnector;
import es.um.redes.nanoFiles.server.NFServer;
import es.um.redes.nanoFiles.server.NFServerSimple;

public class NFControllerLogicP2P {
	/**
	 * El servidor de ficheros de este peer
	 */
	private NFServer bgFileServer = null;
	/**
	 * El cliente para conectarse a otros peers
	 */
	NFConnector nfConnector;
	/**
	 * El controlador que permite interactuar con el directorio
	 */
	private NFControllerLogicDir controllerDir;

	protected NFControllerLogicP2P() {
	}

	protected NFControllerLogicP2P(NFControllerLogicDir controller) {
		// Referencia al controlador que gestiona la comunicación con el directorio
		controllerDir = controller;
	}

	/**
	 * Método para ejecutar un servidor de ficheros en primer plano. Debe arrancar
	 * el servidor y darse de alta en el directorio para publicar el puerto en el
	 * que escucha.
	 * 
	 * 
	 * @param port     El puerto en que el servidor creado escuchará conexiones de
	 *                 otros peers
	 * @param nickname El nick de este peer, parar publicar los ficheros al
	 *                 directorio
	 * @throws IOException 
	 */
	protected void foregroundServeFiles(int port, String nickname) throws IOException {
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		// TODO: Crear objeto servidor NFServerSimple ligado al puerto especificado
		NFServerSimple serverSimple;	
		try {							
			serverSimple = new NFServerSimple(port);
			boolean result = controllerDir.publishLocalFilesToDirectory(port, nickname);
		} catch (IOException e) {
			System.err.println("Unable to start de server.");
			return;
		}
		// TODO: Publicar ficheros compartidos al directorio

		// TODO: Ejecutar servidor en primer plano
		serverSimple.run();  			
		controllerDir.removeServerFromDirectory(nickname);
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor y darse de alta en el directorio para publicar el puerto en el
	 * que escucha.
	 * 
	 * @param port     El puerto en que el servidor creado escuchará conexiones de
	 *                 otros peers
	 * @param nickname El nick de este peer, parar publicar los ficheros al
	 *                 directorio
	 * @throws IOException 
	 */
	protected void backgroundServeFiles(int port, String nickname) throws IOException {
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		// TODO: Comprobar que no existe ya un objeto NFServer previamente creado, en
		// cuyo caso el servidor ya está en marcha
		if(bgFileServer==null) {
			try {
				bgFileServer = new NFServer(port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Unable to start the server");
				e.printStackTrace();
			}
		}

		// TODO: Arrancar un hilo servidor en segundo plano
		bgFileServer.startServer();//el start server lo ejecuta en 2º plano
			
		// TODO: Publicar ficheros compartidos al directorio
		controllerDir.publishLocalFilesToDirectory(port, nickname);

		// TODO: Imprimir mensaje informando de que el servidor está en marcha
		System.out.println("NFServer server running on " + bgFileServer.getServerSocket().getLocalSocketAddress());
	}

	/**
	 * Método para establecer una conexión con un peer servidor de ficheros
	 * 
	 * @param nickname El nick del servidor al que conectarse (o su IP:puerto)
	 * @return true si se ha podido establecer la conexión
	 * @throws IOException 
	 */
	protected boolean browserEnter(String nickname) throws IOException {
		boolean connected = false;
		/*
		 * TODO: Averiguar si el nickname es en realidad una cadena con IP:puerto, en
		 * cuyo caso no es necesario comunicarse con el directorio.
		 */
		String ip = "";							
		int port = 0;							
		boolean isNickname = true;				
		int posSep = nickname.indexOf(":");		
		if(posSep != -1) {						
			isNickname = false;
			ip = nickname.substring(0,posSep);	
			port = Integer.parseInt(nickname.substring(posSep+1));	//Como asumir que el puerto está bien metido
		}
		else {
			/*
		 * TODO: Si es un nickname, preguntar al directorio la IP:puerto asociada a
		 * dicho peer servidor.
		 */
			nickname = controllerDir.lookupUserInDirectory(nickname).split("/")[1];
			if (nickname == null) {
				System.out.println("No era una dirección válida y no existe un servidor hosteado por ese nick");
				return false;
			}
			posSep = nickname.indexOf(":");		
			ip = nickname.substring(0,posSep);	
			port = Integer.parseInt(nickname.substring(posSep+1));
		}
		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);
		try {																
			nfConnector = new NFConnector(serverAddress);
			if (!nfConnector.getSocket().isClosed()) {
				connected = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			connected = false;
		} 
		return connected;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros al que nos
	 * hemos conectador mediante browser Enter
	 * 
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 * @throws IOException 
	 */
	protected void browserDownloadFile(String targetFileHash, String localFileName) throws IOException {
		/*
		 * TODO: Usar el NFConnector creado por browserEnter para descargar el fichero
		 * mediante el método "download". Se debe omprobar si ya existe un fichero con
		 * el mismo nombre en esta máquina, en cuyo caso se informa y no se realiza la
		 * descarga
		 */
		boolean result = false;
		 File localFile = new File(localFileName);
	     if (localFile.exists()) {
	    	 System.out.println("Ya existe un archivo con ese nombre en esta máquina");
	     	return;
	     }
	     else {
	    	 result = nfConnector.download(targetFileHash,localFile);
	     }
		if (result) {
			System.out.println("Archivo descargado con éxito");
		}
		else {
			System.out.println("El archivo no se pudo descargar");
		}

	}

	protected void browserClose() throws IOException {
		/*
		 * TODO: Cerrar el explorador de ficheros remoto (informar al servidor de que se
		 * va a desconectar)
		 */
		nfConnector.getSocket().close();
		
	}

	protected void browserQueryFiles() throws IOException {
		/*
		 * TODO: Crear un objeto NFConnector y guardarlo el atributo correspondiente
		 * para ser usado por otros métodos de esta clase mientras se está en una sesión
		 * del explorador de ficheros remoto.
		 * 
		 */
		boolean result = nfConnector.queryFiles();
		if (!result) {
			System.out.println("Couldn't query files from this server");
		}
	}
}
