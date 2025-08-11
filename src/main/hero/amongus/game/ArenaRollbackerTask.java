package hero.amongus.game;

import net.hero.services.game.GameState;
import hero.amongus.Language;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static hero.amongus.game.Murder.QUEUE;

public class ArenaRollbackerTask extends BukkitRunnable {

  private Murder rollbacking;

  @Override
  public void run() {
    if (this.rollbacking != null) {
      this.rollbacking.getWorld().getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
      this.rollbacking.setState(GameState.AGUARDANDO);
      this.rollbacking.setTimer(Language.options$start$waiting + 1);
      this.rollbacking.getTask().reset();
      this.rollbacking = null;
    } else {
      if (!QUEUE.isEmpty()) {
        this.rollbacking = QUEUE.get(0);
        QUEUE.remove(0);
      }
    }
  }
}
