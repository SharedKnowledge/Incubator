package utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * This class contains arbitrary helper methods used by Calendar functionalities
 *
 */
public final class Utils {
	private static final String ENC = "UTF-8";
	
	
	/**
	 * This method converts a String to an encoded data-URL with the String's 
	 * content being the resource.
	 * 
	 * @param s the String encoded to a data-URL
	 * @return the String representing the URL containing the content of s
	 */
	public static String stringToURI(String s) {
		try {
			return "data://" + URLEncoder.encode(s, ENC);
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Invalid encoding.", e);
		}
	}
	
	/**
	 * This method decodes a String from a data-URL encoded with 
	 * {@link Utils#stringToURI(String)}.
	 * 
	 * @param uri the String representing the data-URL containing the String
	 * @return	the String that was decoded
	 */
	public static String uriToString(String uri) {
		if ( !uri.startsWith("data://") ) {
			throw new IllegalArgumentException("Invalid format.");
		}
		try {
			return URLDecoder.decode(uri.substring(7), ENC);
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Invalid encoding.", e);
		}
	}
	
	/**
	 * This method recursively deletes all contents of the passed directory 
	 * on the file system.
	 * 
	 * @param directory the directory that will be deleted
	 * @return true if the directory was successfully deleted, false otherwise
	 */
	public static boolean deleteDirectory(File directory) {
    if(directory.exists()){
        File[] files = directory.listFiles();
        if(null!=files){
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
    }
    return(directory.delete());
}
}
