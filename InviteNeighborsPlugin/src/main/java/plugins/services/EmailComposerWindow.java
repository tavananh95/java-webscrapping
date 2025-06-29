package plugins.services;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import fxmodels.EventItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmailComposerWindow {
    public static void show(EventItem event) {
        Stage stage = new Stage();
        stage.setTitle("Invitez vos voisins");

        TextField toField = new TextField();
        toField.setPromptText("Ajouter des adresses e-mail séparées par des virgules");

        TextField subjectField = new TextField(event.getEventName() + " – ça vous parle ?");

        TextArea bodyArea = new TextArea(
                "Bonjour,\n\nVoici un évènement qui m'intéresse et peut-être vous aussi :\n\n" +
                        event.toFormattedString()
        );

        Button sendButton = new Button("Envoyer");
        sendButton.setOnAction(e -> {
            try {
                System.out.println("Send button clicked.");
                List<String> recipients = Arrays.stream(toField.getText().split(";"))
                        .map(String::trim)
                        .collect(Collectors.toList());

                EmailService.sendEmail(recipients, subjectField.getText(), bodyArea.getText());

                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        VBox layout = new VBox(10, new Label("À :"), toField,
                new Label("Sujet :"), subjectField,
                new Label("Message :"), bodyArea,
                sendButton);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 500, 400);
        stage.setScene(scene);
        stage.show();
    }
}
