package com.example.chatjavafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {

    @FXML
    private TextArea messageArea;

    @FXML
    private TextField messageField;

    @FXML
    private void checkButtonClick(ActionEvent actionEvent) {
        String text = messageField.getText();
        if (text.length() > 0) {
            messageArea.appendText(messageField.getText() + "\n");
            messageField.setText("");
        }
    }
}