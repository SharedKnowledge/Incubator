package GooglePlus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import test.Logger;

/**
 * The Class GetDataGooglePlus.
 */
public class GetDataGooglePlus {
	
	/**
	 * Get the data from Google Plus without message output.
	 *
	 * @param gpUID the Google Plus UserID
	 * @param gpMaxResults the Google Plus maximal results
	 * @return the data from Google Plus
	 * @throws ParseException the parse exception
	 */
	public static Map<Integer, Map<String, String>> getDataGooglePlus(String gpUID, String gpMaxResults) throws ParseException{
		return getDataGooglePlus(false, gpUID, gpMaxResults);
	}

	/**
	 * Get the data from Google Plus without message output.
	 * Load the Google Plus API.
	 * Convert data and return it.
	 *
	 * @param testModeOn the test mode on
	 * @param gpUID the Google Plus UserID
	 * @param gpMaxResults the Google Plus maximal results
	 * @return the data from Google Plus
	 * @throws ParseException the parse exception
	 */
	public static Map<Integer, Map<String, String>> getDataGooglePlus(boolean testModeOn, String gpUID, String gpMaxResults) throws ParseException{
		// Initialize the Google+ API.
		GooglePlusAPI gpAPI = new GooglePlusAPI();
		
		Map<Integer, Map<String, String>> gpAllData = new HashMap<Integer, Map<String, String>>();

		// Load public data object.
		GooglePlusDataCount gpDataCount = gpAPI.get(gpUID, gpMaxResults);
	    String activities = gpDataCount.getGPData();
	    activities = activities.substring(2, activities.lastIndexOf("}"));
	    String[] arrayactivity = activities.split(Pattern.quote("},  {"));
		int count = gpDataCount.getGPCount();
		
		for(int i = 0; i < count; i++) {
			GooglePlusPublicData activitiy = gpAPI.convert("{" + arrayactivity[i] + "}");
			String arrayPublished[] = activitiy.getPublished().substring(0, activitiy.getPublished().lastIndexOf(".")).split("T");
			Date date = new Date();
			date = new SimpleDateFormat("yyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(arrayPublished[0] + " " + arrayPublished[1]);
			long milliseconds = date.getTime();
			String published = "" + milliseconds;
			
			String actorDisplayName = activitiy.getActor().get("displayName");
			String actorURL = activitiy.getActor().get("url");
			String objectContent = activitiy.getObject().get("content");
			objectContent = objectContent.substring(0, objectContent.lastIndexOf("\ufeff")); 
	
			Map<String, String> gpData = new HashMap<String, String>();
			gpData.put("published" , published);
			gpData.put("actorDisplayName" , actorDisplayName);
			gpData.put("actorURL" , actorURL);
			gpData.put("objectContent" , objectContent);
			
			
			if(testModeOn) {
				// Output the data received from API GET request.
				Logger.getInstance().none("");
				Logger.getInstance().none("----- JSON Object " + i + " -----");
				Logger.getInstance().none("Published: " + activitiy.getPublished());
				// Logger.getInstance().none("published: " + published);
				Logger.getInstance().none("Actor: " + activitiy.getActor());
				// Logger.getInstance().none("displayName: " + actorDisplayName);
				// Logger.getInstance().none("url: " + actorURL);
				Logger.getInstance().none("Object: " + activitiy.getObject());
				// Logger.getInstance().none("objectContent: " + objectContent);
			}
			
			gpAllData.put(i, gpData);
		}
		return gpAllData;
	}
}
