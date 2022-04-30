package tp1.server.resources.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.java.JavaDirectory;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
import tp1.clients.rest.RestFilesClient;
import tp1.clients.rest.RestUsersClient;
import tp1.discovery.Discovery;
import tp1.server.rest.DirectoryServer;

@Singleton
public class DirectoryResource implements RestDirectory{
	private Discovery discovery = DirectoryServer.discovery;
	
	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private JavaDirectory impl = new JavaDirectory();
	
	public DirectoryResource() {}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
		Log.info("writeFile : filename = " + filename + "; data = " + data + "; user = " + userId + "; pwd = " + password);
		try {
			var resultUser = getUser(userId, password);
			if (!resultUser.isOK()) {
				throw new WebApplicationException(convertToStatus(resultUser.error()));
			}
			FileInfo fileInfo = impl.writeFile(filename, data, userId, password);
			
			RestFilesClient rfc = new RestFilesClient(URI.create(
									fileInfo.getFileURL().replace(RestFiles.PATH + "/" + userId + "_" + filename, "")));
			rfc.writeFile(userId + "_" + filename, data, "");
			return fileInfo;
		} catch (WebApplicationException e) {
			throw e;
		}
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {
		Log.info("deleteFile : filename = " + filename + "; user = " + userId + "; pwd = " + password);
		try {
			var resultUser = getUser(userId, password);
			if (!resultUser.isOK()) {
				throw new WebApplicationException(convertToStatus(resultUser.error()));
			}
			
			FileInfo fileInfo = impl.getFileInfo(userId + "_" + filename);
			
			if (fileInfo == null) {
				throw new WebApplicationException(Status.NOT_FOUND);
			}
			
			URI fileUri = URI.create(fileInfo.getFileURL().replace(RestFiles.PATH + "/" + userId + "_" + filename, ""));
			
			RestFilesClient rfc = new RestFilesClient(fileUri);

			rfc.deleteFile(userId + "_" + filename, "");
			
			impl.deleteFile(filename, userId, password);
		} catch (WebApplicationException e) {
			throw e;
		}
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) {
		Log.info("shareFile : filename = " + filename + "; user = " + userId 
							+ "; userIdShare= " + userIdShare +"; pwd = " + password);
		
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultUser.error()));
		}
		
		var resultHasUser = hasUser(userIdShare);
		if (!resultHasUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultHasUser.error()));
		}
		
		var result = impl.shareFile(filename, userId, userIdShare, password);
		if (!result.isOK()) {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) {
		Log.info("unshareFile : filename = " + filename + "; user = " + userId 
								+"; userIdShare= " + userIdShare +"; pwd = " + password);
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultUser.error()));
		}
		
		var resultHasUser = hasUser(userIdShare);
		if (!resultHasUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultHasUser.error()));
		}
		
		var result = impl.unshareFile(filename, userId, userIdShare, password);
		if (!result.isOK()) {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
		
	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password) {
		Log.info("getFile : filename = " + filename + "; user = " + userId 
				+" accUserId= " + accUserId +"; pwd = " + password);
		try {
			var resultUser = getUser(accUserId, password);
			if (!resultUser.isOK()) {
				throw new WebApplicationException(convertToStatus(resultUser.error()));
			}

			var resultHasUser = hasUser(userId);
			if (!resultHasUser.isOK()) {
				throw new WebApplicationException(convertToStatus(resultHasUser.error()));

			}
			
			var result = impl.getFile(filename, userId, accUserId, password);
			if (!result.isOK()) {
				throw new WebApplicationException(convertToStatus(result.error()));
			}
			
			FileInfo fileInfo = impl.getFileInfo(userId + "_" + filename);
			
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(fileInfo.getFileURL())).build());
		} catch (WebApplicationException e) {
			throw e;
		}
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) {
		Log.info("lsFile : user = " + userId +"; pwd = " + password);

		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultUser.error()));
		}
		
		var result = impl.lsFile(userId, password);
		if (result.isOK()) {
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}
	
	private URI[] getUris(String service) {
		URI[] uris = null;
		try {
			while(uris == null || uris.length == 0) {
				uris = discovery.knownUrisOf(service);
			}
		} catch (Exception e) {}
		return uris;
	}
	
	private Result<User> getUser(String userId, String password) {
		RestUsersClient ruc = new RestUsersClient(getUris("users")[0]);
		return ruc.getUser(userId, password);
	}
	
	private Result<Boolean> hasUser(String userId) {
		RestUsersClient ruc = new RestUsersClient(getUris("users")[0]);
		return ruc.hasUser(userId);
	}
	
	private Status convertToStatus(ErrorCode error) {
		
		switch (error) {
			case OK:
				return Status.OK;
			case BAD_REQUEST:
				return Status.BAD_REQUEST;
			case FORBIDDEN:
				return Status.FORBIDDEN;
			case CONFLICT:
				return Status.CONFLICT;
			case NOT_FOUND:
				return Status.NOT_FOUND;
			case INTERNAL_ERROR:
				return Status.INTERNAL_SERVER_ERROR;
			case NOT_IMPLEMENTED:
				return Status.NOT_IMPLEMENTED;
			default:
				break;
			}
			return null;
	}
}
