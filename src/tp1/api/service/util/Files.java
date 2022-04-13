package tp1.api.service.util;


public interface Files {

	/**
	 * Write a file. If the file exists, overwrites the contents.
	 * 
	 * @param fileId - unique id of the file. 
	 * @param token - token for accessing the file server (in the first 
	 * project this will not be used).
     *
	 * @return 204 if success.
	 *         403 if the password is incorrect.
	 * 		   400 otherwise.
	 */
	void writeFile(String fileId, byte[] data, String token);

	/**
	 * Delete an existing file.
	 * 
	 * @param fileId - unique id of the file. 
	 * @param token - token for accessing the file server (in the first 
	 * project this will not be used).
	 * 
	 * @return 204 if success; 
	 *		   404 if the uniqueId does not exist.
	 *         403 if the password is incorrect.
	 * 		   400 otherwise.
	 */
	void deleteFile(String fileId, String token);

	/**
	 * Get the contents of the file. 
	 * 
	 * @param fileId - unique id of the file. 
	 * @param token - token for accessing the file server (in the first 
	 * project this will not be used).
	 * 
	 * @return 200 if success + contents (through redirect to the File server); 
	 *		   404 if the uniqueId does not exist.
	 *         403 if the password is incorrect.
	 * 		   400 otherwise.
	 */
	Result<byte[]> getFile(String fileId, String token);

}
