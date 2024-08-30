package guide.param;

import core.AbstractPlayer;
import core.CoreParameters;
import evaluation.optimisation.TunableParameters;
import games.GameType;

import java.util.List;

public class GuideGenerateParam {

    private GameType gameType;
    private List<AbstractPlayer> players;
    private TunableParameters params;
    private String parameterConfigFile;

    public String getParameterConfigFile() {
        return parameterConfigFile;
    }

    public void setParameterConfigFile(String parameterConfigFile) {
        this.parameterConfigFile = parameterConfigFile;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public List<AbstractPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<AbstractPlayer> players) {
        this.players = players;
    }

    public TunableParameters getParams() {
        return params;
    }

    public void setParams(TunableParameters params) {
        this.params = params;
    }
}
