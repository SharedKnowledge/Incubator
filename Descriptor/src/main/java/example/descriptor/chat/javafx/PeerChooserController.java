/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.descriptor.chat.javafx;

import static example.descriptor.chat.javafx.ChatApp.switchScene;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;

/**
 * FXML Controller class
 *
 * @author Nitros
 */
public class PeerChooserController implements Initializable
{

    public static final String PEER_CHOOSER_FXML = "/fxml/PeerChooser.fxml";

    private static final String PROPERTIES_FILE = "/properties/peers.properties";

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button browseButton;

    @FXML
    private TextField pathField;

    @FXML
    private ChoiceBox<String> choiceBox;

    private Properties properties;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle)
    {
        loadProperties();
        final ObservableList<String> items = FXCollections.observableArrayList();
        for (Object key : properties.keySet())
        {
            final String name = key.toString();
            items.add(name);
        }
        FXCollections.sort(items);
        choiceBox.setItems(items);
        choiceBox.getSelectionModel().select(0);

        okButton.setOnAction(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(final ActionEvent event)
            {
                loadChat();
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(final ActionEvent event)
            {
                ChatApp.closeApp();
            }
        });
        browseButton.setOnAction(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(final ActionEvent event)
            {
                selectKB();
            }
        });
        pathField.textProperty().addListener(new ChangeListener<String>()
        {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                okButtonState();
            }
        });
        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                okButtonState();
            }
        });
        okButtonState();
    }

    private void loadProperties()
    {
        properties = new Properties();
        final InputStream inputStream = getClass().getResourceAsStream(PROPERTIES_FILE);
        try
        {
            properties.load(inputStream);
        } catch (IOException ex)
        {
            throw new IllegalStateException("Could not load Properties.", ex);
        }
    }

    private void loadChat()
    {
        try
        {
            final String name = choiceBox.getSelectionModel().getSelectedItem();
            final String[] values = properties.getProperty(name).split(",");
            final String si = values[0];
            final String address = values[1];
            final URL chatViewUrl = getClass().getResource(ChatViewController.CHAT_VIEW_FXML);
            final FXMLLoader loader = new FXMLLoader(chatViewUrl);
            final Parent parent = loader.load();
            final ChatViewController controller = loader.getController();
            final PeerSemanticTag peer = new InMemoPeerSTSet().createPeerSemanticTag(name, si, address);
            final String path = pathField.getText();
            final PeerSTSet recipients = new InMemoPeerSTSet();
            for (Map.Entry<Object, Object> entry : properties.entrySet())
            {
                final String recipientName = entry.getKey().toString();
                if (!recipientName.equals(name))
                {
                    final String[] recipientValues = entry.getValue().toString().split(",");
                    final String recipientSI = recipientValues[0];
                    final String recipientAddress = recipientValues[1];
                    recipients.createPeerSemanticTag(recipientName, recipientSI, recipientAddress);
                    System.out.println("Add... " + recipientName + ", " + recipientSI + ", " + recipientAddress);
                }
            }
            controller.initChat(path, peer, recipients);
            switchScene(parent);
        } catch (IOException | SharkKBException | ClassCastException | ArrayIndexOutOfBoundsException | NullPointerException ex)
        {
            throw new IllegalStateException("Could not load ChatView FXML.", ex);
        }
    }

    private void selectKB()
    {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Knowledge Base");
        final File file = directoryChooser.showDialog(ChatApp.getStage());
        if (file != null)
        {
            pathField.setText(file.getAbsolutePath());
        }
    }

    private void okButtonState()
    {
        final String peer = choiceBox.getSelectionModel().getSelectedItem();
        final String kb = pathField.getText();
        if (kb.isEmpty() || peer == null || peer.isEmpty())
        {
            okButton.setDisable(true);
        } else
        {
            okButton.setDisable(false);
        }
    }
}
