module com.example.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;


    exports com.example.chatjavafx;
    opens com.example.chatjavafx to javafx.fxml;

}