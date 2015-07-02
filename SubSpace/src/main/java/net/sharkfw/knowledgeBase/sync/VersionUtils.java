/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.knowledgeBase.sync;

import java.util.Iterator;
import java.util.List;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoKnowledge;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public final class VersionUtils
{

    public static final int NO_VERSION = 0;

    private VersionUtils()
    {
    }

    public static Knowledge toKnowledge(final List<? extends ContextPoint> contextPoints)
    {
        final Knowledge knowledge = new InMemoKnowledge();
        for (ContextPoint contextPoint : contextPoints)
        {
            knowledge.addContextPoint(contextPoint);
        }
        return knowledge;
    }

    public static int getVersion(final ContextPoint contextPoint) throws SharkKBException
    {
        final String versionProperty = contextPoint.getProperty(SyncContextPoint.VERSION_PROPERTY_NAME);
        return (versionProperty == null) ? NO_VERSION : Integer.parseInt(versionProperty);
    }

    public static SyncContextPoint copy(final ContextPoint contextPoint)
    {
        SyncContextPoint syncContextPoint;
        try
        {
            final ContextCoordinates contextCoordinates = contextPoint.getContextCoordinates();
            final String versionProperty = contextPoint.getProperty(SyncContextPoint.VERSION_PROPERTY_NAME);
            final ContextPoint copiedContextPoint = InMemoSharkKB.createInMemoContextPoint(contextCoordinates);
            syncContextPoint = new SyncContextPoint(copiedContextPoint);
            syncContextPoint.setProperty(SyncContextPoint.VERSION_PROPERTY_NAME, versionProperty);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Could not read version property.", ex);
        }
        return syncContextPoint;
    }

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
