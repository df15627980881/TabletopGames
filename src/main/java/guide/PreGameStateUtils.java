package guide;

import core.CoreConstants;
import core.actions.AbstractAction;
import core.components.Card;
import core.components.Deck;
import core.components.FrenchCard;
import games.GameType;
import games.blackjack.actions.Hit;
import games.blackjack.actions.Stand;
import games.loveletter.actions.*;
import games.loveletter.cards.LoveLetterCard;
import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.JSONUtils;
import utilities.Pair;

import java.util.*;

public class PreGameStateUtils {

    public static PreGameState<? extends Card> get(GameType gameType, String path) {
        if (gameType == GameType.Blackjack) return getBlackjack(path);
        if (gameType == GameType.LoveLetter) return getLoveLetter(path);
        return null;
    }

    public static PreGameState<FrenchCard> getBlackjack(String path) {
        JSONObject jsonObject = JSONUtils.loadJSONFile(path);
        PreGameState<FrenchCard> result = new PreGameState<>();

        String gameResultDesc = (String) jsonObject.get("gameResultDesc");
        String strategy = (String) jsonObject.get("strategy");
        Integer playerCount = ((Long) jsonObject.get("playerCount")).intValue();

        JSONArray optionsArray = (JSONArray) jsonObject.get("actions");
        List<Pair<Long, AbstractAction>> actions = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(optionsArray)) {
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

        JSONObject simulateInfoObj = (JSONObject) jsonObject.get("simulateInfo");
        if (Objects.nonNull(simulateInfoObj)) {
            PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
            simulateInfo.setBeginActionIndex(((Long) simulateInfoObj.get("beginActionIndex")).intValue());
            simulateInfo.setIsSuccess((String) simulateInfoObj.get("isSuccess"));
            simulateInfo.setStartText((String) simulateInfoObj.get("startText"));
            simulateInfo.setSuccessText((String) simulateInfoObj.get("successText"));
            simulateInfo.setFailText((String) simulateInfoObj.get("failText"));
            JSONArray playerJA = (JSONArray) simulateInfoObj.get("players");
            List<String> players = new ArrayList<>();
            for (Object o : playerJA) {
                String player = (String) o;
                players.add(player);
            }
            simulateInfo.setPlayers(players);
            result.setSimulateInfo(simulateInfo);
        }

        result.setPlayerCount(playerCount);
        result.setPlayerIdAndActions(actions);
        result.setDrawDeck(deck);
        result.setGameResultDesc(gameResultDesc);
//        result.setStrategy(strategy);
        return result;
    }

    public static PreGameState<LoveLetterCard> getLoveLetter(String path) {
        JSONObject jsonObject = JSONUtils.loadJSONFile(path);
        PreGameState<LoveLetterCard> result = new PreGameState<>();

        String gameResultDesc = (String) jsonObject.get("gameResultDesc");
        String strategy = (String) jsonObject.get("strategy");
        Integer playerCount = ((Long) jsonObject.get("playerCount")).intValue();
        Long seed = (Long) jsonObject.get("seed");

        JSONArray optionsArray = (JSONArray) jsonObject.get("actions");
        List<Pair<Long, AbstractAction>> actions = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(optionsArray)) {
            for (Object obj : optionsArray) {
                JSONObject optionObj = (JSONObject) obj;
                Long playerId = (Long) optionObj.get("player");
                JSONObject actionObj = (JSONObject) optionObj.get("action");
                String cardType = (String) actionObj.get("cardType");
                String targetCardType = (String) actionObj.get("targetCardType");
                String forcedCountessCardType = (String) actionObj.get("forcedCountessCardType");
                Long targetPlayer = (Long) actionObj.get("targetPlayer");
                boolean canExecuteEffect = (boolean) actionObj.get("canExecuteEffect");
                boolean discard = (boolean) actionObj.get("discard");
                Long cardIdx = (Long) actionObj.get("cardIdx");
                AbstractAction action = null;
                if (LoveLetterCard.CardType.King.name().equals(cardType)) {
                    action = new KingAction(cardIdx.intValue(), playerId.intValue(), targetPlayer.intValue(), canExecuteEffect, discard);
                } else if (LoveLetterCard.CardType.Prince.name().equals(cardType)) {
                    action = new PrinceAction(cardIdx.intValue(), playerId.intValue(), targetPlayer.intValue(), canExecuteEffect, discard);
                } else if (LoveLetterCard.CardType.Priest.name().equals(cardType)) {
                    action = new PriestAction(cardIdx.intValue(), playerId.intValue(), targetPlayer.intValue(), canExecuteEffect, discard);
                } else if (LoveLetterCard.CardType.Baron.name().equals(cardType)) {
                    action = new BaronAction(cardIdx.intValue(), playerId.intValue(), targetPlayer.intValue(), canExecuteEffect, discard);
                } else if (LoveLetterCard.CardType.Guard.name().equals(cardType)) {
                    action = new GuardAction(cardIdx.intValue(), playerId.intValue(), targetPlayer.intValue(), targetCardType == null ? null : LoveLetterCard.CardType.valueOf(targetCardType), canExecuteEffect, discard);
                } else if (LoveLetterCard.CardType.Handmaid.name().equals(cardType)) {
                    action = new HandmaidAction(cardIdx.intValue(), playerId.intValue());
                } else if (LoveLetterCard.CardType.Countess.name().equals(cardType)) {
                    action = new PlayCard(LoveLetterCard.CardType.Countess, cardIdx.intValue(), playerId.intValue(), -1, null, forcedCountessCardType == null ? null : LoveLetterCard.CardType.valueOf(forcedCountessCardType), canExecuteEffect, discard);
                } else if (LoveLetterCard.CardType.Princess.name().equals(cardType)) {
                    action = new PlayCard(LoveLetterCard.CardType.Princess, cardIdx.intValue(), playerId.intValue(), -1, null, null, true, true);
                }
                assert action != null;
                actions.add(new Pair<>(playerId, action));
//                System.out.println(action == null);
//                actions.add(new Pair<>((long) action.getPlayerID(), (AbstractAction) optionObj.get("action")));
//                if ("Hit".equals(actionText)) {
//                    actions.add(new Pair<>(playerId, new Hit(playerId.intValue(), true, false)));
//                } else if ("Stand".equals(actionText)) {
//                    actions.add(new Pair<>(playerId, new Stand()));
//                }
            }
        }
        JSONArray decksArray = (JSONArray) jsonObject.get("decks");
        List<Deck<LoveLetterCard>> drawDecks = new ArrayList<>();
//        JSONObject deckObj = (JSONObject) jsonObject.get("deck");
        for (Object obj : decksArray) {
            JSONObject optionObj = (JSONObject) obj;
            String deckName = (String) optionObj.get("name");
            String visibilityMode = (String) optionObj.get("visibilityMode");
            Deck<LoveLetterCard> deck = new Deck<>(deckName, CoreConstants.VisibilityMode.valueOf(visibilityMode));


            JSONArray cardsArray = (JSONArray) optionObj.get("cards");
            for (Object cardObj : cardsArray) {
                JSONObject card = (JSONObject) cardObj;
                String type = (String) card.get("cardType");
                if (LoveLetterCard.CardType.Guard.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Guard));
                } else if (LoveLetterCard.CardType.Priest.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Priest));
                } else if (LoveLetterCard.CardType.Baron.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Baron));
                } else if (LoveLetterCard.CardType.Handmaid.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Handmaid));
                } else if (LoveLetterCard.CardType.Prince.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Prince));
                } else if (LoveLetterCard.CardType.King.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.King));
                } else if (LoveLetterCard.CardType.Countess.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Countess));
                } else if (LoveLetterCard.CardType.Princess.name().equals(type)) {
                    deck.add(new LoveLetterCard(LoveLetterCard.CardType.Princess));
                }
            }
//
            deck.reverse();
            drawDecks.add(deck);
        }

        JSONObject simulateInfoObj = (JSONObject) jsonObject.get("simulateInfo");
        if (Objects.nonNull(simulateInfoObj)) {
            PreGameState.SimulateInfo simulateInfo = new PreGameState.SimulateInfo();
            simulateInfo.setBeginActionIndex(((Long) simulateInfoObj.get("beginActionIndex")).intValue());
            simulateInfo.setIsSuccess((String) simulateInfoObj.get("isSuccess"));
            simulateInfo.setStartText((String) simulateInfoObj.get("startText"));
            simulateInfo.setSuccessText((String) simulateInfoObj.get("successText"));
            simulateInfo.setFailText((String) simulateInfoObj.get("failText"));
            JSONArray playerJA = (JSONArray) simulateInfoObj.get("players");
            List<String> players = new ArrayList<>();
            for (Object o : playerJA) {
                String player = (String) o;
                players.add(player);
            }
            simulateInfo.setPlayers(players);
            result.setSimulateInfo(simulateInfo);
        }

        result.setPlayerCount(playerCount);
        result.setPlayerIdAndActions(actions);
        result.setDrawDecks(drawDecks);
        result.setGameResultDesc(gameResultDesc);
//        result.setStrategy(strategy);
        result.setSeed(seed);
        result.setIndexx(0);

        return result;
    }

    public static void generateDeckJson() {
//        Deck<FrenchCard> deck = new Deck<>("aa", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        Set<FrenchCard> deck = new LinkedHashSet<>();
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Hearts));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Clubs));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Hearts));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 8));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Spades, 4));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Clubs, 6));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Spades, 6));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Hearts, 2));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Diamonds));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Spades, 10));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Hearts));

        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 6));
        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Clubs));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.King, FrenchCard.Suite.Clubs));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Queen, FrenchCard.Suite.Spades));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 9));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Spades, 10));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 2));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 6));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Ace, FrenchCard.Suite.Clubs));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Jack, FrenchCard.Suite.Diamonds));
//        deck.add(new FrenchCard(FrenchCard.FrenchCardType.Number, FrenchCard.Suite.Diamonds, 6));


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
        generateDeckJson();
//        getBlackjack();
    }
}
