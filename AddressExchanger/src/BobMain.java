package addressExchanger;

/**
* This program is simulates the peer Bob in an address exchange scenario in Shark. This peer
* runs and just waits for an incoming address update interest. If the conditions are met the
* received address is saved into the knowledge base.
*
* @author Alican Dedekarginoglu
* @version 1.0
*/
public class BobMain
{

	private BobMain()
	{
	}

  /**
  * This program is simulates the peer Bob in an address exchange scenario in Shark. This peer
  * runs and just waits for an incoming address update interest. If the conditions are met the
  * received address is saved into the knowledge base.
  */
	public static void main(String[] args)
	{
		Bob bobSim = new Bob();
		bobSim.run();
	}
}
