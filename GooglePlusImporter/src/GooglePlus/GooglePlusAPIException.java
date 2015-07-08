package GooglePlus;

import java.util.Collection;
import java.util.Map;

import test.Logger;

import com.google.gson.Gson;

/**
 * Custom Exception to return a JSON encoded error message when dealing with the Google+ API.
 */
public class GooglePlusAPIException extends Exception {

	/** The error type & message. */
	private String errType, errMessage;
	
	/**
	 * This deals with serialized components in Java.
	 */
	private static final long serialVersionUID = -4914754445188651128L;
	
	/**
	 * Instantiates a new Google Plus API exception.
	 *
	 * @param response the response
	 */
	public GooglePlusAPIException( String response ){
		
		// Initialize Google's JSON encoder/decoder class (this is a utility, it's API independent)
		Gson gson = new Gson();
		
		try {
			
			// Attempt to parse the server response (JSON assumed) into Error class
			// -- Explanation:
			// An example error string: 
			// { "error": {  
			// 	"errors": [   {    "domain": "global",    "reason": "badRequest",    "message": "Bad Request"   }  ],
			//  "code": 400,  "message": "Bad Request" }
			// }
			GPErrorContainer requestError = gson.fromJson( response, GPErrorContainer.class );
			
			// Assign the appropriate map values to exception class variables
			errType = requestError.getErrors().get("reason");
			errMessage = requestError.getErrors().get( "message" );
			
		}catch( Exception e ){
			Logger.getInstance().error(e.getMessage());
			// Unable to parse the returned JSON string into the GPErrorContainer/GPError object
			errType		= 	"Unknown Error";
			errMessage	= 	"An unkown error has occured. Unable to process response from server: \n" + response;
		}
		
	}
	
	/**
	 * Shows the error message returned from the triggering malformed Google Plus API request.
	 * Error displays in the command line.
	 */
	public void show(){
		Logger.getInstance().none("[Google Plus API Error] " + this.errType);
		Logger.getInstance().error(this.errMessage);
		System.exit( 1 );
	}
	
	/**
	 * Returns the error message returned from the triggering malformed Google Plus API request.
	 *
	 * @return String containing the message returned
	 */
	public String getMessage(){
		return this.errMessage;
	}
	
	/**
	 * Returns the error type returned from the triggering malformed Google Plus API request.
	 *
	 * @return String containing the error type returned
	 */
	public String getType(){
		return this.errType;
	}
	
	/**
	 * Container for the JSON parse of a server returned error. JSON elements in the array are parsed to this.
	 */
	private class GPErrorContainer { 
		
		/** The error. */
		private GPError error;
		
		/**
		 * Gets the errors.
		 *
		 * @return the errors
		 */
		public Map<String,String> getErrors(){ return error.errors.iterator().next(); }
		
		/**
		 * Gets the error code.
		 *
		 * @return the code
		 */
		public String getCode(){ return error.code; }
		
		/**
		 * Gets the error message.
		 *
		 * @return the message
		 */
		public String getMessage(){ return error.message; }
	}
	/**
	 * Contains the data elements that are returned as part of the JSON response.
	 */
	private class GPError {
		
		/** The errors. */
		protected Collection<Map<String,String>> errors;
		
		/** The error code. */
		protected String code;
		
		/** The error message. */
		protected String message;
	}
	
}
