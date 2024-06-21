package guide;

import games.GameType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MechanismEnum {

    DIRECTION(GameType.Blackjack, "Direction", 1),
    SOFT_HAND(GameType.Blackjack, "Soft Hand", 3),
    POINT(GameType.Blackjack, "Point", 2),
    ALL(GameType.Blackjack, "All", 0);

    private final GameType gameType;

    private final String description;

    private final int order;

    MechanismEnum(GameType gameType, String description, int order) {
        this.gameType = gameType;
        this.description = description;
        this.order = order;
    }

    public String getDescription() {
        return description;
    }

    public int getOrder() {
        return order;
    }

    public static List<MechanismEnum> getByGameType(GameType gameType) {
        return Arrays.stream(values()).filter(x -> x.gameType == gameType).collect(Collectors.toList());
    }
}
