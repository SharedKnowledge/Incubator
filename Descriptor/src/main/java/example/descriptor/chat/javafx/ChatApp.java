package example.descriptor.chat.javafx;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Class of the Chat Application. Starts up the JavaFX GUI-Thread.
 *
 * @author Nitros Razril (pseudonym)
 */
public class ChatApp extends Application
{

    /**
     * Title for the frame.
     */
    private static final String TITLE = "Chat";
    /**
     * JavaFX stage to show the GUI in.
     */
    private static Stage stage;

    /**
     * Returns {@link #stage}
     *
     * @return {@link #stage}
     */
    public static Stage getStage()
    {
        return stage;
    }

    /**
     * Switches a scene. This means a new GUI is shown.
     *
     * @param root The class acting as the root container for the GUI.
     */
    public static void switchScene(final Parent root)
    {
        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Closes this application.
     */
    public static void closeApp()
    {
        Platform.exit();
    }

    /**
     * Called when this applications starts. Sets the title an loads the first
     * GUI element.
     *
     * @param stage The stage for the GUI.
     * @throws IOException see {@link Application#start(Stage)}
     */
    @Override
    public void start(final Stage stage) throws IOException
    {
        ChatApp.stage = stage;
        stage.setTitle(TITLE);
        final URL peerChooserUrl = getClass().getResource(PeerChooserController.PEER_CHOOSER_FXML);
        final Parent root = FXMLLoader.load(peerChooserUrl);
        switchScene(root);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args)
    {
        launch(args);
    }
}
