package hero.amongus.game.object.runnables;

import net.hero.services.libraries.npclib.npc.ai.NPCHolder;
import hero.amongus.game.types.AssassinsMurder;
import net.hero.services.nms.NMS;
import net.hero.services.player.Profile;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import hero.amongus.Language;
import hero.amongus.game.Murder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AssassinsIngame extends BukkitRunnable {

  private AssassinsMurder game;

  public AssassinsIngame(Murder game) {
    this.game = (AssassinsMurder) game;
  }

  private List<LivingEntity> getNearbyEntities(Location location, double range) {
    List<LivingEntity> entities = new ArrayList<>();
    for (Entity entity : location.getWorld().getEntities()) {
      if (entity instanceof LivingEntity && !(entity instanceof ArmorStand) && !(entity instanceof NPCHolder) && entity.getLocation().distance(location) <= range) {
        entities.add((LivingEntity) entity);
      }
    }

    return entities;
  }

  @Override
  public void run() {
    if (this.game.getTimer() == 0) {
      this.game.stop(null);
      return;
    }

    List<Player> players = this.game.listPlayers();
    if (this.game.getTimer() > (Language.options$ingame$time - 10)) {
      if (this.game.getTimer() <= (Language.options$ingame$time - 5) || this.game.getTimer() == (Language.options$ingame$time - 10)) {
        String broadcast = Language.ingame$broadcast$contract.replace("{time}", String.valueOf(this.game.getTimer() - (Language.options$ingame$time - 10)))
          .replace("{s}", (this.game.getTimer() - (Language.options$ingame$time - 10)) > 1 ? "s" : "");
        players.forEach(player -> {
          EnumSound.CLICK.play(player, 1.0F, 1.0F);
          player.sendMessage(broadcast);
        });
      }
    } else if (this.game.getTimer() == (Language.options$ingame$time - 10)) {
      this.game.createContracts();
    }

    players.forEach(player -> {
      if (!this.game.isSpectator(player)) {
        if (this.game.getDelay(player) != null) {
          int delay = this.game.getDelay(player);
          if (delay <= 0) {
            NMS.sendActionBar(player, "");
            this.game.removeDelay(player);
          } else {
            NMS.sendActionBar(player, "§6§lRECARREGANDO §8[§7" + delay + "s§8]");
          }
        }

        ItemStack item = player.getItemInHand();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getType().equals(Material.COMPASS)) {
          String name = game.getContract(player);
          if (name != null && Bukkit.getPlayerExact(name) != null) {
            Player target = Bukkit.getPlayerExact(name);
            player.setCompassTarget(target.getLocation());
            NMS.sendActionBar(player, "§7Rastreando: " + target + " §a(" + new DecimalFormat("###.#").format(player.getLocation().distance(target.getLocation())) + " Blocos)");
          }
        }

        getNearbyEntities(player.getLocation(), 10.0D).forEach(collect -> {
          if (!(collect instanceof Player) || !this.game.isContract(player, (Player) collect) || this.game.isSpectator((Player) collect)) {
            return;
          }
          EnumSound.CLICK.play((Player) collect, 1.0F, 2.0F);
          NMS.sendActionBar((Player) collect, "§c[❤]");
        });
        getNearbyEntities(player.getLocation(), 10.0D).forEach(collect -> {
          if (!(collect instanceof Player) || !this.game.isContract((Player) collect, player) || this.game.isSpectator(player)) {
            return;
          }

          EnumSound.CLICK.play(player, 1.0F, 2.0F);
          NMS.sendActionBar(player, "§c[❤]");
        });
      }

      Profile.getProfile(player.getName()).update();
    });

    this.game.setTimer(game.getTimer() - 1);
  }

  @Override
  public synchronized void cancel() throws IllegalStateException {
    super.cancel();
    this.game = null;
  }
}
