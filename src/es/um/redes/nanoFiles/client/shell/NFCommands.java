package es.um.redes.nanoFiles.client.shell;

public class NFCommands {
	/**
	 * Códigos para todos los comandos soportados por el shell
	 */
	public static final byte COM_INVALID = 0;
	public static final byte COM_QUIT = 1;
	public static final byte COM_LOGIN = 2;
	public static final byte COM_USERLIST = 3;
	public static final byte COM_FILELIST = 4;
	public static final byte COM_REGISTER = 5;
	public static final byte COM_MYFILES = 6;
	public static final byte COM_FGSERVE = 10;
	public static final byte COM_BGSERVE = 11;
	public static final byte COM_BROWSE = 20;
	public static final byte COM_QUERYFILES = 22;
	public static final byte COM_DOWNLOAD = 23;
	public static final byte COM_UPLOAD = 25;
	public static final byte COM_CLOSE = 29;
	public static final byte COM_HELP = 50;
	public static final byte COM_SOCKET_IN = 100;


	
	/**
	 * Códigos de los comandos válidos que puede
	 * introducir el usuario del shell. El orden
	 * es importante para relacionarlos con la cadena
	 * que debe introducir el usuario y con la ayuda
	 */
	private static final Byte[] _valid_user_commands = { 
		COM_QUIT,
		COM_LOGIN,
		COM_USERLIST,
		COM_FILELIST,
		COM_REGISTER,
		COM_MYFILES,
		COM_FGSERVE,
		COM_BGSERVE,
		COM_BROWSE,
		COM_QUERYFILES,
		COM_DOWNLOAD,
		COM_UPLOAD,
		COM_CLOSE,
		COM_HELP,
		COM_SOCKET_IN
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_user_commands_str = {
			"quit",
			"login",
			"userlist",	
			"filelist",
			"register",	
			"myfiles",
			"fgserve",
			"bgserve",
			"browse",	
			"queryfiles",
			"download",
			"upload",
			"close",
			"help",
			"fgstop"
		};

	/**
	 * Mensaje de ayuda para cada orden
	 */
	private static final String[] _valid_user_commands_help = {
			"to quit the application",
			"to sign into <directory> ",
			"to show list of users registered in the directory",
			"to show list of files tracked by the directory",
			"to register with directory as user identified by <nickname>",
			"to show contents of local folder (files that may be served)",
			"to begin serving shared files on <port> in foreground (blocking)",
			"to begin serving shared files on <port> in background (non-blocking)",
			"to enter browser in order to query/download files shared by <user>/<IP:port>",
			"(browser-mode) to query list of files shared by this user",
			"(browser-mode) to download the file identified by <hash>",
			"(browser-mode) to upload a local file given by <filename>",
			"(browser-mode) to close a browser session",
			"shows this information",
			"only when receiving a packet from the server"
			};

	/**
	 * Transforma una cadena introducida en el código de comando correspondiente
	 */
	public static byte stringToCommand(String comStr) {
		//Busca entre los comandos si es válido y devuelve su código
		for (int i = 0;
		i < _valid_user_commands_str.length; i++) {
			if (_valid_user_commands_str[i].equalsIgnoreCase(comStr)) {
				return _valid_user_commands[i];
			}
		}
		//Si no se corresponde con ninguna cadena entonces devuelve el código de comando no válido
		return COM_INVALID;
	}

	public static String commandToString(byte command) {
		for (int i = 0;
		i < _valid_user_commands.length; i++) {
			if (_valid_user_commands[i] == command) {
				return _valid_user_commands_str[i];
			}
		}
		return null;
	}

	/**
	 * Imprime la lista de comandos y la ayuda de cada uno
	 */
	public static void printCommandsHelp() {
		System.out.println("List of commands:");
		for (int i = 0; i < _valid_user_commands_str.length-1; i++) {
			System.out.println(String.format("%1$15s", _valid_user_commands_str[i]) + " -- "
					+ _valid_user_commands_help[i]);
		}		
	}
}	

