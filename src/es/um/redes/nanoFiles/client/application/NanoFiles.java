package es.um.redes.nanoFiles.client.application;

import java.io.IOException;

import es.um.redes.nanoFiles.client.shell.NFCommands;
import es.um.redes.nanoFiles.util.FileDatabase;

public class NanoFiles {

	public static final String DEFAULT_SHARED_DIRNAME = "nf-shared";
	public static String sharedDirname = DEFAULT_SHARED_DIRNAME;
	public static FileDatabase db;


	public static void main(String[] args) throws IOException {
		// Comprobamos los argumentos
		if (args.length > 1) {
			System.out.println("Usage: java -jar NanoFiles.jar [<local_shared_directory>]");
			return;
		}
		else if (args.length == 1) {
			// Establecemos el directorio compartido especificado 
			sharedDirname = args[0];
		}

		db = new FileDatabase(sharedDirname);

		// Creamos el controlador que aceptará y procesará los comandos
		NFController controller = new NFController();
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        @Override
	        public void run() {
	        	controller.setCurrentCommand(NFCommands.COM_QUIT);;
	        }
	    });
		// Entramos en el bucle para pedirle al controlador que procese comandos del
		// shell hasta que el usuario quiera salir de la aplicación.
		do {
			controller.readGeneralCommandFromShell();
			controller.processCommand();
		} while (controller.shouldQuit() == false);
		System.out.println("Bye.");
	}
}
