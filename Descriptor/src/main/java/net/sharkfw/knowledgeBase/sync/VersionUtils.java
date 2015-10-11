package net.sharkfw.knowledgeBase.sync;

import java.util.Iterator;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * A utility class to used for with the {@link SyncKB}.
 *
 * @author Nitros Razril (pseudonym)
 */
public final class VersionUtils
{

    /**
     * Flag for no version on an element of {@link SyncKB}.
     */
    public static final int NO_VERSION = 0;

    /**
     * This class has only static methods.
     */
    private VersionUtils()
    {
    }

    /**
     * Gets the Version of a contextpoint.
     * 
     * @param contextPoint The contextpoint to get the version for.
     * @return Version of a contextpoint.
     * @throws SharkKBException Errors while reading the Version.
     * 
     * @see SyncContextPoint
     */
    public static int getVersion(final ContextPoint contextPoint) throws SharkKBException
    {
        final String versionProperty = contextPoint.getProperty(SyncContextPoint.VERSION_PROPERTY_NAME);
        return (versionProperty == null) ? NO_VERSION : Integer.parseInt(versionProperty);
    }

    /**
     * Replaces a contextpoint in an Knowledge Base. This means, the 
     * contextpoint with the same {@link ContextCoordinates} is first deleted
     * an the given contextpoint is inserted.
     * 
     * @param replacement The contextpoint that is the replacement.
     * @param knowledgeBase Knowledge Base to insert the contextpoint into.
     */
    public static void replaceContextPoint(final ContextPoint replacement, final SharkKB knowledgeBase)
    {
        try
        {
            // Remove the old ContextPoint.
            final ContextCoordinates contextCoordinates = replacement.getContextCoordinates();
            knowledgeBase.removeContextPoint(contextCoordinates);
            // Create new ContextPoint and add all old information to it.
            final ContextPoint newContextPoint = knowledgeBase.createContextPoint(contextCoordinates);
            final Iterator<Information> informations = replacement.getInformation();
            while (informations.hasNext())
            {
                final Information information = informations.next();
                newContextPoint.addInformation(information);
            }
            // Add timestemp and version of the old ContextPoint to the new one.
            final String versionProperty = replacement.getProperty(SyncContextPoint.VERSION_PROPERTY_NAME);
            final String timestempProperty = replacement.getProperty(SyncContextPoint.TIMESTAMP_PROPERTY_NAME);
            newContextPoint.setProperty(SyncContextPoint.VERSION_PROPERTY_NAME, versionProperty);
            newContextPoint.setProperty(SyncContextPoint.TIMESTAMP_PROPERTY_NAME, timestempProperty);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Exception occurred while replacing ContextPoint.", ex);
        }
    }
}
