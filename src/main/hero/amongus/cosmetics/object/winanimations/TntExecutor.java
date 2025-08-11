package hero.amongus.cosmetics.object.winanimations;

import hero.amongus.cosmetics.object.AbstractExecutor;
import org.bukkit.entity.*;

public class TntExecutor extends AbstractExecutor {

    public TntExecutor(Player player) {
        super(player);
    }

    @Override
    public void tick() {
        this.player.getWorld().spawn(player.getLocation().clone().add(Math.floor(Math.random() * 3.0D), 5, Math.floor(Math.random() * 3.0D)), TNTPrimed.class);
    }
}
