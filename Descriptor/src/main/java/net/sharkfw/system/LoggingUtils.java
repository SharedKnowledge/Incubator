package net.sharkfw.system;

/**
 * A utility class to print a title surrounded by {@link BOX_CHAR}. Before the
 * log message.
 *
 * @author Nitros Razril (pseudonym)
 */
public final class LoggingUtils
{

    private static final char BOX_CHAR = '*';
    private static final String BOX_PREFIX = new StringBuilder().append(BOX_CHAR).append(BOX_CHAR).append(" ").toString();
    private static final String BOX_POSTFIX = new StringBuilder().append(" ").append(BOX_CHAR).append(BOX_CHAR).toString();

    /**
     * Log level of this class. See
     * {@link #logBox(String, String, LoggingUtils.LogType, Object)}
     */
    private enum LogType
    {

        /**
         * For normal messages.
         */
        NORMAL,
        /**
         * For debug messages.
         */
        DEBUG,
        /**
         * For error messages.
         */
        ERROR,
        /**
         * For waring messages.
         */
        WARNING
    };

    /**
     * This class has only static methods.
     */
    private LoggingUtils()
    {
    }

    /**
     * Print a debug box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     * @param clazz Class that logs this message.
     */
    public static void debugBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.DEBUG, clazz);
    }

    /**
     * Print a debug box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     */
    public static void debugBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.DEBUG, null);
    }

    /**
     * Print a warning box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     * @param clazz Class that logs this message.
     */
    public static void warningBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.WARNING, clazz);
    }

    /**
     * Print a warning box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     */
    public static void warningBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.WARNING, null);
    }

    /**
     * Print a error box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     * @param clazz Class that logs this message.
     */
    public static void errorBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.ERROR, clazz);
    }

    /**
     * Print a error box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     */
    public static void errorBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.ERROR, null);
    }

    /**
     * Print a normal box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     * @param clazz Class that logs this message.
     */
    public static void logBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.NORMAL, clazz);
    }

    /**
     * Print a normal box.
     *
     * @param titel Title in the box.
     * @param message Log message.
     */
    public static void logBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.NORMAL, null);
    }

    /**
     * Print title and message via {@link L}. Depending on the level a diferent
     * method is used:<br/><br/>
     *
     * <table border ="1">
     * <tr>
     * <td>{@link LogType#DEBUG}</td>
     * <td>{@link L#d(String)}</td>
     * <tr>
     * <td>{@link LogType#ERROR}</td>
     * <td>{@link L#e(String)}</td>
     * </tr>
     * <tr>
     * <td>{@link LogType#WARNING}</td>
     * <td>{@link L#w(String)}</td>
     * </tr>
     * <tr>
     * <td>No matching level found.</td>
     * <td>{@link L#l(String)}</td>
     * </table>
     *
     * @param titel Title of the message.
     * @param message The message itself.
     * @param level the level of the log.
     * @param clazz Class that logs the message.
     */
    private static void logBox(final String titel, final String message, final LogType level, final Object clazz)
    {
        final String formatedTitel = makeTitle(titel);
        switch (level)
        {
            case DEBUG:
                L.d(formatedTitel + message, clazz);
                break;
            case ERROR:
                L.e(formatedTitel + message, clazz);
                break;
            case WARNING:
                L.w(formatedTitel + message, clazz);
                break;
            default:
                L.l(formatedTitel + message, clazz);
        }
    }

    /**
     * Surrounded the title by {@link BOX_CHAR}.
     *
     * @param title title to surround.
     * @return Title surrounded by {@link BOX_CHAR}.
     */
    private static String makeTitle(final String title)
    {
        // Calculates lenght of lines above and belove the title.
        final int lenght = title.length() + BOX_PREFIX.length() + BOX_POSTFIX.length();
        final StringBuilder builder = new StringBuilder("\n");
        // Add line above title
        repeatAppend(builder, BOX_CHAR, lenght);
        builder.append("\n");
        // Add title surrounded by prefix and postfix.
        builder.append(BOX_PREFIX).append(title).append(BOX_POSTFIX);
        builder.append("\n");
        // Add line below title
        repeatAppend(builder, BOX_CHAR, lenght);
        builder.append("\n");
        return builder.toString();
    }

    /**
     * Appends a character a specific number if times to a StringBuilder.
     *
     * @param builder StringBuilder to append the character.
     * @param appendix The character to append.
     * @param repeat Number of times the character should be appended.
     */
    private static void repeatAppend(final StringBuilder builder, final char appendix, final int repeat)
    {
        for (int i = 0; i < repeat; i++)
        {
            builder.append(appendix);
        }
    }
}
