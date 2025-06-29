package plugins.implementation;

import fxmodels.EventItem;
import plugins.services.EmailComposerWindow;
import plugins.EventActionPlugin;

public class InviteNeighborsPlugin implements EventActionPlugin {
    @Override
    public String getActionLabel() {
        return "Invitez mes voisins";
    }

    @Override
    public void onActionTriggered(EventItem event) {
        EmailComposerWindow.show(event);
    }
}
