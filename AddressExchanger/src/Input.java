package addressExchanger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**	An Input object represents the given input from the keyboard.
*/
public class Input
{
	/** The given input from the keyboard. */
	private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	
	/** Class constructor. */
	private Input()
	{
	}

	/**	Gets the input string given from the keyboard.
	*/
	public static String getText()
	{
		return getText("");
	}

	/**	Respectively prints a prompt on the monitor and gets the 
	*	input string given from the keyboard.
	*/
	public static String getText(String inputPrompt)
	{
		System.out.print(inputPrompt);
		String text = "";
		boolean inputOk = false;
		while (!inputOk)
		{
			try
			{
				text = input.readLine();
				inputOk = true;
			}
			catch (IOException ioe)
			{
			}
		}
		return text;
	}

	/**	Gets the input int value given from the keyboard.
	*/
	public static int getInt()
	{
		return getInt("");
	}

	/**	Respectively prints a prompt on the monitor and gets the 
	*	input int value given from the keyboard.
	*/
	public static int getInt(String inputPrompt)
	{
		int value = 0;
		boolean inputOk = false;
		while(!inputOk)
		{
			try
			{
				value = Integer.parseInt(getText(inputPrompt));
				inputOk = true;
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		return value;
	}

	/**	Respectively prints a prompt on the monitor and gets the 
	*	input int value given from the keyboard and validates 
	*	whether it lies in the desired interval. 
	*/
	public static int getInt(String inputPrompt, int min, int max)
	{
		int value = 0;
		boolean inputOk = false;
		while (!inputOk)
		{
			value = getInt(inputPrompt);
			inputOk = (min <= value && value <= max);
		}
		return value;
	}
}
