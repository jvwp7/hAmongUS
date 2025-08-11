package hero.amongus.cosmetics.object.winanimations;

import hero.amongus.cosmetics.object.AbstractExecutor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class CartExecutor extends AbstractExecutor {

    private Entity cart;

    public CartExecutor(Player player) {
        super(player);
        this.cart = player.getWorld().spawn(player.getLocation(), Minecart.class);
    }

    @Override
    public void tick() {
        this.cart.teleport(player.getLocation().add(0.5, 0, 0.5));
    }

    @Override
    public void cancel() {
        this.cart.remove();
        this.cart = null;
    }
}
