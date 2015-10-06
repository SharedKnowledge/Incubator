/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.system;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public final class LoggingUtils
{

    private static final char BOX_CHAR = '*';
    private static final String BOX_PREFIX = new StringBuilder().append(BOX_CHAR).append(BOX_CHAR).append(" ").toString();
    private static final String BOX_POSTFIX = new StringBuilder().append(" ").append(BOX_CHAR).append(BOX_CHAR).toString();

    private enum LogType
    {

        NONE,
        DEBUG,
        ERROR,
        WARNING
    };

    private LoggingUtils()
    {
    }

    public static void debugBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.DEBUG, clazz);
    }

    public static void debugBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.DEBUG, null);
    }

    public static void warningBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.WARNING, clazz);
    }

    public static void warningBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.WARNING, null);
    }

    public static void errorBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.ERROR, clazz);
    }

    public static void errorBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.ERROR, null);
    }

    public static void logBox(final String titel, final String message, final Object clazz)
    {
        logBox(titel, message, LogType.NONE, clazz);
    }

    public static void logBox(final String titel, final String message)
    {
        logBox(titel, message, LogType.NONE, null);
    }

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

    private static void repeatAppend(final StringBuilder builder, final char appendix, final int repeat)
    {
        for (int i = 0; i < repeat; i++)
        {
            builder.append(appendix);
        }
    }
}
