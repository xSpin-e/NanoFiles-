package es.um.redes.nanoFiles.client.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import es.um.redes.nanoFiles.client.comm.NFConnector;

public class NFShell {
	/**
	 * Scanner para leer comandos de usuario de la entrada estándar
	 */
	private Scanner reader;

	byte command = NFCommands.COM_INVALID;
	String[] commandArgs = new String[0];

	boolean enableComSocketIn = true;
	private boolean skipValidateArgs;

	public NFShell() {
		reader = new Scanner(System.in);

		System.out.println("NanoFiles shell");
		System.out.println("For help, type 'help'");
	}

	// devuelve el comando introducido por el usuario
	public byte getCommand() {
		return command;
	}

	// Devuelve los parámetros proporcionados por el usuario para el comando actual
	public String[] getCommandArguments() {
		return commandArgs;
	}

	// Espera hasta obtener un comando válido entre los comandos existentes
	public void readGeneralCommand() {
		boolean validArgs;
		do {
			commandArgs = readGeneralCommandFromStdIn();
			// si el comando tiene parámetros hay que validarlos
			validArgs = validateCommandArguments(commandArgs);
		} while (!validArgs);
	}

	// Usa la entrada estándar para leer comandos y procesarlos
	private String[] readGeneralCommandFromStdIn() {
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		while (true) {
			System.out.print("(nanoFiles) ");
			// obtenemos la línea tecleada por el usuario
			String input = reader.nextLine();
			StringTokenizer st = new StringTokenizer(input);
			// si no hay ni comando entonces volvemos a empezar
			if (st.hasMoreTokens() == false) {
				continue;
			}
			// traducimos la cadena del usuario en el código de comando correspondiente
			command = NFCommands.stringToCommand(st.nextToken());
			skipValidateArgs = false;
			// Dependiendo del comando...
			switch (command) {
			case NFCommands.COM_INVALID:
				// El comando no es válido
				System.out.println("Invalid command");
				continue;
			case NFCommands.COM_HELP:
				// Mostramos la ayuda
				NFCommands.printCommandsHelp();
				continue;
			case NFCommands.COM_QUIT:
			case NFCommands.COM_USERLIST:
			case NFCommands.COM_FILELIST:
			case NFCommands.COM_MYFILES:
				// Estos comandos son válidos sin parámetros
				break;
			case NFCommands.COM_BROWSE:
			case NFCommands.COM_LOGIN:
			case NFCommands.COM_REGISTER:
			case NFCommands.COM_FGSERVE:
			case NFCommands.COM_BGSERVE:
				// Estos requieren un parámetro
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				break;
			default:
				skipValidateArgs = true;
				System.out.println("That command is only valid if you are in the browser");
				;
			}
			break;
		}
		return vargs.toArray(args);
	}

	// Espera a que haya un comando válido de sala o llegue un mensaje entrante
	public void readBrowserCommand(NFConnector ngclient) {
		boolean validArgs;
		do {
			commandArgs = readBrowserCommandFromStdIn(ngclient);
			// si hay parámetros se validan
			validArgs = validateCommandArguments(commandArgs);
		} while (!validArgs);
	}

	// Utiliza la entrada estándar para leer comandos y comprueba si hay datos en el
	// flujo de entrada del conector
	private String[] readBrowserCommandFromStdIn(NFConnector nfclient) {
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		/*
		 * Por ahora, no desactivamos la comprobación de si hay datos disponibles en el
		 * socket (usado para reaccionar como si se tratase de un comando). Es necesario
		 * activar
		 */
		if (enableComSocketIn) {
			assert (nfclient != null);

		} else {
			assert (nfclient == null);
		}
		while (true) {
			System.out.print("(nanoFiles-browser) ");
			// Utilizamos un BufferedReader en lugar de un Scanner porque no podemos
			// bloquear la entrada
			BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
			boolean blocked = true;
			String input = "";
			// Estamos esperando comando o mensaje entrante
			while (blocked) {
				try {
					if (enableComSocketIn && nfclient.isDataAvailable()) {
						// Si el flujo de entrada tiene datos entonces el comando actual es SOCKET_IN y
						// debemos salir
						System.out.println("* Message received from server...");
						command = NFCommands.COM_SOCKET_IN;
						input = nfclient.getDis().readUTF();
						System.out.println(input);
						if (input.equals("fgstop")) {
							blocked = false;
						}
						command = NFCommands.COM_SOCKET_IN;
					} else
					// Analizamos si hay datos en la entrada estándar (el usuario tecleó INTRO)
					if (standardInput.ready()) {
						input = standardInput.readLine();
						blocked = false;
					}
					// Puesto que estamos sondeando las dos entradas de forma continua, esperamos
					// para evitar un consumo alto de CPU
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (IOException | InterruptedException e) {
					command = NFCommands.COM_INVALID;
					return null;
				}
			}
			// Si el usuario tecleó un comando entonces procedemos de igual forma que
			// hicimos antes para los comandos generales
			StringTokenizer st = new StringTokenizer(input);
			if (st.hasMoreTokens() == false) {
				continue;
			}
			command = NFCommands.stringToCommand(st.nextToken());
			skipValidateArgs = false;
			switch (command) {
			case NFCommands.COM_SOCKET_IN:
				break;
			case NFCommands.COM_INVALID:
				System.out.println("Invalid command (" + input + ")");
				continue;
			case NFCommands.COM_CLOSE:
			case NFCommands.COM_QUERYFILES:
				break;
			case NFCommands.COM_UPLOAD:
			case NFCommands.COM_DOWNLOAD:
				// Requiere parámetros
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				break;
			case NFCommands.COM_HELP:
				NFCommands.printCommandsHelp();
				continue;
			default:
				skipValidateArgs = true;
				System.out.println("That command is only valid outside the file browser");
			}
			break;
		}
		return vargs.toArray(args);
	}

	// Algunos comandos requieren un parámetro
	// Este método comprueba si se proporciona parámetro para los comandos
	private boolean validateCommandArguments(String[] args) {
		if (skipValidateArgs)
			return false;
		switch (this.command) {
		// signin requiere el parámetro <directory>
		case NFCommands.COM_LOGIN:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: " + NFCommands.commandToString(command) + " <directory_server>");
				return false;
			}
			break;
		// browse requiere el parámetro <remote_user>
		case NFCommands.COM_BROWSE:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: " + NFCommands.commandToString(command) + " <remote_user>");
				return false;
			}
			break;
		// register requiere el parámetro <nickname>
		case NFCommands.COM_REGISTER:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use:" + NFCommands.commandToString(command) + " <nickname>");
				return false;
			}
			break;
		// serve requiere el parámetro <port>
		case NFCommands.COM_FGSERVE:
		case NFCommands.COM_BGSERVE:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use:" + NFCommands.commandToString(command) + " <port>");
				return false;
			}
			break;
		// serve requiere el parámetro <port>
		case NFCommands.COM_DOWNLOAD:
			if (args.length != 2) {
				System.out.println(
						"Correct use:" + NFCommands.commandToString(command) + " <file_hash> <local_filename>");
				return false;
			}
			break;
		case NFCommands.COM_UPLOAD:
			if (args.length != 1) {
				System.out.println("Correct use:" + NFCommands.commandToString(command) + " <local_filename>");
				return false;
			}
			break;
		default:
		}
		// El resto no requieren parámetro
		return true;
	}
}
