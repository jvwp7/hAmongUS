package hero.amongus.cosmetics.object.winanimations;

import hero.amongus.cosmetics.object.AbstractExecutor;
import hero.amongus.nms.NMS;
import org.bukkit.entity.Player;

public class EnderDragonExecutor extends AbstractExecutor {

    public EnderDragonExecutor(Player player) {
        super(player);
        NMS.createMountableEnderDragon(player);
    }
}

