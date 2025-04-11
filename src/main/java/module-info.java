/**
 * For demonstration purposes and for avoiding some JavaFX warnings, the RoboRally
 * application is now configured as a Java module.
 */
module rest_client {
    // JavaFX modules
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.base;

    // Spring and HTTP modules
    requires spring.web;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    // Export your app’s packages so they can be used by JavaFX and Spring
    exports dk.dtu.compute.course02324.part4.consuming_rest;
    exports dk.dtu.compute.course02324.part4.consuming_rest.model;
    exports dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

    // Allow reflection access (optional but might be needed)
    opens dk.dtu.compute.course02324.part4.consuming_rest.model to com.fasterxml.jackson.databind;
}
