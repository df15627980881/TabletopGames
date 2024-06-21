package guide;

import games.GameType;

public class GameMechanismForGuide {

    /**
     * Because BlackjackMechanismGUI already inherits BlackjackGUIManager, Java cannot inherit multiple classes,
     * so there is no way to declare the MechanismGUI for each game in the GameType
     */
    private GameType gameType;

}
