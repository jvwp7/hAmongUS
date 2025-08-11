package hero.amongus.listeners.player;

import net.hero.services.player.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import hero.amongus.Main;
import hero.amongus.game.Murder;

import java.util.List;

public class PlayerDeathListener implements Listener {

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent evt) {
    Player player = evt.getEntity();
    evt.setDeathMessage(null);

    Profile profile = Profile.getProfile(player.getName());
    if (profile != null) {
      evt.setDroppedExp(0);
      evt.getDrops().clear();
      player.setHealth(20.0);

      Murder game = profile.getGame(Murder.class);
      if (game == null) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), profile::refresh, 3);
      } else {
        List<Profile> hitters = profile.getLastHitters();
        game.kill(profile, hitters.size() > 0 ? hitters.get(0) : null);
        hitters.clear();
      }
    }
  }
}
