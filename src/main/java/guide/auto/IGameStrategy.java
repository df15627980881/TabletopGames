package guide.auto;

import core.Game;

public interface IGameStrategy {

    boolean isValid(String strategy, Game game);

    void exportJson();

    boolean isEnd();
}
