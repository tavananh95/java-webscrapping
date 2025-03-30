package org.playground;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class ButtonController {

    @FXML
    private Button button;  // This links to the button in FXML

    @FXML
    private void handleButtonClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Button Clicked");
        alert.setHeaderText(null);
        alert.setContentText("Hello from JavaFX!");
        alert.showAndWait();
    }
}
