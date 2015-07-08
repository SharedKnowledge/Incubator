package GooglePlus;

/**
 * The Class GooglePlusDataCount.
 */
public class GooglePlusDataCount {
    
    /** The Google Plus data as a string. */
    private final String gpData;
    
    /** The results as an integer. */
    private final int results;

    /**
     * Instantiates a new Google Plus data count.
     *
     * @param gpData the Google Plus data as a string
     * @param results the amount of data objects
     */
    public GooglePlusDataCount(String gpData, int results) {
        this.gpData = gpData;
        this.results = results;
    }

    /**
     * Gets the Google Plus data as a string.
     *
     * @return the Google Plus data as a string
     */
    public String getGPData() {
        return gpData;
    }

    /**
     * Gets the Google Plus data object count.
     *
     * @return the Google Plus data object count
     */
    public int getGPCount() {
        return results;
    }
}