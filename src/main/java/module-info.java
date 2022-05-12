module com.example.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    exports com.example.chatjavafx;
    opens com.example.chatjavafx to javafx.fxml;

}