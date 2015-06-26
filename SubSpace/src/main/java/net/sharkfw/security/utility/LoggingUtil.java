/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.security.utility;

import net.sharkfw.system.L;

/**
 *
 * @author jgrundma
 */
public final class LoggingUtil
{

    private static final String LOG_BOX = "\n******************************************\n%s\n******************************************\n";

    private enum LogType
    {

        NONE,
        DEBUG,
        ERROR,
        WARNING
    };

    private LoggingUtil()
    {
    }

    public static void logBox(final String titel, final String message, final LogType level, final Object clazz)
    {
        final String formatedTitel = String.format(LOG_BOX, titel);
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
}
