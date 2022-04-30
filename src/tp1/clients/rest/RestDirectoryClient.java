
package tp1.clients.rest;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Result.ErrorCode;

public class RestDirectoryClient extends RestClient implements Directory {
	
	final WebTarget target;
	
	public RestDirectoryClient(URI serverURI) {
		super( serverURI );
		target = client.target( serverURI ).path( RestDirectory.PATH );
	}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
		return super.reTry(() -> {
			return clt_writeFile(filename, data, userId, password);
		});
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {
		super.reTry(() -> {
			clt_deleteFile(filename, userId, password);
			return null;
		});

	}
	
	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		return super.reTry(() -> {
			return clt_shareFile(filename, userId, userIdShare, password);
		});
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		return super.reTry(() -> {
			return clt_unshareFile(filename, userId, userIdShare, password);
		});
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		return super.reTry(() -> {
			return clt_getFile(filename, userId, accUserId, password);
		});
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		return super.reTry(() -> {
			return clt_lsFile(userId, password);
		});
	}

	private FileInfo clt_writeFile(String filename, byte[] data, String userId, String password) {
		Response r = target.path(userId).path(filename)
				.queryParam(RestDirectory.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
		
		int status = r.getStatus();
		
		if(status == Status.NO_CONTENT.getStatusCode()) {
			System.out.println("Write File Success");
			return r.readEntity(FileInfo.class);
		}
		else {
			System.out.println("Error, HTTP error status: " + status);
			return null;
		}
	}

	private void clt_deleteFile(String filename, String userId, String password) {
		Response r = target.path(userId).path(filename)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		
		int status = r.getStatus();
		
		if(status == Status.NO_CONTENT.getStatusCode())
			System.out.println("Delete File Success");
		else
			System.out.println("Error, HTTP error status: " + status);
		
	}
	
	private Result<Void> clt_shareFile(String filename, String userId, String userIdShare, String password) {
		//TODO
		Response r = target.path(userId).path(filename).path("/share").path(userIdShare)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Void.class, MediaType.APPLICATION_JSON));
		
		int status = r.getStatus();
		
		if(status == Status.NO_CONTENT.getStatusCode()) {
			return Result.ok();
		}
		else {
			return Result.error(convertToError(status));
		}
	}
	
	private Result<Void> clt_unshareFile(String filename, String userId, String userIdShare, String password) {
		Response r = target.path(userId).path(filename).path("/share").path(userIdShare)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		
		int status = r.getStatus();
		
		if(status == Status.NO_CONTENT.getStatusCode()) {
			return Result.ok();
		}
		else {
			return Result.error(convertToError(status));
		}
	}

	private Result<byte[]> clt_getFile(String filename, String userId, String accUserId, String password) {
		Response r = target.path(userId).path(filename).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		int status =  r.getStatus();

		if(status == Status.OK.getStatusCode() && r.hasEntity() ) {
			byte[] f = r.readEntity(byte[].class);
			System.out.println( "File : " + f);
			return Result.ok(f);
		} else {
			System.out.println("Error, HTTP error status: " + status );
			return Result.error(convertToError(status));
		}
	}
	
	private Result<List<FileInfo>> clt_lsFile(String userId, String password) {
		Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		int status =  r.getStatus();

		if(status == Status.OK.getStatusCode() && r.hasEntity() ) {
			List<FileInfo> l = r.readEntity(new GenericType<List<FileInfo>>() {});
			System.out.println( "List of files : " + l);
			return Result.ok(l);
		} else {
			System.out.println("Error, HTTP error status: " + status );
			return Result.error(convertToError(status));
		}
	}
	
	private ErrorCode convertToError(int error) {
		
		switch (error) {
			case 200:
				return ErrorCode.OK;
			case 400:
				return ErrorCode.BAD_REQUEST;
			case 403:
				return ErrorCode.FORBIDDEN;
			case 409:
				return ErrorCode.CONFLICT;
			case 404:
				return ErrorCode.NOT_FOUND;
			case 500:
				return ErrorCode.INTERNAL_ERROR;
			case 501:
				return ErrorCode.NOT_IMPLEMENTED;
			default:
				break;
			}
			return null;
}

}
