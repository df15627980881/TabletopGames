package guide;

import core.components.Card;

import java.util.List;

public class GuideContext {

    public static PreGameState deckForMechanism;

    /**
     * Don't forget to use copy() when assigning
     */
    public static List<PreGameState> deckForResult;
    public static int deckForResultIndex;
    public static List<PreGameState> deckForSimulate;
    public static int deckForSimulateIndex;

    public static InterfaceTech frame;

    /**
     * 0 - guide close
     * 1 - show game rule
     * 2 - show mechanism turn
     * 3 - show mechanism point
     * 4 - show game result
     * 5 - simulate actions by players
     * 6 - show actions executed by MCTS algorithm
     * 7 - show questions
     */
    public static GuideState guideStage;

    public enum GuideState {
        GUIDE_CLOSE("Guide Close"),
        SHOW_GAME_RULE("Show Game Rule"),
        SHOW_MECHANISM_TURN("Show Mechanism Turn"),
        SHOW_MECHANISM_POINT("Show Mechanism Point"),
        SHOW_GAME_RESULT("Show Game Result"),
        SIMULATE_ACTIONS_BY_PLAYERS("Simulate Actions by Players"),
        SHOW_ACTIONS_EXECUTED_BY_MCTS("Show Actions Executed by MCTS Algorithm"),
        SHOW_QUESTIONS("Show Questions");

        private final String description;

        GuideState(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}
