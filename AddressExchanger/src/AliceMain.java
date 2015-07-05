package addressExchanger;

/**
*	This program is simulates the peer Alice in an address exchange scenario in Shark. Users can add a new
*	address to the peer. Then the new address is transfered to the other peers in the knowledge base.
*
*	@author Alican Dedekarginoglu
*	@version 1.0
*/
public class AliceMain
{

	private AliceMain()
	{
	}

	/**
	*	This program is simulates the peer Alice in an address exchange scenario in Shark. Users can add a new
	*	address to the peer. Then the new address is transfered to the other peers in the knowledge base.
	*/
	public static void main(String[] args)
	{
		Alice aliceSim = new Alice();
		aliceSim.run();
		while (true)
		{
			int option = Input.getInt("Press 1 to add an address\n", 1, 1);
			switch (option)
			{
				case 1:
					String address = Input.getText("Enter the address\n");
					aliceSim.addAddressToAlice(address);
					break;
			}
		}
	}
}
