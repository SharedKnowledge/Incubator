package GooglePlus;

import java.util.Map;

/**
 * Google Plus Public Data storing the result of a generic Google Plus API request:
 * <pre>https://www.googleapis.com/plus/v1/people/[USER_ID]/activities/public?maxResults=[MAX_RESULTS]&key=[API_KEY]</pre>
 */
public class GooglePlusPublicData implements InterfaceGooglePlusPublicData {

	/** The Constant countarrayjson. */
	public static final GooglePlusPublicData countarrayjson = null;

	/** The published date. */
	private String published;
	
	/** The actor information. */
	private Map<String,String> actor;
	
	/** The object content. */
	private Map<String,String> object;
	
	/* (non-Javadoc)
	 * @see GooglePlus.InterfaceGooglePlusPublicData#getPublished()
	 */
	public String getPublished(){ return published; }
	
	/* (non-Javadoc)
	 * @see GooglePlus.InterfaceGooglePlusPublicData#getActor()
	 */
	public Map<String,String> getActor(){ return actor; }
	
	/* (non-Javadoc)
	 * @see GooglePlus.InterfaceGooglePlusPublicData#getObject()
	 */
	public Map<String,String> getObject(){ return object; }

}
