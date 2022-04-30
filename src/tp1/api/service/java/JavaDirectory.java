package tp1.api.service.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Singleton;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
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
		Set<String> sharedWith = new HashSet<String>();
		
		URI[] fileUris = getUris("files");
		URI fileUri = chooseFileServer(fileUris);
		
		String fileUriString = "http://" + fileUri.getHost() + ":" + fileUri.getPort() + fileUri.getPath();
		
		FileInfo fileInfo;
		
		synchronized (this) {
			if (files.containsKey(userId + "_" + filename)) {
				fileInfo = files.get(userId + "_" + filename);
			}
			else {
				fileInfo = new FileInfo(userId, filename, 
						fileUriString + RestFiles.PATH + "/" + userId + "_" + filename, sharedWith);
				
				files.put(userId + "_" + filename, fileInfo);
			}

			fileServerCount.merge(fileUri, 1, (a, b) -> a + b);
		}

		return fileInfo;
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {
		synchronized (this) {
			FileInfo f = files.get(userId + "_" + filename);

			URI fileUri = URI.create(f.getFileURL().replace(RestFiles.PATH + "/" + userId + "_" + filename, ""));

			files.remove(userId + "_" + filename);

			fileServerCount.merge(fileUri, 1, (a, b) -> a - b);
		}
		
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		synchronized (this) {
			if (!files.containsKey(userId + "_" + filename)) {
				return Result.error(ErrorCode.NOT_FOUND);
			}

			FileInfo file = files.get(userId + "_" + filename);

			Set<String> sharedWith = file.getSharedWith();
			sharedWith.add(userIdShare);
			System.out.println("Share With " + sharedWith);
			file.setSharedWith(sharedWith);
		}
		
		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		synchronized (this) {
			if (!files.containsKey(userId + "_" + filename)) {
				return Result.error(ErrorCode.NOT_FOUND);
			}
			
			FileInfo file = files.get(userId + "_" + filename);
			Set<String> sharedWith = file.getSharedWith();
			sharedWith.remove(userIdShare);
			file.setSharedWith(sharedWith);
		}
		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		synchronized (this) {
			if (!files.containsKey(userId + "_" + filename)) {
				return Result.error(ErrorCode.NOT_FOUND);
			}

			FileInfo f = files.get(userId + "_" + filename);

			if (!f.getSharedWith().contains(accUserId) && !accUserId.equals(f.getOwner())) {
				return Result.error(ErrorCode.FORBIDDEN);
			}

			return Result.ok();
		}
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		List<FileInfo> userFiles = new ArrayList<>();
		
		synchronized (this) {
			for (Map.Entry<String, FileInfo> entry : files.entrySet()) {
				FileInfo val = entry.getValue();

				if (val.getOwner().equals(userId) || val.getSharedWith().contains(userId)) {
					userFiles.add(val);
				}

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
	
	public synchronized FileInfo getFileInfo(String fileId) {
		return files.get(fileId);
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
}
