package sharkfw_tests;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Enumeration;

import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.TimeSemanticTag;
import net.sharkfw.knowledgeBase.geom.SharkGeometry;
import net.sharkfw.knowledgeBase.geom.inmemory.InMemoSharkGeometry;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;

import org.junit.Before;
import org.junit.Test;

import utils.Utils;

/**
 * This test shows a problem when trying to retrieve a ContextPoint with 
 * a SpatialSemanticTag.
 * One test runs here ( etrieveLocationFromCPByKB() ), the other does not 
 * ( retrieveLocationFromCPByExtract() )
 * 
 * <code>sharkfw.2.12.1.jar</code> was used.
 *
 */
public class RetrieveLocationTest {
	private SharkKB kb;
	private long duration = 60*60*1000;
	private String location_wkt = "POINT (30 10)";
	private PeerSemanticTag alice;
	private SpatialSemanticTag location;
	private ContextCoordinates cc;
	
	
	@Before
	public void setUp() throws Exception {
		kb = new InMemoSharkKB();
		
		TimeSemanticTag time = kb
				.createTimeSemanticTag((new Date()).getTime(), duration);
		SharkGeometry geo = InMemoSharkGeometry.createGeomByWKT(location_wkt);
		location = kb.getSpatialSTSet().createSpatialSemanticTag(location_wkt, new String[]{ Utils.stringToURI(location_wkt) }, geo);
		SemanticTag topic = kb.createSemanticTag("mytopic",
				"http://example.com/mytopic");
		alice = InMemoSharkKB.createInMemoPeerSemanticTag(
				"alice", new String[] {"alice@example.com"}, new String[] {"localhost:8080"} );
		kb.setOwner(alice);
		
		cc = kb.createContextCoordinates(
				topic,
				alice, 
				alice, 
				null, 
				time, 
				location,
				SharkCS.DIRECTION_INOUT);
		ContextPoint cp = kb.createContextPoint(cc);
		cp.addInformation("my description");
	}
	


	/**
	 * This test does not run; SharkCSAlgebra.extract().contextPoints() does 
	 * not seem to keep the location.
	 * 
	 * @throws SharkKBException
	 */
	@Test
	public void retrieveLocationFromCPByExtract() throws SharkKBException {
		// this does not contain the location -> exception is thrown
		Enumeration<ContextPoint> e = SharkCSAlgebra.extract(kb, cc).contextPoints();

		while ( e.hasMoreElements() ) {
			ContextPoint cp = e.nextElement();
			System.out.println("get cp location: " + L.semanticTag2String(cp.getContextCoordinates().getLocation()));

			assertNotNull(cp.getContextCoordinates().getLocation());
		}
	}
	
	/**
	 * This test extracts the CPs using getContextPoints(); the location can be 
	 * retrieved
	 * 
	 * @throws SharkKBException
	 */
	@Test
	public void retrieveLocationFromCPByKB() throws SharkKBException {

		//this works
		Enumeration<ContextPoint> e = kb.getContextPoints(cc);

		while ( e.hasMoreElements() ) {
			ContextPoint cp = e.nextElement();
			System.out.println("get cp location: " + L.semanticTag2String(cp.getContextCoordinates().getLocation()));

			assertNotNull(cp.getContextCoordinates().getLocation());
		}
	}
}
