package example.descriptor.chat.base;

import example.descriptor.chat.javafx.ChatViewController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorAlgebra;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.descriptor.peer.StandardDescriptorSyncKP;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.TimeSemanticTag;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoTimeSemanticTag;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.SharkSecurityException;

/**
 * This class is a chat. It uses a {@link StandardDescriptorSyncKP} to publish
 * its entries.
 *
 * @author Nitros Razril (pseudonym)
 */
public class Chat implements KPListener
{

    /**
     * ID of the descriptor to create.
     */
    private static final String CHAT_ID = "example.subspace.chat.base.Chat#CHAT_ID";
    /**
     * {@link KnowledgePort} used by this Chat.
     */
    private final StandardDescriptorSyncKP descriptorPort;
    /**
     * General chat topic. Used to create {@link ContextPoint}.
     */
    private final SemanticTag topic;
    /**
     * Collection of listeners to notify if the chat changes.
     */
    private final Collection<ChatListener> listeners;

    /**
     * Constructor for this class. It creates the
     * {@link StandardDescriptorSyncKP}, the topic and sets a {@link KPListener}
     * on the Port. When finished, pulls data from all recipients.<br/><br/>
     *
     * Note:
     * {@link SyncDescriptorSchema#overrideDescriptor(ContextSpaceDescriptor)}
     * is called to save the created {@link ContextSpaceDescriptor}-
     *
     * @param sharkKB Knowledge Base to create the {@link ContextPoint} in.
     * @param engine Engine to send the data.
     * @param recipients Recipients to chat with.
     * @throws SharkKBException If creating the context for the
     * {@link ContextSpaceDescriptor} fails.
     * @throws DescriptorSchemaException If the {@link ContextSpaceDescriptor}
     * could not be saved in {@link SyncDescriptorSchema}.
     */
    public Chat(final SharkKB sharkKB, final SharkEngine engine, final PeerSTSet recipients) throws SharkKBException, DescriptorSchemaException
    {
        final STSet topics = new InMemoSTSet();
        topic = topics.createSemanticTag(CHAT_ID, CHAT_ID);
        final SharkCS context = new InMemoSharkKB().createInterest(
                topics,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextSpaceDescriptor descriptor = new ContextSpaceDescriptor(context, CHAT_ID);
        final SyncKB syncKB = new SyncKB(sharkKB);
        final SyncDescriptorSchema schema = new SyncDescriptorSchema(syncKB);
        schema.overrideDescriptor(descriptor);
        descriptorPort = new StandardDescriptorSyncKP(engine, schema, descriptor, recipients);
        descriptorPort.addListener(this);
        listeners = new ArrayList<>();
        read();
    }

    /**
     * Adds an entry to the chat and sends it.<br/>
     * It first creates a {@link ContextPoint}, adds the parameter text as
     * {@link Information} and calls {@link #send()}. The ContextPoint contains
     * the Knowledge Base owner as originator and the current time as 'from' in
     * the time dimension.
     *
     * @param text Text of the new entry.
     * @throws SharkKBException If creating the {@link ContextPoint} fails.
     */
    public void addEntry(final String text) throws SharkKBException
    {
        final SyncKB sharkKB = descriptorPort.getSchema().getSyncKB();
        final long time = System.currentTimeMillis();
        final TimeSemanticTag timeTag = new InMemoTimeSemanticTag(time, 0);
        final ContextCoordinates contextCoordinates = sharkKB.createContextCoordinates(
                topic,
                sharkKB.getOwner(),
                null,
                null,
                timeTag,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint contextPoint = sharkKB.createContextPoint(contextCoordinates);
        contextPoint.addInformation(text);
        notifyChatListener();
        send();
    }

    /**
     * Gets all entries of this chat. In detail, it extracts all
     * {@link ContextPoint} from {@link #descriptorPort} underlying Knowledge
     * Base using the ports {@link ContextSpaceDescriptor}. The point have the
     * following format:<br/>
     * <table border="1">
     * <tr><td>Topic</td><td>technical description</td></tr>
     * <tr><td>Originator</td><td>author of the entry</td></tr>
     * <tr>
     * <td>Time</td>
     * <td>time the entry was written ({@link TimeSemanticTag#getFrom()})</td>
     * </tr>
     * </table>
     *
     * @return A List of all ContextPoint representing the entries of this chat.
     * @throws SharkKBException Any error while extracting the points.
     */
    public List<ContextPoint> getEntries() throws SharkKBException
    {
        final Knowledge knowledge = DescriptorAlgebra.extract(
                descriptorPort.getSchema(),
                descriptorPort.getDescriptor()
        );
        final List<ContextPoint> list = Collections.list(knowledge.contextPoints());
        Collections.sort(list, new Comparator<ContextPoint>()
        {

            @Override
            public int compare(ContextPoint firstPoint, ContextPoint secondPoint)
            {
                long firstTime = firstPoint.getContextCoordinates().getTime().getFrom();
                long secondTime = secondPoint.getContextCoordinates().getTime().getFrom();
                return (int) (firstTime - secondTime);
            }
        });
        return list;
    }

    /**
     * Sends data. Simply calls {@link StandardDescriptorSyncKP#pullRequest()}.
     */
    private void send()
    {
        try
        {
            descriptorPort.pullRequest();
        } catch (SharkKBException | SharkSecurityException | IOException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Message send failed. Check Log.", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads data. Simply calls {@link StandardDescriptorSyncKP#pull()}.
     */
    private void read()
    {
        try
        {
            descriptorPort.pull();
        } catch (SharkSecurityException | IOException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Reading failed. Check Log.", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add a listener to get notified when the chat changes.
     *
     * @param listener The listener to add.
     */
    public void addListener(final ChatListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Removes a listener to no longer get notified when the chat changes.
     *
     * @param listener The listener to removes.
     */
    public void removeListener(final ChatListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Notify all listener when the chat changes.
     */
    private void notifyChatListener()
    {
        for (ChatListener listener : listeners)
        {
            listener.chatChanged();
        }
    }

    /**
     * Class is not interested in this event.
     */
    @Override
    public void exposeSent(KnowledgePort kp, SharkCS scs)
    {
        // Do nothing
    }

    /**
     * Class is not interested in this event.
     */
    @Override
    public void insertSent(KnowledgePort kp, Knowledge knwldg)
    {
        // Do nothing
    }

    /**
     * Notify all listener when {@link StandardDescriptorSyncKP} assimilates 
     * a ContextPoint.
     * 
     * @param knowledgePort Always {@link #descriptorPort} in this case.
     * @param contextPoint The assimilated ContextPoint.
     */
    @Override
    public void knowledgeAssimilated(KnowledgePort knowledgePort, ContextPoint contextPoint)
    {
        notifyChatListener();
    }
}
