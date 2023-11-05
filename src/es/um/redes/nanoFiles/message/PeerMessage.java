package es.um.redes.nanoFiles.message;

import es.um.redes.nanoFiles.client.application.NanoFiles;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases NFServerComm y NFConnector, y se
 * codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class PeerMessage {
	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation;
	private String oneField;
	private byte[] fileData;
	private FileInfo[] filesInfo;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public PeerMessage(String operation) {
		assert (operation == PeerMessageOps.OP_DOWNLOAD_FAIL);
		this.operation = operation;
		assert (operation == PeerMessageOps.OP_QUERYFILES);
		this.operation = operation;
	}

	public PeerMessage(String operation, String oneField) {
		assert (operation == PeerMessageOps.OP_DOWNLOAD);
		this.operation = operation;
		this.oneField = oneField;
		
	}

	public PeerMessage(String operation, byte[] fileData) {
		assert (operation == PeerMessageOps.OP_DOWNLOAD_OK);
		this.operation = operation;
		this.fileData = fileData;
	}

	public PeerMessage(String operation, FileInfo[] filesInfo) {
		assert (operation == PeerMessageOps.OP_SERVEDFILES);
		this.operation = operation;
		this.filesInfo= filesInfo;
	}

	public String getOperation() {
		return operation;
	}

	public String getFileHash() {
		return oneField;
	}

	public byte[] getFileData() {
		return fileData;
	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static PeerMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */
		String[] lineas = message.split(Character.toString(END_LINE));
		String[] parteslineaoperacion = lineas[0].split(Character.toString(DELIMITER));
		PeerMessage mensaje = null;
		String datos;
		if (parteslineaoperacion.length == 2) {
			String valoropcode = parteslineaoperacion[1];
			switch (valoropcode) {
			case PeerMessageOps.OP_DOWNLOAD:
				String filehash = lineas[1].split(Character.toString(DELIMITER))[1];
				mensaje = new PeerMessage(PeerMessageOps.OP_DOWNLOAD, filehash);
				break;
			case PeerMessageOps.OP_QUERYFILES:
				mensaje = new PeerMessage(PeerMessageOps.OP_QUERYFILES);
				break;
			case PeerMessageOps.OP_DOWNLOAD_OK:
				datos = message.split(Character.toString(END_LINE))[1];
				datos = datos.split(Character.toString(DELIMITER))[1];
				byte[] fileData = java.util.Base64.getDecoder().decode(datos);
				mensaje = new PeerMessage(PeerMessageOps.OP_DOWNLOAD_OK, fileData);
				break;
			case PeerMessageOps.OP_DOWNLOAD_FAIL:
				mensaje = new PeerMessage(PeerMessageOps.OP_DOWNLOAD_FAIL);
				break;
			case PeerMessageOps.OP_SERVEDFILES:
				datos = "";
				for (int i=1;i<lineas.length;i++) {
					datos=datos+lineas[i].split(Character.toString(DELIMITER))[1]+":";
				}
				datos = datos.substring(0, datos.length()-1);
				mensaje = new PeerMessage(PeerMessageOps.OP_SERVEDFILES, datos);
				break;

			default:
			}
		}
		return mensaje;

	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toEncodedString() {
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("operation:"); //NO SABEMOS SI PONER ESPACIO TRAS EL : O NO
		switch (this.operation) {
		case PeerMessageOps.OP_DOWNLOAD:
			sb.append(PeerMessageOps.OP_DOWNLOAD + END_LINE + "filehash:" + this.oneField + END_LINE + END_LINE);
			break;
		case PeerMessageOps.OP_QUERYFILES:
			sb.append("queryfiles"+END_LINE+END_LINE);
			break;
		case PeerMessageOps.OP_DOWNLOAD_OK:
			sb.append("downloadok"+END_LINE);
			sb.append("filedata:");
			sb.append(java.util.Base64.getEncoder().encodeToString(fileData)+END_LINE+END_LINE);//SI O SÍ SE TIENEN QUE PONER ASÍ LOS END_LINE, SI NO DA ERROR LUEGO AL DECODIFICAR
			break;
		case PeerMessageOps.OP_DOWNLOAD_FAIL:
			sb.append("downloadfail"+END_LINE+END_LINE);
			break;
		case PeerMessageOps.OP_SERVEDFILES:
			sb.append("servedfiles"+END_LINE);
			for(int i=0;i<NanoFiles.db.getFiles().length;i++) {
				sb.append("file:"+NanoFiles.db.getFiles()[i].toEncodedString()+"\n");
			}
			sb.append("\n");
			break;
		default:
			break;
		}
		return sb.toString();
	}
}
