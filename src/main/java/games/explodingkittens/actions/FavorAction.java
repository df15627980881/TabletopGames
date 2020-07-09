package games.explodingkittens.actions;

import core.actions.AbstractAction;
import core.actions.DrawCard;
import core.AbstractGameState;
import core.interfaces.IPrintable;
import games.explodingkittens.ExplodingKittensTurnOrder;
import games.explodingkittens.ExplodingKittensGameState;
import core.turnorders.TurnOrder;

import java.util.Objects;

import static games.explodingkittens.ExplodingKittensGameState.ExplodingKittensGamePhase.Favor;

public class FavorAction extends DrawCard implements IsNopeable, IPrintable {
    final int target;

    public FavorAction(int deckFrom, int deckTo, int index, int target) {
        super(deckFrom, deckTo, index);
        this.target = target;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        super.execute(gs);

        ExplodingKittensGameState ekgs = ((ExplodingKittensGameState)gs);
        ekgs.setGamePhase(Favor);
        ekgs.setPlayerGettingAFavor(gs.getTurnOrder().getCurrentPlayer(gs));

        ExplodingKittensTurnOrder ekto = (ExplodingKittensTurnOrder) gs.getTurnOrder();
        ekto.registerFavorAction(target);
        return true;
    }

    public boolean nopedExecute(AbstractGameState gs, TurnOrder turnOrder) {
        return super.execute(gs);
    }

    @Override
    public String toString(){//overriding the toString() method
        return String.format("Player asks Player %d for a favor", target);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return String.format("Ask player %d for a favor", target);
    }

    @Override
    public void printToConsole(AbstractGameState gameState) {
        System.out.println(this.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavorAction)) return false;
        if (!super.equals(o)) return false;
        FavorAction that = (FavorAction) o;
        return target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), target);
    }

    @Override
    public AbstractAction copy() {
        return new FavorAction(deckFrom, deckTo, fromIndex, target);
    }

}
