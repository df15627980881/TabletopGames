package guide.auto;

import core.Game;

public interface IGameStrategy {

    boolean isValid(String strategy, Game game, Long seed);

    void exportJson();

    boolean isEnd();
}
