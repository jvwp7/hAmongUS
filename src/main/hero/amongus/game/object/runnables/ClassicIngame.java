package hero.amongus.game.object.runnables;

import hero.amongus.container.HotbarContainer;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.CosmeticType;
import hero.amongus.cosmetics.types.Knife;
import hero.amongus.game.types.ClassicMurder;
import net.hero.services.nms.NMS;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import hero.amongus.Language;
import hero.amongus.game.Murder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ClassicIngame extends BukkitRunnable {

  private ClassicMurder game;

  public ClassicIngame(Murder game) {
    this.game = (ClassicMurder) game;
  }

  @Override
  public void run() {
    if (this.game.getTimer() == 0) {
      this.game.stop(null);
      return;
    }

    if (this.game.getTimer() % 5 == 0) {
      Location gold = this.game.getConfig().getRandomGold();
      Item item = gold.getWorld().dropItem(gold, BukkitUtils.deserializeItemStack("GOLD_INGOT : 1 : display=" + ThreadLocalRandom.current().nextDouble()));
      item.setPickupDelay(0);
      item.setVelocity(new Vector());
    }

    List<Player> players = this.game.listPlayers();
    if (this.game.getTimer() > (Language.options$ingame$time - 15)) {
      if (this.game.getTimer() % 5 == 0) {
        String broadcast = Language.ingame$broadcast$knife.replace("{time}", String.valueOf(this.game.getTimer() - (Language.options$ingame$time - 15)))
          .replace("{s}", (this.game.getTimer() - (Language.options$ingame$time - 15)) > 1 ? "s" : "");
        players.forEach(player -> {
          EnumSound.CLICK.play(player, 1.0F, 1.0F);
          player.sendMessage(broadcast);
        });
      }
    } else if (this.game.getTimer() == (Language.options$ingame$time - 15)) {
      players.forEach(player -> {
        if (this.game.isKiller(player)) {
          Knife knife = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.KNIFE, Knife.class);

          HotbarContainer config2 = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "murderHotbar", HotbarContainer.class);

          player.getInventory().setItem(config2.get("DI0", 0), BukkitUtils.deserializeItemStack((knife == null ? "DIAMOND_SWORD" : knife.getItem().getType().name()) +
                  " : 1 : esconder>tudo : nome>&cFaca do Assassino"));
          player.getInventory().setItem(config2.get("CO0", 1), BukkitUtils.deserializeItemStack("COMPASS : 1 : nome>&aLocalizador"));
        }
        player.sendMessage(Language.ingame$broadcast$knife_received);
      });
    }

    players.forEach(player -> {
      if (!this.game.isSpectator(player)) {
        if (this.game.getDelay(player) != null) {
          int delay = this.game.getDelay(player);
          if (delay <= 0) {
            NMS.sendActionBar(player, "");
            HotbarContainer config2 = Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "detectiveHotbar", HotbarContainer.class);
            player.getInventory().setItem(config2.get("AR0", 26), BukkitUtils.deserializeItemStack("ARROW : 1"));
            player.updateInventory();
            this.game.removeDelay(player);
          } else {
            NMS.sendActionBar(player, "§6§lRECARREGANDO §8[§7" + delay + "s§8]");
          }
        } else if (this.game.getDropItem() != null && this.game.isKiller(player)) {
          int delay = 100 - (this.game.getDropItem().getTicksLived());
          if (delay <= 0) {
            NMS.sendActionBar(player, "");
            player.getInventory().addItem(this.game.getDropItem().getItemStack());
            this.game.getDropItem().remove();
            this.game.setSwordDrop(null);
            player.updateInventory();
          } else {
            NMS.sendActionBar(player, "§6§lRECUPERANDO FACA §8[§7" + ((delay / 20) + 1) + "s§8]");
          }
        }
      }

      Profile.getProfile(player.getName()).update();
    });

    this.game.setTimer(this.game.getTimer() - 1);
  }

  @Override
  public synchronized void cancel() throws IllegalStateException {
    super.cancel();
    this.game = null;
  }
}
