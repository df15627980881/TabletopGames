package guide.auto;

import core.Game;
import games.GameType;
import guide.GuideContext;

public class GameContext {

    public IGameStrategy gameStrategy;

    public GameContext(GameType gameType) {
        if (gameType == GameType.Blackjack) {
            this.gameStrategy = new BlackjackGameStrategy();
        }
        if (gameType == GameType.LoveLetter) {
            this.gameStrategy = new LoveLetterGameStrategy();
        }
        GuideContext.gameStrategy = this.gameStrategy;
    }

    public void execute(String strategy, Game game, Long seed) {
        gameStrategy.isValid(strategy, game, seed);
    }

    public void exportJSON() {
        gameStrategy.exportJson();
    }

    public boolean isEnd() {
        return gameStrategy.isEnd();
    }
}
