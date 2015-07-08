package test;

/**
 * The Class Logger.
 * 
 * Write messages to console.
 */
public class Logger {

	/** The instance. */
	private static Logger instance;

	/**
	 * Instantiates a new logger.
	 */
	private Logger() {
	}

	/**
	 * Gets the single instance of Logger.
	 *
	 * @return single instance of Logger
	 */
	public static Logger getInstance() {
		if (Logger.instance == null)
			Logger.instance = new Logger();
		return instance;
	}

	/**
	 * Message with no level.
	 *
	 * @param to_log the to_log
	 */
	public void none(String to_log) {
		log("none", to_log);
	}

	/**
	 * Message with level Info.
	 *
	 * @param to_log the to_log
	 */
	public void info(String to_log) {
		log("INFO", to_log);
	}

	/**
	 * Message with level Error.
	 *
	 * @param to_log the to_log
	 */
	public void error(String to_log) {
		log("ERROR", to_log);
	}	

	/**
	 * Write message to console.
	 *
	 * @param level the level
	 * @param to_log the to_log
	 */
	private void log(String level, String to_log) {
		if(level == "none") {
			System.out.println(to_log);
		}
		else {
			System.out.println(level + ": " + to_log);
		}
	}
}
