package plugins.implementation;

import javafx.scene.Scene;
import plugins.Plugin;

public class DarkThemePlugin implements Plugin {
    private final String DARK_CSS = getClass().getResource("/theme-dark.css").toExternalForm();

    @Override
    public String getName() {
        return "Dark Theme";
    }

    @Override
    public void onLoad(Scene scene) {
        if (scene != null && !scene.getStylesheets().contains(DARK_CSS)) {
            scene.getStylesheets().add(DARK_CSS);
        }
    }

    @Override
    public void onUnload(Scene scene) {
        if (scene != null) {
            scene.getStylesheets().remove(DARK_CSS);
        }
    }
}
