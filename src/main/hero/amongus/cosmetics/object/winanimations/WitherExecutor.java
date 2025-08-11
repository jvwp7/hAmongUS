package hero.amongus.cosmetics.object.winanimations;

import hero.amongus.Main;
import hero.amongus.cosmetics.object.AbstractExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class WitherExecutor extends AbstractExecutor implements Listener {

    private Wither wither;

    public WitherExecutor(Player player) {
        super(player);
        this.wither = player.getWorld().spawn(player.getLocation(), Wither.class);
        this.wither.setPassenger(player);

        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public void cancel() {
        this.wither.remove();
        this.wither = null;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent evt) {
        if (evt.getEntity() instanceof Wither) {
            evt.setCancelled(true);
        }
    }
}
