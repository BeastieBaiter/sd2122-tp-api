package tp1.server.resources.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tp1.api.service.rest.RestFiles;

public class FilesResource implements RestFiles{
	
	private final Map<String, byte[]> files = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFile(String fileId, String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getFile(String fileId, String token) {
		// TODO Auto-generated method stub
		return null;
	}

}
