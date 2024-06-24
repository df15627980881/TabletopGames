package guide.auto;

import core.AbstractPlayer;
import core.Game;
import games.GameType;

import java.util.ArrayList;
import java.util.List;

import static guide.auto.LoveLetterGameStrategy.tmpCardsForReserve;
import static guide.auto.LoveLetterGameStrategy.tmpCardsForReserveSwitch;


public class AutoGenerateDeck {

    public static boolean generate(List<AbstractPlayer> players, String strategy, GameType gameType) {
        GameContext gameContext = new GameContext(gameType);
        while (true) {
            if (gameContext.isEnd()) {
                gameContext.exportJSON();
                tmpCardsForReserveSwitch = false;
                break;
            }
            tmpCardsForReserveSwitch = true;
            tmpCardsForReserve = new ArrayList<>();
            long seed = System.currentTimeMillis();
            Game game = Game.runOne(gameType, null, players, seed, false, null, null, 1);
            gameContext.execute(strategy, game, seed);
        }
        return true;
    }
}
