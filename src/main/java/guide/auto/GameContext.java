package guide.auto;

import core.Game;
import games.GameType;

public class GameContext {

    public IGameStrategy gameStrategy;

    public GameContext(GameType gameType) {
        if (gameType == GameType.Blackjack) {
            this.gameStrategy = new BlackjackGameStrategy();
        }
    }

    public void execute(String strategy, Game game) {
        gameStrategy.isValid(strategy, game);
    }

    public void exportJSON() {
        gameStrategy.exportJson();
    }

    public boolean isEnd() {
        return gameStrategy.isEnd();
    }
}
