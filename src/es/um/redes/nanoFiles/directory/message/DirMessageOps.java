package es.um.redes.nanoFiles.directory.message;

import java.util.Map;
import java.util.TreeMap;

public class DirMessageOps {

	private static final byte OP_INVALID_CODE = -1;
	public static final byte OPCODE_QUIT = 0;
	public static final byte OPCODE_LOGIN = 1;
	public static final byte OPCODE_LOGIN_OK = 2;
	public static final byte OPCODE_LOOKUP_USERNAME = 3;
	public static final byte OPCODE_LOOKUP_USERNAME_FOUND = 4;
	public static final byte OPCODE_LOOKUP_USERNAME_NOTFOUND = 5;
	public static final byte OPCODE_REGISTER_USERNAME = 6;
	public static final byte OPCODE_REGISTER_USERNAME_OK = 7;
	public static final byte OPCODE_REGISTER_USERNAME_FAIL = 8;
	public static final byte OPCODE_SERVE_FILES = 9;
	public static final byte OPCODE_SERVE_FILES_OK = 10;
	public static final byte OPCODE_SERVE_FILES_FAIL = 11;
	public static final byte OPCODE_GETUSERS = 12;
	public static final byte OPCODE_USERLIST = 13;
	public static final byte OPCODE_GETFILES = 14;
	public static final byte OPCODE_FILELIST = 15;

	private static final Byte[] _valid_opcodes = { 
			OPCODE_LOGIN, 
			OPCODE_LOGIN_OK, 
			OPCODE_LOOKUP_USERNAME,
			OPCODE_LOOKUP_USERNAME_FOUND, 
			OPCODE_LOOKUP_USERNAME_NOTFOUND,
			OPCODE_REGISTER_USERNAME,
			OPCODE_REGISTER_USERNAME_OK, 
			OPCODE_REGISTER_USERNAME_FAIL, 
			OPCODE_SERVE_FILES, 
			OPCODE_SERVE_FILES_OK,
			OPCODE_SERVE_FILES_FAIL, 
			OPCODE_GETUSERS,
			OPCODE_USERLIST, 
			OPCODE_GETFILES, 
			OPCODE_FILELIST };
	private static final String[] _valid_operations_str = { 
			"SIGNIN", 
			"SIGNIN_OK", 
			"LOOKUP_USERNAME",
			"LOOKUP_USERNAME_FOUND", 
			"LOOKUP_USERNAME_NOTFOUND", 
			"REGISTER_USERNAME", 
			"REGISTER_USERNAME_OK",
			"REGISTER_USERNAME_FAIL", 
			"SERVE_FILES", 
			"SERVE_FILES_OK",
			"SERVE_FILES_FAIL", 
			"GET_USERLIST", 
			"USERLIST",
			"GET_FILELIST", 
			"FILELIST" };

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OP_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
