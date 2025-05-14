package plugins;

import fxmodels.EventItem;

public interface EventActionPlugin {
    String getActionLabel(); // "Invitez mes voisins"
    void onActionTriggered(EventItem event); // ce qui se passe au clic
}
