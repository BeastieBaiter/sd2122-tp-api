package tp1.server.resources.rest;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.discovery.Discovery;
import tp1.server.rest.DirectoriesServer;

public class DirectoriesResource implements RestDirectory{
	
	private final Map<String, FileInfo> files = new HashMap<>();
	
	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private static Discovery discovery = DirectoriesServer.discovery;
	
	private String usersURI = null;
	
	public DirectoriesResource() {
	}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
		Log.info("writeFile : filename = " + filename + "; data = " + data + "; user = " + userId + "; pwd = " + password);
		if (!getUser(userId, password)) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		Set<String> sharedWith = new HashSet<String>();
		
		String[] filesUris = getFilesUris();
		String fileUri = filesUris[(int) (Math.random() * filesUris.length)];
		
		FileInfo fileInfo = new FileInfo(userId, filename, fileUri + "/" + userId + "/" + "filename", sharedWith);
		files.put(filename, fileInfo);
		
		/*Client client = ClientBuilder.newClient();
		WebTarget target = client.target(fileUri);
		
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(data, MediaType.APPLICATION_JSON));*/
		
		return fileInfo;
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {
		// TODO Auto-generated method stub
		Log.info("deleteFile : filename = " + filename + "; user = " + userId + "; pwd = " + password);
		if (!getUser(userId, password)) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (files.get(filename)==null) {
			Log.info("File does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		files.remove(filename);
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) {
		// TODO Auto-generated method stub
		Log.info("deleteFile : filename = " + filename + "; user = " + userId + "; pwd = " + password);
		if (!getUser(userId, password)) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (files.get(filename)==null) {
			Log.info("File does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (searchUser(userIdShare)) {
			Log.info("User to share does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		FileInfo file = files.get(filename);
		Set<String> sharedWith = file.getSharedWith();
		sharedWith.add(userIdShare);
		file.setSharedWith(sharedWith);
		
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) {
		// TODO Auto-generated method stub
		Log.info("deleteFile : filename = " + filename + "; user = " + userId + "; pwd = " + password);
		if (!getUser(userId, password)) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (files.get(filename)==null) {
			Log.info("File does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (searchUser(userIdShare)) {
			Log.info("User to share does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		FileInfo file = files.get(filename);
		Set<String> sharedWith = file.getSharedWith();
		sharedWith.remove(userIdShare);
		file.setSharedWith(sharedWith);
		
	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password) {
		// TODO Auto-generated method stub
		if (!getUser(accUserId, password)) {
			Log.info("User executing the operation does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (!searchUser(userId)) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		if (!files.containsKey(filename)) {
			Log.info("File does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		return null;
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean getUser(String userId, String password) {
		try {
			while (usersURI == null) {
				URI[] uris = discovery.knownUrisOf("users");
				usersURI = "http://" + uris[0].getHost() + uris[0].getPath();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(usersURI);
		
		Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		User user = r.readEntity(User.class);
		
		return user != null;
	}
	
	private boolean searchUser(String userId) {
		try {
			while (usersURI == null) {
				URI[] uris = discovery.knownUrisOf("users");
				usersURI = "http://" + uris[0].getHost() + uris[0].getPath();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(usersURI);
		
		Response r = target
				.queryParam(QUERY, userId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return r.readEntity(List.class).isEmpty();
	}
	
	private String[] getFilesUris() {
		try {
			URI[] uris = discovery.knownUrisOf("files");
			String[] filesUri = new String[uris.length];
			int counter = 0;
			for (URI uri : uris) {
				filesUri[counter++] = "http://" + uri.getHost() + uri.getPath();
			}
			return filesUri;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
	}
}
