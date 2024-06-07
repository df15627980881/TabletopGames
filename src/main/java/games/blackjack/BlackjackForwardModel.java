package games.blackjack;

import com.beust.ah.A;
import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.FrenchCard;
import core.components.PartialObservableDeck;
import evaluation.metrics.Event;
import games.blackjack.actions.Hit;
import games.blackjack.actions.Stand;
import guide.DialogUtils;
import guide.InterfaceTech;
import guide.PreGameState;
import guide.PreGameStateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.testng.collections.Lists;
import utilities.Pair;

import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

import static core.CoreConstants.GameResult.*;
import static core.CoreConstants.GameResult.LOSE_GAME;
import static evaluation.metrics.Event.GameEvent.TURN_OVER;


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

        if (AbstractGameState.isGuide) {
            PreGameState preGameState = PreGameStateUtils.getBlackjack();
            bjgs.drawDeck = preGameState.getDrawDeck();
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
    }

    @Override
    protected void _afterAction(AbstractGameState gameState, AbstractAction action){
        BlackjackGameState bjgs = (BlackjackGameState) gameState;
        System.out.println("####" + action);
        if (action instanceof Hit) {
            Hit hit = (Hit)action;
            // Check if bust or win score
            int points = bjgs.calculatePoints(hit.playerID);
//            System.out.println("playerId: " + hit.playerID + " points: " + points);

            if (AbstractGameState.isGuide && points >= ((BlackjackParameters)gameState.getGameParameters()).dealerStand && hit.playerID == ((BlackjackGameState) gameState).dealerPlayer) {
                System.out.println("mcakjckas");
                _endTurn((BlackjackGameState) gameState);
                return;
            }

            if (points > ((BlackjackParameters)gameState.getGameParameters()).winScore) {
                gameState.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, hit.playerID);
                if (Objects.nonNull(gameState.getFrame())) {
                    gameState.getDialogs().add(DialogUtils.create(gameState.getFrame(), "Game Guide",
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
//        if (bjgs.getTurnCounter() >= bjgs.getNPlayers()) {
//        if (CollectionUtils.isNotEmpty(bjgs.getHistory()) &&
//                bjgs.getHistory().get(bjgs.getHistory().size()-1).a == bjgs.dealerPlayer &&
//                (bjgs.getHistory().get(bjgs.getHistory().size()-1).b instanceof Stand ||
//                        bjgs.getPlayerResults()[bjgs.getDealerPlayer()] == LOSE_GAME)) {
        if (CollectionUtils.isNotEmpty(bjgs.getHistory())
                && bjgs.getHistory().get(bjgs.getHistory().size()-1).a == bjgs.dealerPlayer) {
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