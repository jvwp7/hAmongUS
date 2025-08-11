package hero.amongus.listeners.player;

import hero.amongus.cmd.mm.BuildCommand;
import hero.amongus.cmd.mm.CreateCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import hero.amongus.tagger.TagUtils;

public class PlayerQuitListener implements Listener {

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    evt.setQuitMessage(null);
    BuildCommand.remove(evt.getPlayer());
    TagUtils.reset(evt.getPlayer().getName());
    CreateCommand.CREATING.remove(evt.getPlayer());
  }
}
