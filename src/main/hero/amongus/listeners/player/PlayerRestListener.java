package hero.amongus.listeners.player;

import hero.amongus.cmd.mm.BuildCommand;
import hero.amongus.container.HotbarContainer;
import hero.amongus.game.types.ClassicMurder;
import net.hero.services.nms.NMS;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerRestListener implements Listener {

  @EventHandler
  public void onPlayerItemDamage(PlayerItemDamageEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null && profile.playingGame()) {
      evt.setCancelled(true);
      evt.setDamage(0);
      evt.getPlayer().updateInventory();
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      evt.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());
    if (profile != null) {
      ClassicMurder murder = profile.getGame(ClassicMurder.class);
      if (murder == null) {
        evt.setCancelled(true);
      } else if (evt.getItem().getItemStack().getType() == Material.GOLD_INGOT) {
        evt.setCancelled(true);
        if (murder.isSpectator(player) || murder.isKiller(player) || player.getInventory().contains(Material.BOW)) {
          return;
        }

        evt.getItem().remove();
        EnumSound.ITEM_PICKUP.play(player, 1.0F, 1.0F);
        NMS.sendActionBar(player, "§6Ouro coletado!");
        if (!player.getInventory().contains(Material.GOLD_INGOT)) {
          HotbarContainer config = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "innocentHotbar", HotbarContainer.class);
          player.getInventory().setItem(config.get("GO0", 8), new ItemStack(Material.GOLD_INGOT));
        } else {
          HotbarContainer config = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "innocentHotbar", HotbarContainer.class);
          int current = player.getInventory().getItem(config.get("GO0", 8)).getAmount();
          if (current >= 9) {
            player.getInventory().setItem(config.get("GO0", 8), new ItemStack(Material.AIR));
            player.sendMessage("§aVocê conseguiu uma arma com um mercenário e agora tem uma chance de acabar com os assassinatos!");
            HotbarContainer config2 = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "detectiveHotbar", HotbarContainer.class);
            player.getInventory().setItem(config2.get("BO0", 0), BukkitUtils.deserializeItemStack("BOW : 1 : nome>&6Arma do Mercenário"));
            player.getInventory().setItem(config2.get("AR0", 26), new ItemStack(Material.ARROW));
          } else {
            player.getInventory().getItem(config.get("GO0", 8)).setAmount(current + 1);
          }

          player.updateInventory();
        }
      } else if (murder.isSpectator(player) || !murder.isKiller(player)) {
        evt.setCancelled(true);
      } else if (murder.getDropItem() == null || !murder.getDropItem().equals(evt.getItem())) {
        evt.setCancelled(true);
      } else {
        murder.setSwordDrop(null);
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      evt.setCancelled(!BuildCommand.hasBuilder(evt.getPlayer()));
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      evt.setCancelled(!BuildCommand.hasBuilder(evt.getPlayer()));
    }
  }
}
