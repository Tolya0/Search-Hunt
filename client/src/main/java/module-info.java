module org.kurilin.recruitment.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires com.google.gson;
    requires org.kurilin.recruitment.shared;
    requires atlantafx.base;
    requires static lombok;

    opens org.kurilin.recruitment.client to javafx.fxml;
    opens org.kurilin.recruitment.client.controller to javafx.fxml;
    exports org.kurilin.recruitment.client;
}
