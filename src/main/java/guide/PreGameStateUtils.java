package guide;

import core.CoreConstants;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import games.blackjack.actions.Hit;
import games.blackjack.actions.Stand;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.Int;
import utilities.JSONUtils;
import utilities.Pair;

import java.util.*;

public class PreGameStateUtils {

    public static PreGameState getBlackjack() {
        JSONObject jsonObject = JSONUtils.loadJSONFile("data/preGameState/Blackjack.json");
        PreGameState result = new PreGameState();

        JSONArray optionsArray = (JSONArray) jsonObject.get("actions");
        List<Pair<Long, AbstractAction>> actions = new ArrayList<>();

        for (Object obj : optionsArray) {
            JSONObject optionObj = (JSONObject) obj;
            Long playerId = (Long) optionObj.get("player");
            String actionText = (String) optionObj.get("action");
            if ("Hit".equals(actionText)) {
                actions.add(new Pair<>(playerId, new Hit(playerId.intValue(), true, false)));
            } else if ("Stand".equals(actionText)) {
                actions.add(new Pair<>(playerId, new Stand()));
            }
        }

        JSONObject deckObj = (JSONObject) jsonObject.get("deck");
        String deckName = (String) deckObj.get("name");
        String visibilityMode = (String) deckObj.get("visibilityMode");
        Deck<FrenchCard> deck = new Deck<>(deckName, CoreConstants.VisibilityMode.valueOf(visibilityMode));

        JSONArray cardsArray = (JSONArray) deckObj.get("cards");
        for (Object cardObj : cardsArray) {
            JSONObject card = (JSONObject) cardObj;
            String type = (String) card.get("type");
            String suite = (String) card.get("suite");
            FrenchCard.FrenchCardType cardType = FrenchCard.FrenchCardType.valueOf(type);
            FrenchCard.Suite cardSuite = FrenchCard.Suite.valueOf(suite);
            if (FrenchCard.FrenchCardType.Number.name().equals(type)) {
                int cardNumber = Integer.parseInt((String) card.get("number"));
                deck.add(new FrenchCard(cardType, cardSuite, cardNumber));
            } else {
                deck.add(new FrenchCard(cardType, cardSuite));
            }
        }

        deck.reverse();

        result.setPlayerIdAndActions(actions);
        result.setDrawDeck(deck);
        return result;
    }

    public static void generateDeckJson() {
//        Deck<FrenchCard> deck = new Deck<>("aa", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        Set<FrenchCard> deck = new LinkedHashSet<>();
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 5));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 10));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Spades));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Hearts));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Hearts, 10));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Clubs, 7));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Clubs, 8));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Spades));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Spades, 5));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 8));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Clubs, 2));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 7));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 6));


        for (int j=2; j<=10; ++j) {
            deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, j));
        }
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Diamonds));
        for (int j=2; j<=10; ++j) {
            deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Clubs, j));
        }
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Clubs));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Clubs));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Clubs));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Clubs));
        for (int j=2; j<=10; ++j) {
            deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Hearts, j));
        }
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Hearts));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Hearts));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Hearts));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Hearts));
        for (int j=2; j<=10; ++j) {
            deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Spades, j));
        }
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Spades));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Spades));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Spades));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Spades));

        for (FrenchCard frenchCard : deck) {
            System.out.println("{");
            if (frenchCard.type == FrenchCard.FrenchCardType.Number) {
                System.out.println("\"type\": \"" + frenchCard.type + "\",");
                System.out.println("\"suite\": \"" + frenchCard.suite + "\",");
                System.out.println("\"number\": \"" + frenchCard.number + "\"");
            } else {
                System.out.println("\"type\": \"" + frenchCard.type + "\",");
                System.out.println("\"suite\": \"" + frenchCard.suite + "\"");
            }
            System.out.println("},");
        }
    }

    public static void main(String[] args) {
//        generateDeckJson();
        getBlackjack();
    }
}
