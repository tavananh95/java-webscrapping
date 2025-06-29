package org.updater;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class Updater extends Application {

    private static final String LATEST_VERSION_URL = "https://raw.githubusercontent.com/tavananh95/java-webscrapping/main/latest-version.txt";
    private static final String DOWNLOAD_URL = "https://github.com/tavananh95/java-webscrapping/releases/download/1.0-SNAPSHOT/event-scraper-app-v1.0-SNAPSHOT.jar";
    private static final String APP_JAR = "event-scraper-app.jar";

    @Override
    public void start(Stage stage) {
        checkAndUpdate();
    }

    private String getCurrentAppVersion() {
        try (JarFile jar = new JarFile(APP_JAR)) {
            Manifest manifest = jar.getManifest();
            Attributes attr = manifest.getMainAttributes();
            String version = attr.getValue("Implementation-Version");
            return (version != null) ? version : "unknown";
        } catch (IOException e) {
            System.out.println("Failed to read app version: " + e.getMessage());
            return "unknown";
        }
    }

    private void checkAndUpdate() {
        try {
            String latestVersion = fetchLatestVersion();
            if (!isUpToDate(latestVersion)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "A new version (" + latestVersion + ") is available. Update now?",
                        ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        downloadAndReplace();
                        launchMainApp();
                    } else {
                        launchMainApp();
                    }
                });
            } else {
                launchMainApp();
            }
        } catch (Exception e) {
            System.out.println("Update check failed: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to check for updates. Launching app anyway.", ButtonType.OK);
            alert.showAndWait();
            launchMainApp();
        }

    }

    private String fetchLatestVersion() throws IOException {
        URL url = new URL(LATEST_VERSION_URL);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return in.readLine().trim();
        }
    }

    private boolean isUpToDate(String latestVersion) {
        String currentVersion = getCurrentAppVersion();
        if (currentVersion.equals("unknown")) {
            return false; // force update if cannot read version
        }
        System.out.println("Current version: " + currentVersion + ", Latest version: " + latestVersion);
        return currentVersion.equals(latestVersion);
    }


    private void downloadAndReplace() {
        try (InputStream in = new URL(DOWNLOAD_URL).openStream()) {
            Files.copy(in, Paths.get(APP_JAR), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Updated to latest version.");
        } catch (IOException e) {
            System.out.println("Download failed: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to download update: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }


    private void launchMainApp() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", APP_JAR);
            pb.inheritIO();
            pb.start();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Failed to launch main app: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
