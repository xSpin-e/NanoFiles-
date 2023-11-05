package es.um.redes.nanoFiles.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

import es.um.redes.nanoFiles.client.application.NanoFiles;
import es.um.redes.nanoFiles.message.PeerMessage;
import es.um.redes.nanoFiles.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDatabase;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static boolean serveFilesToClient(Socket socket) throws IOException {
		boolean whoclosed = false;
		boolean clientConnected = true;
		// Bucle para atender mensajes del cliente
		/*
		 * TODO: Crear dis/dos a partir del socket
		 */
		BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
		socket.setSoTimeout(1000);
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

		while (clientConnected) { // Bucle principal del servidor
			// TODO: Leer un mensaje de socket y convertirlo a un objeto PeerMessage
			/*
			 * TODO: Actuar en función del tipo de mensaje recibido. Se pueden crear métodos
			 * en esta clase, cada uno encargado de procesar/responder un tipo de petición.
			 */
				try {
					String message = dis.readUTF();
					PeerMessage messageFromPeer = PeerMessage.fromString(message);
					PeerMessage messageToPeer;
					String dataToPeer;
					switch (messageFromPeer.getOperation()) {
					case PeerMessageOps.OP_DOWNLOAD:
						String hash = messageFromPeer.getFileHash();
						FileInfo arrayfileinfo[] = NanoFiles.db.getFiles();
						String path = "pruebapath";
						for (int i = 0; i < arrayfileinfo.length; i++) {
							if (arrayfileinfo[i].fileHash.equals(hash)) {
								path = arrayfileinfo[i].filePath;
							}
						}
						try {
							File f = new File(path);
							FileInputStream fis = new FileInputStream(f);
							int longitud = fis.available();
							while (longitud != 0) {
								if (longitud % 1024 == longitud) {
									byte[] bytesToPeer = new byte[longitud];
									fis.read(bytesToPeer);
									messageToPeer = new PeerMessage(PeerMessageOps.OP_DOWNLOAD_OK, bytesToPeer);
									dataToPeer = messageToPeer.toEncodedString();
									dos.writeUTF(dataToPeer);
									longitud = 0;
								} else {
									byte[] bytesToPeer = new byte[1024];
									fis.read(bytesToPeer);
									messageToPeer = new PeerMessage(PeerMessageOps.OP_DOWNLOAD_OK, bytesToPeer);
									dataToPeer = messageToPeer.toEncodedString();
									dos.writeUTF(dataToPeer);
									longitud = longitud - 1024;
								}
							}
							fis.close();
							dataToPeer = "";
							dos.writeUTF(dataToPeer);
							System.out.println("Client succsefully downloaded a file");
						} catch (FileNotFoundException e) {
							messageToPeer = new PeerMessage(PeerMessageOps.OP_DOWNLOAD_FAIL);
							dataToPeer = messageToPeer.toEncodedString();
							dos.writeUTF(dataToPeer);
							System.out.println("Client failed downloading a file");
						}
						break;
					case PeerMessageOps.OP_QUERYFILES:
						messageToPeer = new PeerMessage(PeerMessageOps.OP_SERVEDFILES, NanoFiles.db.getFiles());
						dataToPeer = messageToPeer.toEncodedString();
						dos.writeUTF(dataToPeer);
						break;
					default:
					}
				} catch (SocketTimeoutException e) {
					if (standardInput.ready()) {
						if (standardInput.readLine().equals("fgstop")) {
							dos.writeUTF("fgstop");
							whoclosed = true;
							clientConnected = false;
						}
						
					}
				}catch (IOException e) {

					clientConnected = false;
				}
		}
		System.out.println("Client disconnected from " + socket.getInetAddress().toString() + ":" + socket.getPort());

		dis.close();
		dos.close();
		socket.close();
		return whoclosed;
	}
}
