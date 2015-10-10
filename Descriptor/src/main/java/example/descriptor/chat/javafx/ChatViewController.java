package example.descriptor.chat.javafx;

import example.descriptor.chat.base.Chat;
import example.descriptor.chat.base.ChatListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;

/**
 * The controller for the chat view. Handles input of chat entries,
 * shows the chat entries and the recipients.
 * 
 * @author Nitros Razril (pseudonym)
 */
public class ChatViewController implements ChatListener
{
    /**
     * FXML File this class is the controller of.
     */
    public static final String CHAT_VIEW_FXML = "/fxml/ChatView.fxml";
    /**
     * List of chat entries.
     */
    @FXML
    private ListView<String> entryList;
    /**
     * List of recipients. This a the persons who will receive the send chat entry.
     */
    @FXML
    private ListView<String> recipientList;
    /**
     * Input field for a message.
     */
    @FXML
    private TextField input;
    /**
     * Button to send a message.
     */
    @FXML
    private Button sendButton;
    /**
     * The actual chat.
     */
    private Chat chat;

    /**
     * Whenever the chat changes reload all entries.
     */
    @Override
    public void chatChanged()
    {
        reloadEntries();
    }

    /**
     * Initializes the chat. Creates a {@link FSSharkKB} based on the given
     * parameters. Adds all recipients to it, creates and starts the engine,
     * adds a close Request to the stage of this application, so the engine is
     * stopped on close and crates the actual chat object. After that, all entries
     * are loaded.<br/><br/>
     * 
     * NOTE: The name of peer is added to the End of path before creating 
     * the Knowledge Base.
     * 
     * @param path Path to create the Knowledge Base in.
     * @param peer Person that chats. Will also be owner of Knowledge Base.
     * @param recipients Persons to chat with.
     */
    public void initChat(final String path, final PeerSemanticTag peer, final PeerSTSet recipients)
    {
        try
        {
            final FSSharkKB sharkKB = new FSSharkKB(path + "/" + peer.getName());
            sharkKB.getPeersAsTaxonomy().asPeerSTSet().merge(recipients);
            if (sharkKB.getOwner() == null)
            {
                sharkKB.setOwner(peer);
            }
            final PeerSemanticTag owner = sharkKB.getOwner();
            final String portAsString = owner.getAddresses()[0].split(":")[2];
            final int port = Integer.parseInt(portAsString);
            final J2SEAndroidSharkEngine engine = new J2SEAndroidSharkEngine();
            engine.startTCP(port);
            ChatApp.getStage().setOnCloseRequest(new EventHandler<WindowEvent>()
            {

                @Override
                public void handle(WindowEvent event)
                {
                    if (engine != null)
                    {
                        engine.stopTCP();
                    }
                }
            });
            chat = new Chat(sharkKB, engine, recipients);
            chat.addListener(this);
            input.setOnAction(new EventHandler<ActionEvent>()
            {

                @Override
                public void handle(ActionEvent event)
                {
                    addEntry();
                }
            });
            sendButton.setOnAction(new EventHandler<ActionEvent>()
            {

                @Override
                public void handle(ActionEvent event)
                {
                    addEntry();
                }
            });
            setRecipients(recipients);
            reloadEntries();
        } catch (SharkKBException | IOException | DescriptorSchemaException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, ex);
            ChatApp.closeApp();
        }
    }

    /**
     * Adds the recipients to {@link #recipientList}.
     * 
     * @param recipients recipients to {@link #recipientList}
     */
    private void setRecipients(final PeerSTSet recipients)
    {
        final ObservableList<String> items = FXCollections.observableArrayList();
        Enumeration<PeerSemanticTag> peers = recipients.peerTags();
        while (peers.hasMoreElements())
        {
            final PeerSemanticTag peer = peers.nextElement();
            items.add(peer.getName());
        }
        recipientList.setItems(items);
    }

    /**
     * Loads all entries and adds them to{@link #entryList}. Only {@link ContextPoint}
     * which have {@link Information} and only the first one will be added.
     * The formate is:<br/>
     * Sender (Date): Text 
     */
    private void reloadEntries()
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());
        final ObservableList<String> items = FXCollections.observableArrayList();
        try
        {
            final List<ContextPoint> entries = chat.getEntries();
            for (final ContextPoint entry : entries)
            {
                if (entry.getInformation().hasNext())
                {
                    final String text = entry.getInformation().next().getContentAsString();
                    final String sender = entry.getContextCoordinates().getOriginator().getName();
                    final long time = entry.getContextCoordinates().getTime().getFrom();
                    final Date date = new Date(time);
                    final StringBuilder builder = new StringBuilder();
                    builder.append(sender).append(' ');
                    builder.append('(').append(dateFormat.format(date)).append(')');
                    builder.append(':').append(' ');
                    builder.append(text);
                    items.add(builder.toString());
                }
            }
            entryList.setItems(items);
        } catch (SharkKBException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, ex);
            ChatApp.closeApp();
        }
    }

    /**
     * Add an entry to the chat, but only if {@link #input} is not empty.
     * Clear {@link #input} afterwards.
     */
    private void addEntry()
    {
        try
        {
            String text = input.getText();
            if (!text.isEmpty())
            {
                chat.addEntry(text);
                input.setText("");
            }
        } catch (SharkKBException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Message send failed. Check Log.", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
