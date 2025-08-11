package hero.amongus.listeners.player;

import net.hero.services.Core;
import net.hero.services.nms.NMS;
import net.hero.services.player.Profile;
import net.hero.services.player.hotbar.Hotbar;
import net.hero.services.player.role.Role;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.hook.MMCoreHook;
import hero.amongus.tagger.TagUtils;

public class PlayerJoinListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent evt) {
    evt.setJoinMessage(null);

    Player player = evt.getPlayer();
    TagUtils.sendTeams(player);

    Profile profile = Profile.getProfile(player.getName());
    MMCoreHook.reloadScoreboard(profile);
    profile.setHotbar(Hotbar.getHotbarById("lobby"));
    profile.refresh();
  
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> TitleManager.joinLobby(profile), 10);

    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
      TagUtils.setTag(evt.getPlayer());
      
      if (Role.getPlayerRole(player).isBroadcast()) {
        String broadcast = Language.lobby$broadcast.replace("{player}", Role.getPrefixed(player.getName()));
        Profile.listProfiles().forEach(pf -> {
          if (!pf.playingGame()) {
            Player players = pf.getPlayer();
            if (players != null) {
              players.sendMessage(broadcast);
            }
          }
        });
      }
    }, 5);

    NMS.sendTitle(player, "", "", 0, 1, 0);
    if (Language.lobby$tab$enabled) {
      NMS.sendTabHeaderFooter(player, Language.lobby$tab$header, Language.lobby$tab$footer);
    }

    // Removed kMysteryBox related code
  }
}
