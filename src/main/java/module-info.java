module com.example.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.chatjavafx to javafx.fxml;
    exports com.example.chatjavafx;
}