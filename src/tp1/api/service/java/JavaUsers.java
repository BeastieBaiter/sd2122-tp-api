package tp1.api.service.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Singleton;
import tp1.api.User;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;
import tp1.api.service.util.Users;

@Singleton
public class JavaUsers implements Users{
	
	private final Map<String,User> users = new HashMap<>();
	
	public JavaUsers() {}

	@Override
	public synchronized Result<String> createUser(User user) {
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
	public synchronized Result<User> getUser(String userId, String password) {
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
	public synchronized Result<User> updateUser(String userId, String password, User user) {
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
		
		synchronized (this) {
			User user = users.get(userId);

			// Check if user exists
			if (user == null) {
				return Result.error(ErrorCode.NOT_FOUND);
			}

			// Check if the password is correct
			if (!user.getPassword().equals(password)) {
				return Result.error(ErrorCode.FORBIDDEN);
			}

			User deletedUser = user;
			users.remove(userId);
			return Result.ok(deletedUser);
		}
	}

	@Override
	public synchronized Result<List<User>> searchUsers(String pattern) {
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
		if (userId == null) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		synchronized (this) {
			User user = users.get(userId);

			// Check if user exists
			if (user == null) {
				return Result.error(ErrorCode.NOT_FOUND);
			}
		}

		return Result.ok(true);
	}
}
