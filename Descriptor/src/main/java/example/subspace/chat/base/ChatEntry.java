/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.subspace.chat.base;

import java.util.Date;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.TimeSemanticTag;
import net.sharkfw.knowledgeBase.inmemory.InMemoTimeSemanticTag;

/**
 *
 * @author Nitros
 */
public class ChatEntry
{

    private final ContextPoint contextPoint;

    protected static final ChatEntry createChatEntry(final ContextSpaceDescriptor chatDescriptor, final SharkKB sharkKB, final String text) throws SharkKBException
    {
        final PeerSemanticTag owner = sharkKB.getOwner();
        if (owner == null)
        {
            throw new IllegalArgumentException("SharkKB must have an Owner.");
        }
        // topic
        final SemanticTag topic = chatDescriptor.getTopics().tags().nextElement(); //should have exactly one element
        // originator
        final PeerSemanticTag originator = chatDescriptor.getOriginator();
        // time
        final long currentTime = System.currentTimeMillis();
        final TimeSemanticTag time = new InMemoTimeSemanticTag(currentTime, 0);
        // build ContextPoint as Entry
        final ContextCoordinates coordinates = sharkKB.createContextCoordinates(
                topic,
                originator,
                owner,
                null,
                time,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint contextPoint = sharkKB.createContextPoint(coordinates);
        contextPoint.addInformation(text);
        return new ChatEntry(contextPoint);
    }

    protected ChatEntry(final ContextPoint contextPoint) throws SharkKBException
    {
        this.contextPoint = contextPoint;
    }

    public String getText() throws SharkKBException
    {
        return contextPoint.getInformation().next().getContentAsString();
    }

    public PeerSemanticTag getAuthor()
    {
        return contextPoint.getContextCoordinates().getPeer();
    }

    public Date getTime()
    {
        final long time = contextPoint.getContextCoordinates().getTime().getFrom();
        return new Date(time);
    }

}
