package GooglePlus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import test.Logger;

import com.google.gson.Gson;

/**
 * Connect to the Google Plus API and download data.
 */
public class GooglePlusAPI {
	
	/**
	 * The API Key assigned to the application in the Google API Console (https://code.google.com/apis/console).
	 * 
	 * This is needed so that Google can determine which application is making the request, which they then use
	 * to limit API usage (no more than X a day).
	 */
	private static final String API_KEY = "AIzaSyDZBg15_6olKJi03kmBh7l4nxZ_jd0ZVLA";
	
	/**
	 * Query the Google Plus Public API and load public user data.
	 *
	 * @param UID Google Plus UserID of the user account to pull data from.
	 * @param maxResults the maximal results
	 * @return GooglePlusDataCount object containing public user data and amount of data objects
	 */
	public GooglePlusDataCount get(String UID, String maxResults){
		
		// Initialize return value
		String json = "";
		GooglePlusDataCount result = null;
		
		try {
			
			// Open a connection to the URL at URI API request string
			URLConnection gpAPI = new URL("https://www.googleapis.com/plus/v1/people/" + UID + "/activities/public?fields=items(published%2Cobject%2Fcontent%2Cactor(displayName%2Curl))&maxResults=" + maxResults + "&key=" + API_KEY ).openConnection();
			// Declare an HTTPURLConnection resource, used to get the response code (and in the error case, evaluate)
			HttpURLConnection gpConnection = ((HttpURLConnection)gpAPI);
			
			// Determine if the request was a success (Code 200)
			if(gpConnection.getResponseCode() != 200){
				// Request was not successful, pull error message from the server
				BufferedReader reader = new BufferedReader(new InputStreamReader(gpConnection.getErrorStream()));
				
				String input;
				while((input = reader.readLine()) != null)
					json += input;
				
				reader.close();
				// Generate a custom exception that parses the JSON output and displays to the user
				throw new GooglePlusAPIException(json);
			}
			
			// Get the response text from the server
			BufferedReader reader = new BufferedReader(new InputStreamReader(gpAPI.getInputStream()));
			
			String input;
			while((input = reader.readLine() ) != null)
				json += input;
			
			String[] arrayjson = json.split("published");
			int countarrayjson = (arrayjson.length - 1);
			String newjson = json.substring(13, json.lastIndexOf(" ]}"));
			
			reader.close();
			
			return new GooglePlusDataCount(newjson, countarrayjson);
			
		}catch(GooglePlusAPIException e){
			e.show();
		}catch(Exception e){
			Logger.getInstance().error("Google+ API Fatal Error: " + e.getMessage());
		}
		
		return result;
		
	}
	
	/**
	 * Convert string to JSON
	 *
	 * @param json the string
	 * @return the JSON with the Google Plus public data
	 */
	public GooglePlusPublicData convert(String json){
		// Convert the JSON request to class object
		Gson gson = new Gson();
		return gson.fromJson(json, GooglePlusPublicData.class);
	}
	
}