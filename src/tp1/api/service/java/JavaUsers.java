package tp1.api.service.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Singleton;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
import tp1.api.service.util.Users;
import tp1.clients.rest.RestDirectoryClient;
import tp1.discovery.Discovery;
import tp1.server.rest.UsersServer;

@Singleton
public class JavaUsers implements Users{
	
	private Discovery discovery = UsersServer.discovery;
	
	private final Map<String,User> users = new HashMap<>();
	
	public JavaUsers() {}

	@Override
	public Result<String> createUser(User user) {
		// Check if user data is valid
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		// Check if userId already exists
		if( users.containsKey(user.getUserId())) {
			return Result.error(ErrorCode.CONFLICT);
		}

		//Add the user to the map of users
		users.put(user.getUserId(), user);
		return Result.ok(user.getUserId());
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		// Check if user is valid
		if(userId == null) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
				
		User user = users.get(userId);
				
		// Check if user exists 
		if( user == null ) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
					
		//Check if the password is correct
		if(!user.getPassword().equals(password)) {
			return Result.error(ErrorCode.FORBIDDEN);
		}
					
		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		if(userId == null || password == null) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		User oldUser = users.get(userId);
		
		// Check if user exists 
		if( oldUser == null ) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !oldUser.getPassword().equals( password)) {
			return Result.error(ErrorCode.FORBIDDEN);
		}
		oldUser.updateUser(user);
		return Result.ok(oldUser);	
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		if(userId == null) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null ) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			return Result.error(ErrorCode.FORBIDDEN);
		}
		
		RestDirectoryClient rdc = getRestDirectoryClient();
		
		Result<List<FileInfo>> resultListOfFiles = rdc.lsFile(userId, password);
		System.out.println(resultListOfFiles.toString());
		
		if (resultListOfFiles.isOK()) {
			Iterator<FileInfo> it = resultListOfFiles.value().iterator();
			while(it.hasNext()) {
				FileInfo f = it.next();
				if (f.getOwner().equals(userId)) {
					rdc.deleteFile(f.getFilename(), userId, password);
				}
				else {
					Set<String> sharedWith = f.getSharedWith();
					sharedWith.remove(userId);
					f.setSharedWith(sharedWith);
				}
			}
		}
		
		User deletedUser = user;
		users.remove(userId);
		return Result.ok(deletedUser);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		List<User> userSearch = new ArrayList<>();
		for (User u : users.values()) {
			if (u.getFullName().toUpperCase().contains(pattern.toUpperCase())) {
				String pwd = u.getPassword();
				u.setPassword("");
				userSearch.add(u);
				u.setPassword(pwd);
			}
		}
		return Result.ok(userSearch);
	}

	@Override
	public Result<Boolean> hasUser(String userId) {
		// Check if user is valid
		if(userId == null) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null ) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		return Result.ok(true);
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
	
	private RestDirectoryClient getRestDirectoryClient() {
		return new RestDirectoryClient(getUris("directory")[0]);
	}
}
