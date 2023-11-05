package es.um.redes.nanoFiles.directory.message;

import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;


public class DirMessage {

	public static final int PACKET_MAX_SIZE = 65536;

	public static final byte OPCODE_SIZE_BYTES = 1;

	private byte opcode;

	private String userName;
	private int longitud;
	private String field;
//El assert sirve para que si el valor no es el que pedimos salte excepcion
	public DirMessage(byte operation) { //CONSTRUCTOR DEL TIPO DE MENSAJE DE CONTROL
		assert (operation == DirMessageOps.OPCODE_LOGIN);
		opcode = operation;
		assert (operation == DirMessageOps.OPCODE_LOGIN_OK);
		opcode = operation;
		assert (operation == DirMessageOps.OPCODE_GETUSERS);
		opcode = operation;
		assert (operation == DirMessageOps.OPCODE_GETFILES);
		opcode = operation;
		assert (operation == DirMessageOps.OPCODE_SERVE_FILES_OK);
		opcode = operation;
		assert (operation == DirMessageOps.OPCODE_SERVE_FILES_FAIL);
		opcode = operation;
		assert (operation == DirMessageOps.OPCODE_LOOKUP_USERNAME_NOTFOUND);
		opcode = operation;
	}


	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros tipos de datos
	 * 
	 */
	public DirMessage(byte operation, String parameter) { //CONSTRUCTOR DEL TIPO DE MENSAJE ONEPARAMETER
		/*
		 * TODO: Añadir al aserto el resto de opcodes de mensajes con los mismos campos
		 * (utilizan el mismo constructor)
		 */
		assert (opcode == DirMessageOps.OPCODE_REGISTER_USERNAME);
		opcode = operation;
		userName = parameter;
		assert (opcode == DirMessageOps.OPCODE_REGISTER_USERNAME_OK);
		opcode = operation;
		this.field = parameter;
		assert (opcode == DirMessageOps.OPCODE_QUIT);
		opcode = operation;
		field = parameter;
		
	}
	
	public DirMessage(byte operation, int longitud, String field) {
		assert (opcode == DirMessageOps.OPCODE_SERVE_FILES);
		opcode = operation;
		this.longitud = longitud;
		this.field = field;
		assert (opcode == DirMessageOps.OPCODE_LOOKUP_USERNAME);
		opcode = operation;
		this.longitud = longitud;
		this.field = field;
		assert (opcode == DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND);
		opcode = operation;
		this.longitud = longitud;
		this.field = field;
		assert (opcode == DirMessageOps.OPCODE_USERLIST);
		opcode = operation;
		this.longitud = longitud;
		this.field = field;
		assert (opcode == DirMessageOps.OPCODE_FILELIST);
		opcode = operation;
		this.longitud = longitud;
		this.field = field;
		
	}
	/**
	 * Método para obtener el tipo de mensaje (opcode)
	 * @return
	 */
	public byte getOpcode() {
		return opcode;
	}

	public String getUserName() {
		if (userName == null) {
			System.err.println(
					"PANIC: DirMessage.getUserName called but 'userName' field is not defined for messages of type "
							+ DirMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return userName;
	}
	
	public String getField() {
		return field;
	}

	public int getLongitud() {
		return longitud;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El
	 * @return
	 */
	public static DirMessage buildMessageFromReceivedData(byte[] data) {
		/*
		 * TODO: En función del tipo de mensaje, parsear el resto de campos para extraer
		 * los valores y llamar al constructor para crear un objeto DirMessage que
		 * contenga en sus atributos toda la información del mensaje
		 */
		DirMessage receivedMessage = null;
		ByteBuffer bb = ByteBuffer.wrap(data);
		byte receiveOpcode = bb.get();
		int longitud;
		byte[] nickbytes;
		switch(receiveOpcode) {
		case DirMessageOps.OPCODE_LOGIN:
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_LOGIN);
			break;
		case DirMessageOps.OPCODE_REGISTER_USERNAME:
			longitud = bb.getInt();
			nickbytes = new byte [longitud];
			bb.get(nickbytes);
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_REGISTER_USERNAME, new String(nickbytes));
			break;
		case DirMessageOps.OPCODE_LOOKUP_USERNAME:
			longitud = bb.getInt();
			nickbytes = new byte [longitud];
			bb.get(nickbytes);
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_LOOKUP_USERNAME, longitud, new String(nickbytes));
			break;
		case DirMessageOps.OPCODE_GETUSERS:
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_GETUSERS);
			break;
		case DirMessageOps.OPCODE_GETFILES:
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_GETFILES);
			break;
		case DirMessageOps.OPCODE_SERVE_FILES:
			longitud = bb.getInt();
			byte[] messagebytes = new byte [longitud];
			bb.get(messagebytes);
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_SERVE_FILES, longitud, new String(messagebytes));
			break;
		case DirMessageOps.OPCODE_QUIT:
			longitud = bb.getInt();
			nickbytes = new byte [longitud];
			bb.get(nickbytes);
			receivedMessage = new DirMessage(DirMessageOps.OPCODE_QUIT, new String(nickbytes));
			break;
		default:
		}
		return receivedMessage;
	}

	/**
	 * Método para construir una solicitud de ingreso en el directorio
	 * 
	 * @return El array de bytes con el mensaje de solicitud de login
	 */
	public static byte[] buildLoginRequestMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_LOGIN);
		return bb.array();
	}
	
	public static byte[] buildRegisterRequestMessage(String nick) {
		int longitud = nick.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES+4+longitud);
		bb.put(DirMessageOps.OPCODE_REGISTER_USERNAME);
		bb.putInt(longitud);
		bb.put(nick.getBytes());
		return bb.array();
	}
	
	public static byte[] buildUserListRequestMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_GETUSERS);
		return bb.array();	
	}

	public static byte[] buildFileListRequestMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_GETFILES);
		return bb.array();
	}
	
	public static byte[] buildLookUpUsernameRequestMessage(String nick) {
		int longitud = nick.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES+4+longitud);
		bb.put(DirMessageOps.OPCODE_LOOKUP_USERNAME);
		bb.putInt(longitud);
		bb.put(nick.getBytes());
		return bb.array();
	}
	
	public static byte[] buildServeFilesRequestMessage(int port, String nick, FileInfo[] filearray) {
		//opcodelongitudpuerto:nick:hash1:nombre1:tamaño1:ruta1:...:hashN:nombreN:tamañoN:rutaN
		String finalstring = nick;
		for (int i=0;i<filearray.length;i++) {
			//Todos los campos de cada elemento del FileInfo
			finalstring = finalstring + ":" + filearray[i].toEncodedString();
			//El primer +4 es para el entero del tamaño y el segundo +4 para los caracteres separadores
		}
		int longitud = finalstring.length()+4;
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES+4+longitud);
		bb.put(DirMessageOps.OPCODE_SERVE_FILES);
		bb.putInt(longitud);
		bb.putInt(port);
		bb.put(finalstring.getBytes());
		return bb.array();
	}
	
	public static byte[] buildDisconnectRequestMessage(String nick) {
		int longitud = nick.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES+4+longitud);
		bb.put(DirMessageOps.OPCODE_QUIT);
		bb.putInt(longitud);
		bb.put(nick.getBytes());
		return bb.array();
	}
	/**
	 * Método para construir una respuesta al ingreso del peer en el directorio
	 * 
	 * @param numServers El número de peer registrados como servidor en el
	 *                   directorio
	 * @return El array de bytes con el mensaje de solicitud de login
	 */
	public static byte[] buildLoginOKResponseMessage(int numServers) {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES);
		bb.put(DirMessageOps.OPCODE_LOGIN_OK);
		bb.putInt(numServers);
		return bb.array();
	}
	
	public static byte[] buildRegisterOKResponseMessage(String nick) {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + nick.length());
		bb.put(DirMessageOps.OPCODE_REGISTER_USERNAME_OK);
		bb.put(nick.getBytes());
		return bb.array();
		
	}
	
	public static byte[] buildRegisterFAILResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_REGISTER_USERNAME_FAIL);
		return bb.array();
	}
	
	public static byte[] buildUserListResponse(byte[] userlist) {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES + userlist.length);
		bb.put(DirMessageOps.OPCODE_USERLIST);
		bb.putInt(userlist.length);
		bb.put(userlist);
		return bb.array();
	}
	
	public static byte[] buildFileListResponse(byte[] filelist) {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES + filelist.length);
		bb.put(DirMessageOps.OPCODE_FILELIST);
		bb.putInt(filelist.length);
		bb.put(filelist);
		return bb.array();
	}
	
	public static byte[] buildLookUpUsernameFoundResponseMessage(String ipport) {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES + ipport.length());
		bb.put(DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND);
		bb.putInt(ipport.getBytes().length);
		bb.put(ipport.getBytes());
		return bb.array();
	}
	
	public static byte[] buildLookUpUsernameNotFoundResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_LOOKUP_USERNAME_NOTFOUND);
		return bb.array();
	}
	
	public static byte[] buildServeFilesOKResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_SERVE_FILES_OK);
		return bb.array();
	}
	
	public static byte[] buildServeFilesFailResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_SERVE_FILES_FAIL);
		return bb.array();
	}

	/**
	 * Método que procesa la respuesta a una solicitud de login
	 * 
	 * @param data El mensaje de respuesta recibido del directorio
	 * @return El número de peer servidores registrados en el directorio en el
	 *         momento del login, o -1 si el login en el servidor ha fallado
	 */
	public static int processLoginResponse(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get();
		if (opcode == DirMessageOps.OPCODE_LOGIN_OK) {
			return buf.getInt(); // Return number of available file servers
		} else {
			return -1;
		}
	}
	
	public static boolean processRegisterResponse(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get();
		if (opcode == DirMessageOps.OPCODE_REGISTER_USERNAME_OK) {
			return true;
		}
		else if(opcode == DirMessageOps.OPCODE_REGISTER_USERNAME_FAIL) {
			return false;
		}
		
		return false;
	}
	
	public static String processLookUpUsernameResponse(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get();
		if (opcode == DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND) {
			int longitud = buf.getInt();
			byte[] aux = new byte[longitud]; 
			buf.get(aux);
			String ipport = new String(aux);
			return ipport;
		}
		else if(opcode == DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND) {
			return "notfound";
		}
		return "notfound";
	}
	
	public static String processUserListResponse(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get(); //Primero recuperamos el opcode
		int longitud = buf.getInt(); //Ahora recuperamos la longitud del campo para saber cuándo parar de leer
		char charRet; //Ahora inicializamos el caracter donde iremos almacenando los caracteres leídos en cada iteración
		String mensajenicks = "Usuarios registrados\n---------------------\n\\\n"; //Esta cadena es la que se devolverá
		String nombreiterado = ""; //Este String irá concantenando los caracteres leídos de los nicks que vayan iterando
		for(int i = 0; i<longitud;i++) { //Recorremos el campo con el límite de la longitud
			charRet = (char) buf.get(); //Leemos un carácter del buffer de bytes
			if (charRet == ":".charAt(0)) { //Si el carácter es ":" es que hemos terminado de leer un nick
				mensajenicks = mensajenicks + " |-> "+nombreiterado+"\n"; //Por lo que este se concatena al mensaje
				nombreiterado = ""; //Y se resetea la variable donde vamos almacenando cada nick
			}
			else {
				nombreiterado = nombreiterado + charRet; //Si el carácter leído no es ":" es un carácter perteneciente a un nick, por lo que lo concatenamos
			}
					
		}
		return mensajenicks; //Devolvemos el mensaje ya construido
	}
	
	public static String processFileListResponse(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get(); //Primero recuperamos el opcode
		int longitud = buf.getInt(); //Ahora recuperamos la longitud del campo para saber cuándo parar de leer
		char charRet; //Ahora inicializamos el caracter donde iremos almacenando los caracteres leídos en cada iteración
		String mensajeficheros = "Archivos disponibles\n---------------------\n\\\n"; //Esta cadena es la que se devolverá
		String ficheroiterado = ""; //Este String irá concantenando los caracteres leídos de los nicks que vayan iterando
		for(int i = 0; i<longitud;i++) { //Recorremos el campo con el límite de la longitud
			charRet = (char) buf.get(); //Leemos un carácter del buffer de bytes
			if (charRet == ";".charAt(0)) { //Si el carácter es ":" es que hemos terminado de leer un nick
				mensajeficheros = mensajeficheros + " |-> "+ficheroiterado+"\n"; //Por lo que este se concatena al mensaje
				ficheroiterado = ""; //Y se resetea la variable donde vamos almacenando cada nick
			}
			else {
				ficheroiterado = ficheroiterado+ charRet; //Si el carácter leído no es ":" es un carácter perteneciente a un nick, por lo que lo concatenamos
			}
					
		}
		return mensajeficheros; //Devolvemos el mensaje ya construido
	}
	
	public static boolean processServeFilesResponse(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get(); //Primero recuperamos el opcode
		if (opcode ==DirMessageOps.OPCODE_SERVE_FILES_OK) {
			return true;
		}
		else {
			return false;
		}
	}
}
