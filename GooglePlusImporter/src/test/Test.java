package test;

import java.text.ParseException;
import java.util.Map;

import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import GooglePlus.GetDataGooglePlus;
import SharkImport.SharkGPImport;

/**
 * Test GooglePlusImport with an Account.
 * 
 * UID: 115144293939158706064
 * maxResults: 5
 */
class Test {
	
	/** Google+ User ID for testing: 102812681980455627731. */
	public static final String googlePlusUID = "115144293939158706064";
	
	/** How many Results should be shown. */
	public static final String googlePlusMaxResults = "2";
	
	/**
	 * The main method.
	 * Calls GetDataGooglePlus.getDataGooglePlus.
	 * Calls SharkGPImport.sharkGPImport.
	 *
	 * @param args the arguments
	 * @throws ParseException the parse exception
	 * @throws SharkKBException the shark kb exception
	 */
	public static void main( String[] args ) throws ParseException, SharkKBException {
		
		// Google+ API.
		Logger.getInstance().none("_____ Google+ API Results _____");
		Map<Integer, Map<String, String>> gpAllData = GetDataGooglePlus.getDataGooglePlus(true,googlePlusUID,googlePlusMaxResults);
		Logger.getInstance().none("_______________________________");
		
		Logger.getInstance().none("");
		Logger.getInstance().none("");
		Logger.getInstance().none("");

		// Shark Google+ Import.
		FSSharkKB gpKB = new FSSharkKB("googlePlusKB");
		SharkGPImport sharkGPImportKB = new SharkGPImport(gpKB);
		Logger.getInstance().none("_____ Shark Google+ Import Results _____");
		SharkGPImport.sharkGPImport(true,googlePlusUID,googlePlusMaxResults);
		Logger.getInstance().none("_______________________________");
		
		// Exit program.
		System.exit(0);
		
	}
	
}