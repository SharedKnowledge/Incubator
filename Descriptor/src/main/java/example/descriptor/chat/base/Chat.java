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
 *
 * @author Nitros
 */
public class Chat implements KPListener
{

    private static final String CHAT_ID = "example.subspace.chat.base.Chat#CHAT_ID";

    private final StandardDescriptorSyncKP descriptorPort;
    private final SemanticTag topic;

    private final Collection<ChatListener> listeners;

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
        System.out.println("Add I: " + Collections.list(sharkKB.getAllContextPoints()).size());;
        contextPoint.addInformation(text);
        notifyChatListener();
        System.out.println("C Size: " + Collections.list(sharkKB.getAllContextPoints()).size());;
        send();
    }

    public List<ContextPoint> getEntries() throws SharkKBException
    {
        final Knowledge knowledge = DescriptorAlgebra.extract(
                descriptorPort.getSchema(),
                descriptorPort.getDescriptor()
        );

        System.out.println("K Size: " + knowledge.getNumberOfContextPoints());;

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

    public StandardDescriptorSyncKP getDescriptorPort()
    {
        return descriptorPort;
    }

    public void addListener(final ChatListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(final ChatListener listener)
    {
        listeners.remove(listener);
    }

    private void notifyChatListener()
    {
        for (ChatListener listener : listeners)
        {
            System.out.println("Hello World 1");
            listener.chatChanged();
        }
    }

    @Override
    public void exposeSent(KnowledgePort kp, SharkCS scs)
    {
        // Do nothing
    }

    @Override
    public void insertSent(KnowledgePort kp, Knowledge knwldg)
    {
        // Do nothing
    }

    @Override
    public void knowledgeAssimilated(KnowledgePort knowledgePort, ContextPoint contextPoint)
    {
        System.out.println("Hello World 0");
        notifyChatListener();
    }
}
