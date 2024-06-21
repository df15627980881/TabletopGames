package games.blackjack;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import games.blackjack.actions.Hit;
import games.blackjack.actions.Stand;
import guide.DialogUtils;
import guide.GuideContext;
import guide.PreGameState;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

import static core.CoreConstants.GameResult.*;


public class BlackjackForwardModel extends StandardForwardModel {
    @Override
    protected void _setup(AbstractGameState firstState) {
        BlackjackGameState bjgs = (BlackjackGameState) firstState;
        bjgs.dealerPlayer = bjgs.getNPlayers() - 1;  // Dealer player is last

        //Create a deck
        bjgs.playerDecks = new ArrayList<>();

        //create the playing deck
        bjgs.drawDeck = FrenchCard.generateDeck("DrawDeck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        //shuffle the cards
        bjgs.drawDeck.shuffle(new Random((bjgs.getGameParameters().getRandomSeed())));
        if (GuideContext.guideStage == GuideContext.GuideState.SHOW_MECHANISM_TURN) {
            PreGameState<FrenchCard> preGameState = GuideContext.deckForMechanism;
            bjgs.drawDeck = preGameState.getDrawDeck().copy();
        } else if (GuideContext.guideStage == GuideContext.GuideState.SHOW_GAME_RESULT) {
            bjgs.drawDeck = GuideContext.deckForResult.get(GuideContext.deckForResultIndex).getDrawDeck().copy();
        } else if (GuideContext.guideStage == GuideContext.GuideState.SIMULATE_ACTIONS_BY_PLAYERS) {
            bjgs.drawDeck = GuideContext.deckForSimulate.get(GuideContext.deckForSimulateIndex).getDrawDeck().copy();
        }

        bjgs.setFirstPlayer(0);

        //Create a hand for each player
        boolean[] visibility = new boolean[firstState.getNPlayers()];
        Arrays.fill(visibility, true);
        for (int i = 0; i < bjgs.getNPlayers(); i++) {
            PartialObservableDeck<FrenchCard> playerDeck = new PartialObservableDeck<>("Player " + i + " deck", i, visibility);
            bjgs.playerDecks.add(playerDeck);
        }

        for (int card = 0; card < ((BlackjackParameters)bjgs.getGameParameters()).nCardsPerPlayer; card++) {
            for (int i = 0; i < bjgs.getNPlayers(); i++) {
                if (i == bjgs.dealerPlayer && i < ((BlackjackParameters)bjgs.getGameParameters()).nDealerCardsHidden) {
                    new Hit(i, false, true).execute(bjgs);
                } else {
                    new Hit(i).execute(bjgs);
                }
            }
        }

        if (GuideContext.guideStage == GuideContext.GuideState.SIMULATE_ACTIONS_BY_PLAYERS) {
            PartialObservableDeck<FrenchCard> playerDeck = bjgs.playerDecks.get(bjgs.getNPlayers()-1);
            // need allocate new address because it will modify bjgs directly by shallow copy
            visibility = new boolean[firstState.getNPlayers()];
            Arrays.fill(visibility, true);
            visibility[0] = false;
            ArrayList<boolean[]> elementVisibility = (ArrayList<boolean[]>) playerDeck.getElementVisibility();
            // Because PartialObservableDeck#add method add the element at the first place......
            elementVisibility.set(0, visibility);
            playerDeck.setVisibility(elementVisibility);
            bjgs.playerDecks.set(bjgs.getNPlayers()-1, playerDeck);
        }
    }

    @Override
    protected void _afterAction(AbstractGameState gameState, AbstractAction action){
        BlackjackGameState bjgs = (BlackjackGameState) gameState;
        if (action instanceof Hit) {
            Hit hit = (Hit)action;
            // Check if bust or win score
            int points = bjgs.calculatePoints(hit.playerID);
//            System.out.println("playerId: " + hit.playerID + " points: " + points);

            if (Objects.nonNull(GuideContext.frame) && points >=
                    ((BlackjackParameters)gameState.getGameParameters()).dealerStand
                    && hit.playerID == ((BlackjackGameState) gameState).dealerPlayer) {
                _endTurn((BlackjackGameState) gameState);
                return;
            }

            if (points > ((BlackjackParameters)gameState.getGameParameters()).winScore) {
                gameState.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, hit.playerID);
                if (Objects.nonNull(GuideContext.frame)) {
                    gameState.getDialogs().add(DialogUtils.create(GuideContext.frame, "Game Guide",
                            Boolean.TRUE, 300, 200, "<html><h2>Lose Game</h2><p>The player's" +
                                    " score is greater than 21.<p>"));
                }
                if (hit.advanceTurnOrder) {
                    _endTurn((BlackjackGameState) gameState);
                }
            } else if (points == ((BlackjackParameters)gameState.getGameParameters()).winScore) {
                gameState.setPlayerResult(CoreConstants.GameResult.WIN_GAME, hit.playerID);
                if (hit.advanceTurnOrder) {
                    _endTurn((BlackjackGameState) gameState);
                }
            }
        } else {
            _endTurn(bjgs);
        }
    }

    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        BlackjackGameState bjgs = (BlackjackGameState) gameState;
        ArrayList<AbstractAction> actions = new ArrayList<>();
        int player = bjgs.getCurrentPlayer();
        // Check if current player is the dealer.
        // Dealer must hit if score is <=16 otherwise must stand
        if (bjgs.getCurrentPlayer() == bjgs.dealerPlayer){
            if (Arrays.stream(bjgs.getPlayerResults()).filter(a -> a != LOSE_GAME).toList().size() == 1) {
                actions.add(new Stand());
            } else if (bjgs.calculatePoints(bjgs.dealerPlayer) >= ((BlackjackParameters) bjgs.getGameParameters()).dealerStand) {
                actions.add(new Stand());
            }
            else {
//                System.out.println("Hit");
                actions.add(new Hit(player, true, false));
            }
        }
        else {
            actions.add(new Hit(player, true, false));
            actions.add(new Stand());
        }

        return actions;
    }

    private void _endTurn(BlackjackGameState bjgs) {
        if (GuideContext.guideStage == GuideContext.GuideState.SIMULATE_ACTIONS_BY_PLAYERS && bjgs.getCurrentPlayer() == bjgs.dealerPlayer) {
            List<PartialObservableDeck<FrenchCard>> playerDecks = bjgs.playerDecks;
            boolean[] visibility = new boolean[bjgs.getNPlayers()];
            Arrays.fill(visibility, true);
            PartialObservableDeck<FrenchCard> playerDeck = playerDecks.get(bjgs.getNPlayers()-1);
            ArrayList<boolean[]> elementVisibility = (ArrayList<boolean[]>) playerDeck.getElementVisibility();
            for (int i=0; i<elementVisibility.size(); ++i) {
                elementVisibility.set(i, visibility);
            }
            playerDeck.setVisibility(elementVisibility);
            playerDecks.set(bjgs.getNPlayers()-1, playerDeck);
//            bjgs.setPlayerDecks(playerDecks);
        }
//        if (bjgs.getTurnCounter() >= bjgs.getNPlayers()) {
//        if (CollectionUtils.isNotEmpty(bjgs.getHistory()) &&
//                bjgs.getHistory().get(bjgs.getHistory().size()-1).a == bjgs.dealerPlayer &&
//                (bjgs.getHistory().get(bjgs.getHistory().size()-1).b instanceof Stand ||
//                        bjgs.getPlayerResults()[bjgs.getDealerPlayer()] == LOSE_GAME)) {
//        boolean isOnePlayerGameGoing = false;
//        for (int i=0; i<bjgs.getNPlayers()-1; ++i) {
//            isOnePlayerGameGoing |= bjgs.getPlayerResults()[i] == GAME_ONGOING;
//        }
//        System.out.println();
        if ((CollectionUtils.isNotEmpty(bjgs.getHistory())
                && bjgs.getHistory().get(bjgs.getHistory().size()-1).a == bjgs.dealerPlayer)) {
//            for (int i = 0; i < bjgs.getHistory().size(); i++) {
//                System.out.println(bjgs.getHistory().get(i).a + " " + bjgs.getHistory().get(i).b);
//            }
            // Everyone finished, game is over, assign results
            bjgs.setGameStatus(GAME_END);

            BlackjackParameters params = (BlackjackParameters) bjgs.getGameParameters();

            int[] score = new int[bjgs.getNPlayers()];
            for (int j = 0; j < bjgs.getNPlayers(); j++){
                if (bjgs.getPlayerResults()[j] != LOSE_GAME) {
                    score[j] = bjgs.calculatePoints(j);
                }
            }
            bjgs.setPlayerResult(GAME_END, bjgs.dealerPlayer);
            if (score[bjgs.dealerPlayer] > params.winScore) {
                // Dealer went bust, everyone else wins
                bjgs.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, bjgs.dealerPlayer);
            }
            for (int i = 0; i < bjgs.getNPlayers()-1; i++) {  // Check all players and compare to dealer
                if (bjgs.getPlayerResults()[i] != LOSE_GAME) {
                    if (score[bjgs.dealerPlayer] > params.winScore) {
                        // Dealer went bust, everyone else wins
                        bjgs.setPlayerResult(CoreConstants.GameResult.WIN_GAME, i);
                    } else if (score[bjgs.dealerPlayer] > score[i]) {
                        bjgs.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, i);
                    } else if (score[bjgs.dealerPlayer] < score[i]) {
                        bjgs.setPlayerResult(CoreConstants.GameResult.WIN_GAME, i);
                    } else if (score[bjgs.dealerPlayer] == score[i]) {
                        bjgs.setPlayerResult(CoreConstants.GameResult.DRAW_GAME, i);
                    }
                }
            }

            // set dealer's result
            if (bjgs.getPlayerResults()[bjgs.dealerPlayer] != CoreConstants.GameResult.LOSE_GAME) {
                int maxScore = Arrays.stream(score).filter(x -> x <= params.winScore).max().getAsInt();
                for (int i = 0; i < bjgs.getNPlayers() - 1; i++) {
                    if (score[i] > score[bjgs.dealerPlayer]) {
                        bjgs.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, bjgs.dealerPlayer);
                        break;
                    }
                    if (score[i] == maxScore && score[bjgs.dealerPlayer] == maxScore) {
                        bjgs.setPlayerResult(CoreConstants.GameResult.DRAW_GAME, bjgs.dealerPlayer);
                        break;
                    }
                    if (i == bjgs.getNPlayers() - 2) {
                        bjgs.setPlayerResult(CoreConstants.GameResult.WIN_GAME, bjgs.dealerPlayer);
                    }
                }
            }

            for (int i = 0; i < bjgs.getNPlayers(); i++) {
                if (bjgs.getPlayerResults()[i] == GAME_ONGOING) {
                    bjgs.setPlayerResult(LOSE_GAME, i);
                }
            }

        }
        else {
//            for (Pair<Integer, AbstractAction> integerAbstractActionPair : bjgs.getHistory()) {
//                System.out.println("aa" + integerAbstractActionPair.a + " " + integerAbstractActionPair.b);
//            }
//            for (CoreConstants.GameResult playerResult : bjgs.getPlayerResults()) {
//                System.out.println(playerResult);
//            }
//            System.out.println("***************");
            endPlayerTurn(bjgs);
        }
    }

}