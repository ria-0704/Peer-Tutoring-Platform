module com.peertutor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.logging;

    opens com.peertutor             to javafx.graphics;
    opens com.peertutor.controller  to javafx.fxml, javafx.graphics;
    opens com.peertutor.model       to javafx.base, javafx.controls;
}
