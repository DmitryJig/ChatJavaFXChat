module com.example.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.commons.io;
    requires org.apache.logging.log4j;


    exports com.example.chatjavafx;
    opens com.example.chatjavafx to javafx.fxml;

}