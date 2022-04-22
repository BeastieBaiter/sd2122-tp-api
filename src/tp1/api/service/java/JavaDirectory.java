package tp1.api.service.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
import tp1.clients.rest.RestFilesClient;
import tp1.clients.rest.RestUsersClient;
import tp1.discovery.Discovery;
import tp1.server.rest.DirectoryServer;

@Singleton
public class JavaDirectory implements Directory{
	
	private Discovery discovery = DirectoryServer.discovery;
	
	private final Map<String, FileInfo> files = new HashMap<>();
	
	private final Map<URI, Integer> fileServerCount = new HashMap<>();
	
	public JavaDirectory() {}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultUser.error()));
		}
		
		Set<String> sharedWith = new HashSet<String>();
		
		URI[] fileUris = getUris("files");
		URI fileUri = chooseFileServer(fileUris);
		
		String fileUriString = "http://" + fileUri.getHost() + ":" + fileUri.getPort() + fileUri.getPath();
		
		RestFilesClient rfc = new RestFilesClient(fileUri); 
		
		FileInfo fileInfo = new FileInfo(userId, filename, 
										fileUriString + RestFiles.PATH + "/" + userId + "_" + filename, sharedWith);
		files.put(userId + "_" + filename, fileInfo);
		
		rfc.writeFile(userId + "_" + filename, data, "");
		
		fileServerCount.merge(fileUri, 1, (a,b) -> a + b);
		
		return fileInfo;
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			throw new WebApplicationException(convertToStatus(resultUser.error()));
		}
		
		if (!files.containsKey(userId + "_" + filename)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		FileInfo f = files.get(userId + "_" + filename);
		
		URI fileUri = URI.create(f.getFileURL().replace( RestFiles.PATH + "/" + userId + "_" + filename, ""));
		
		RestFilesClient rfc = new RestFilesClient(fileUri); 
		
		files.remove(userId + "_" + filename);
		
		rfc.deleteFile(userId + "_" + filename, "");
		
		fileServerCount.merge(fileUri, 1, (a,b) -> a - b);
		
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			return Result.error(resultUser.error());
		}
		
		if (!files.containsKey(userId + "_" + filename)) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		var resultHasUser = hasUser(userIdShare);
		if (!resultHasUser.isOK()) {
			return Result.error(resultHasUser.error());
		}
		
		FileInfo file = files.get(userId + "_" + filename);
		Set<String> sharedWith = file.getSharedWith();
		sharedWith.add(userIdShare);
		System.out.println(sharedWith);
		file.setSharedWith(sharedWith);
		
		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			return Result.error(resultUser.error());
		}
		
		if (!files.containsKey(userId + "_" + filename)) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		var resultHasUser = hasUser(userIdShare);
		if (!resultHasUser.isOK()) {
			return Result.error(resultHasUser.error());
		}
		FileInfo file = files.get(userId + "_" + filename);
		Set<String> sharedWith = file.getSharedWith();
		sharedWith.remove(userIdShare);
		file.setSharedWith(sharedWith);
		
		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		var resultUser = getUser(accUserId, password);
		if (!resultUser.isOK()) {
			return Result.error(resultUser.error());
		}
		
		if (!files.containsKey(userId + "_" + filename)) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		var resultHasUser = hasUser(userId);
		if (!resultHasUser.isOK()) {
			return Result.error(resultHasUser.error());

		}
		
		FileInfo f = files.get(userId + "_" + filename);
		
		if (!f.getSharedWith().contains(accUserId) && !accUserId.equals(f.getOwner())) {
			return Result.error(ErrorCode.FORBIDDEN);
		}
		
		throw new WebApplicationException(Response.temporaryRedirect(URI.create(f.getFileURL())).build());
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		List<FileInfo> userFiles = new ArrayList<>();
		var resultUser = getUser(userId, password);
		if (!resultUser.isOK()) {
			return Result.error(resultUser.error());
		}
		
		for (Map.Entry<String, FileInfo> entry : files.entrySet()) {
			FileInfo val = entry.getValue();
			
			if (val.getOwner().equals(userId) || val.getSharedWith().contains(userId)) {
				userFiles.add(val);
			}
			
		}
		
		return Result.ok(userFiles);
	}
	
	private URI chooseFileServer(URI[] uris) {
		URI result = null;
		int best = Integer.MAX_VALUE;
		for(URI uri : uris) {
			int count;
			if (!fileServerCount.containsKey(uri))
				count = 0;
			else
				count = fileServerCount.get(uri);
			if (count < best) {
				best = count;
				result = uri;
			}
		}
		return result;
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
