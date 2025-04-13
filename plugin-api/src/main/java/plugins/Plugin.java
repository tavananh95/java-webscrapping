package plugins;

import javafx.scene.Scene;

public interface Plugin {
    String getName();
    void onLoad(Scene scene);
    void onUnload(Scene scene);
}
