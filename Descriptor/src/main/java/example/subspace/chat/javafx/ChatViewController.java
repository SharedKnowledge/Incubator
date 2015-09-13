package example.subspace.chat.javafx;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import net.sharkfw.knowledgeBase.PeerSemanticTag;

public class ChatViewController implements Initializable
{

    public static final String CHAT_VIEW_FXML = "/fxml/ChatView.fxml";

    @FXML
    private Label label;

    private PeerSemanticTag peer;

    @FXML
    private void handleButtonAction(ActionEvent event)
    {
        System.out.println("You clicked me!");
        label.setText("Hello World, it's me!");
        if (peer != null)
        {
            System.out.println(
                    "Name: " + peer.getName()
                    + "\nSI: " + Arrays.toString(peer.getSI())
                    + "\nAddress: " + Arrays.toString(peer.getAddresses())
            );
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }

    public void setPeer(final PeerSemanticTag peer)
    {
        this.peer = peer;
    }
}
