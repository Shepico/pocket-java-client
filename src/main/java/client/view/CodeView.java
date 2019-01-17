package client.view;

import com.github.twalcari.prettify.RTSyntaxHighlighter;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

public class CodeView extends Application {

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            /*VBox root = new VBox();
            TextArea textArea = new TextArea();
            root.getChildren().add(textArea);*/

            CodeArea codeArea = new CodeArea("public static void main(){}");

            RTSyntaxHighlighter xmlCodeHighlighter = new RTSyntaxHighlighter(codeArea, "java");
            //textArea.setText(xmlCodeHighlighter..toString());
            BorderPane root = new BorderPane(codeArea);
            Scene scene = new Scene(root, 400, 400);
    //      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
