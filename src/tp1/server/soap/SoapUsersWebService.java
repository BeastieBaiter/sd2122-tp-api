package tp1.server.soap;


import java.util.List;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import tp1.api.User;
import tp1.api.service.java.JavaUsers;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;

@WebService(serviceName=SoapUsers.NAME, targetNamespace=SoapUsers.NAMESPACE, endpointInterface=SoapUsers.INTERFACE)
public class SoapUsersWebService implements SoapUsers {
	private final JavaUsers impl = new JavaUsers();

	static Logger Log = Logger.getLogger(SoapUsersWebService.class.getName());
	
	public SoapUsersWebService() {
	}

	@Override
	public String createUser(User user) throws UsersException {
		Log.info(String.format("SOAP createUser: user = %s\n", user));
		var result = impl.createUser(user);
		if (result.isOK()) {
			return result.value();
		}
		else {
			throw new UsersException(result.toString()); 
		}
		
	}

	@Override
	public User getUser(String userId, String password) throws UsersException {
		Log.info(String.format("SOAP getUser: userId = %s, password = %s\n", userId, password));
		var result = impl.getUser(userId, password);
		if (result.isOK()) {
			return result.value();
		}
		else {
			throw new UsersException(result.toString()); 
		}
	}

	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		Log.info(String.format("SOAP updateUser: userId = %s, password = %s, user = %s\n", userId, password, user));
		var result = impl.updateUser(userId, password, user);
		if (result.isOK()) {
			return result.value();
		}
		else {
			throw new UsersException(result.toString()); 
		}
	}

	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		Log.info(String.format("SOAP deleteUser: userId = %s, password = %s\n", userId, password));
		var result = impl.deleteUser(userId, password);
		if (result.isOK()) {
			return result.value();
		}
		else {
			throw new UsersException(result.toString()); 
		}
	}

	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		Log.info(String.format("SOAP searchUser: pattern = %s\n", pattern));
		var result = impl.searchUsers(pattern);
		if (result.isOK()) {
			return result.value();
		}
		else {
			throw new UsersException(result.toString()); 
		}
	}
}
