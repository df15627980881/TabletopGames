package guide;

import core.actions.AbstractAction;
import games.blackjack.actions.Hit;
import games.blackjack.actions.Stand;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.Int;
import utilities.JSONUtils;
import utilities.Pair;

import java.util.ArrayList;
import java.util.List;

public class PreGameStateUtils {

    public static PreGameState getBlackjack() {
        JSONObject jsonObject = JSONUtils.loadJSONFile("data/preGameState/Blackjack.json");
        PreGameState result = new PreGameState();

        Long seed = (Long) jsonObject.get("seed");
        JSONArray optionsArray = (JSONArray) jsonObject.get("actions");
        List<Pair<Long, AbstractAction>> actions = new ArrayList<>();

        for (Object obj : optionsArray) {
            JSONObject optionObj = (JSONObject) obj;
            Long playerId = (Long) optionObj.get("player");
            String actionText = (String) optionObj.get("action");
            if (actionText.startsWith("Hit")) {
                String[] d = actionText.split("#");
                actions.add(new Pair<>(playerId, new Hit(Integer.parseInt(d[1]))));
            } else if ("Stand".equals(actionText)) {
                actions.add(new Pair<>(playerId, new Stand()));
            }
        }

        result.setPlayerIdAndActions(actions);
        result.setSeed(seed);
        return result;
    }

    public static void generateDeckJson() {

    }
}
