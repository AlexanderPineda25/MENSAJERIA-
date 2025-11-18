module frontend.client {
    requires javafx.controls;
    requires javafx.fxml;

    opens frontend.client to javafx.fxml;
    opens frontend.client.Controllers to javafx.fxml;
    opens frontend.client.Modelos to javafx.fxml;

    exports frontend.client;
    exports frontend.client.Controllers;
    exports frontend.client.Modelos;
    exports frontend.client.network;
    exports Shared;
    opens Shared to javafx.fxml;
}