/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author Nitros
 */
public class ChatApp extends Application
{
    private static final String TITLE = "SubSpace Test Chat";

    private static Stage stage;

    public static void switchScene(final Parent root)
    {
        final Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setScene(scene);
        stage.show();
    }

    public static void closeApp()
    {
        Platform.exit();
    }

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
