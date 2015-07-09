package SharkImport;

import java.text.ParseException;
import java.util.Map;

import test.Logger;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.TimeSemanticTag;
import net.sharkfw.system.L;
import GooglePlus.GetDataGooglePlus;

/**
 * The Class SharkGPImport.
 * 
 * Calls GetDataGooglePlus.getDataGooglePlus.
 * Converts received Data to Shark ContextCoordinates.
 */
public class SharkGPImport {
	
	private static SharkKB knowledgeBase;
	
	public SharkGPImport(SharkKB knowledgeBase) {
        SharkGPImport.knowledgeBase = knowledgeBase;
    }

	/**
	 * Shark Google Plus Import without message output.
	 *
	 * @param gpUID the Google Plus UserID
	 * @param gpMaxResults the maximal results
	 * @throws ParseException the parse exception
	 * @throws SharkKBException the shark kb exception
	 */
	public static void sharkGPImport(String gpUID, String gpMaxResults) throws ParseException, SharkKBException {
		sharkGPImport(false, gpUID, gpMaxResults);

	}
	
	/**
	 * Shark Google Plus Import.
	 *
	 * @param testModeOn the test mode on/off
	 * @param gpUID the Google Plus UserID
	 * @param gpMaxResults the maximal results
	 * @throws ParseException the parse exception
	 * @throws SharkKBException the shark kb exception
	 */
	public static void sharkGPImport(boolean testModeOn, String gpUID, String gpMaxResults) throws ParseException, SharkKBException {

		Map<Integer, Map<String, String>> gpAllData = GetDataGooglePlus.getDataGooglePlus(gpUID,gpMaxResults);
		int count = gpAllData.size();
		
		for(int i = 0; i < count; i++) {
			Map<String, String> gpData = gpAllData.get(i);
			
	        PeerSemanticTag originator = GPPeerSemanticTag(gpData.get("actorDisplayName"), gpData.get("actorURL"), null);
	        
	        TimeSemanticTag time = GPTimeSemanticTag(gpData.get("published"));
	        
	        ContextCoordinates cc = knowledgeBase.createContextCoordinates(
	                null, // topic
	                originator, // originator
	                originator, // peer
	                null, // remote peer
	                time, // time
	                null, // location
	                SharkCS.DIRECTION_OUT); // direction

	        ContextPoint cp = knowledgeBase.createContextPoint(cc);
	        Information info = cp.addInformation(gpData.get("objectContent"));
	        
	        ContextPoint cp2 = knowledgeBase.getContextPoint(cc);
	        
			if(testModeOn) {
				// Output the data received from API GET request.
				Logger.getInstance().none("");
				Logger.getInstance().none("----- Data Object " + i + " -----");
				Logger.getInstance().none("Coordinates: \n" + L.contextSpace2String(cc));
				Logger.getInstance().none("Information: \n" + gpData.get("objectContent"));
				
			}
	        
		}
	}
	
	/**
	 * If PeerSemanticTag doesn't already exist, create a new one.
	 *
	 * @param name the name
	 * @param si the si
	 * @param address the address
	 * @return the peer semantic tag
	 * @throws SharkKBException the shark kb exception
	 */
    private static PeerSemanticTag GPPeerSemanticTag(String name, String si, String address) throws SharkKBException {
    	if(si == null) {
    		si = "https://plus.google.com/";
    	}
        PeerSemanticTag GPPST = null;
        if (name != null && si != null) {
        	GPPST = knowledgeBase.getPeerSemanticTag(si);
	        if (GPPST == null) {
	        	GPPST = knowledgeBase.createPeerSemanticTag(name, si, address);
            }
            else {
                GPPST.setName(name);
                GPPST.setAddresses(new String[]{address});
            }
        }
        return GPPST;
    }
	
	/**
	 * If TimeSemanticTag doesn't already exist, create a new one.
	 *
	 * @param published the publish date
	 * @return the time semantic tag
	 * @throws SharkKBException the shark kb exception
	 */
    private static TimeSemanticTag GPTimeSemanticTag(String published) throws SharkKBException {
    	long time = Long.parseLong(published, 10);
        TimeSemanticTag GPTST = null;
        if (published != null) {
        	GPTST = knowledgeBase.createTimeSemanticTag(time, 0);
        }
        return GPTST;
    }
}
