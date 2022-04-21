package tp1.server.resources.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.java.JavaUsers;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result.ErrorCode;

@Singleton
public class UsersResource implements RestUsers {

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final JavaUsers impl = new JavaUsers();
	
	public UsersResource() {
	}
		
	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		var result = impl.createUser(user);
		
		if(result.isOK()) {
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}


	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		var result = impl.getUser(userId, password);
		
		if(result.isOK()) {
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}


	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
		var result = impl.updateUser(userId, password, user);
		
		if(result.isOK()) {
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		var result = impl.deleteUser(userId, password);
		
		if(result.isOK()) {
			
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}


	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		var result = impl.searchUsers(pattern);
		
		if(result.isOK()) {
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
	}
	
	@Override
	public boolean hasUser(String userId) {
		Log.info("hasUser : user = " + userId + ";");
		var result = impl.hasUser(userId);
		
		if(result.isOK()) {
			return result.value();
		}
		else {
			throw new WebApplicationException(convertToStatus(result.error()));
		}
		
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
