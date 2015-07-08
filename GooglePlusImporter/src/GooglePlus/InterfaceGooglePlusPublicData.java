package GooglePlus;

import java.util.Map;

/**
 * The Interface InterfaceGooglePlusPublicData.
 */
public interface InterfaceGooglePlusPublicData {
	
	/**
	 * Gets the published date.
	 *
	 * @return the published
	 */
	public String getPublished();
	
	/**
	 * Gets the actor information.
	 *
	 * @return the actor
	 */
	public Map<String,String> getActor();
	
	/**
	 * Gets the object content.
	 *
	 * @return the object
	 */
	public Map<String,String> getObject();

}
