package hero.amongus.cmd.mm;

import net.hero.services.player.Profile;
import net.hero.services.player.hotbar.Hotbar;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import hero.amongus.Main;
import hero.amongus.cmd.SubCommand;
import hero.amongus.game.Murder;
import hero.amongus.game.enums.MurderMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CreateCommand extends SubCommand {

  public static final Map<Player, Object[]> CREATING = new HashMap<>();

  public CreateCommand() {
    super("criar", "criar [classic/assassins] [nome]", "Criar uma sala.", true);
  }

  @Override
  public void perform(Player player, String[] args) {
    if (Murder.getByWorldName(player.getWorld().getName()) != null) {
      player.sendMessage("§cJá existe uma sala neste mundo.");
      return;
    }

    if (args.length <= 1) {
      player.sendMessage("§cUtilize /am " + this.getUsage());
      return;
    }

    MurderMode mode = MurderMode.fromName(args[0]);
    if (mode == null) {
      player.sendMessage("§cUtilize /am " + this.getUsage());
      return;
    }

    String name = StringUtils.join(args, 1, " ");
    Object[] array = new Object[6];
    array[0] = player.getWorld();
    array[1] = name;
    array[2] = mode.name();
    array[4] = new ArrayList<String>();
    array[5] = new ArrayList<String>();
    CREATING.put(player, array);

    player.getInventory().clear();
    player.getInventory().setArmorContents(null);

    player.getInventory().setItem(0, BukkitUtils.deserializeItemStack("BLAZE_ROD : 1 : nome>&aAdicionar Spawn"));
    if (mode == MurderMode.IMPOSTOR_1) {
      player.getInventory().setItem(1, BukkitUtils.deserializeItemStack("GOLD_INGOT : 1 : nome>&aAdicionar Ouro"));
    }

    player.getInventory().setItem(4, BukkitUtils.deserializeItemStack("BEACON : 1 : nome>&aLocal de Espera"));

    player.getInventory().setItem(8, BukkitUtils.deserializeItemStack("STAINED_CLAY:13 : 1 : nome>&aConfirmar"));

    player.updateInventory();

    Profile.getProfile(player.getName()).setHotbar(null);
  }

  public static void handleClick(Profile profile, String display, PlayerInteractEvent evt) {
    Player player = profile.getPlayer();

    switch (display) {
      case "§aAdicionar Spawn": {
        if (((List<String>) CREATING.get(player)[4]).size() >= MurderMode.fromName(CREATING.get(player)[2].toString()).getSize()) {
          player.sendMessage("§cA quantidade de spawns já chegou ao limite!");
          return;
        }

        evt.setCancelled(true);
        Location location = player.getLocation().getBlock().getLocation().clone().add(.5, 0, .5);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        ((List<String>) CREATING.get(player)[4]).add(BukkitUtils.serializeLocation(location));
        player.sendMessage("§aSpawn de jogador adicionado!");
        break;
      }
      case "§aAdicionar Ouro": {
        evt.setCancelled(true);
        ((List<String>) CREATING.get(player)[5]).add(BukkitUtils.serializeLocation(player.getLocation().getBlock().getLocation().clone().add(.5, 0, .5)));
        player.sendMessage("§aSpawn de ouro adicionado!");
        break;
      }
      case "§aLocal de Espera": {
        evt.setCancelled(true);
        Location location = player.getLocation().getBlock().getLocation().clone().add(.5, 0, .5);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        CREATING.get(player)[3] = BukkitUtils.serializeLocation(location);
        player.sendMessage("§aLocal de espera adicionado!");
        break;
      }
      case "§aConfirmar": {
        evt.setCancelled(true);
        if (CREATING.get(player)[3] == null) {
          player.sendMessage("§cLocal de espera não setado.");
          return;
        }

        if (((List<String>) CREATING.get(player)[4]).size() < 2) {
          player.sendMessage("§cSpawns insuficientes para continuar.");
          return;
        }

        Object[] array = CREATING.get(player);
        World world = player.getWorld();
        KConfig config = Main.getInstance().getConfig("arenas", world.getName());
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        CREATING.remove(player);
        player.sendMessage("§aCriando sala...");

        config.set("name", array[1]);
        config.set("mode", array[2]);
        config.set("minPlayers", Math.max(((List<String>) array[4]).size(), 4) / 2);
        config.set("spawn", array[3]);
        config.set("spawns", array[4]);
        if (((String) array[2]).equalsIgnoreCase("classic")) {
          config.set("golds", array[5]);
        }
        world.save();

        player.sendMessage("§aCriando backup do mapa...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          Main.getInstance().getFileUtils()
                  .copyFiles(new File(world.getName()), new File("plugins/hAmongUS/mundos/" + world.getName()), "playerdata", "stats", "uid.dat");

          profile.setHotbar(Hotbar.getHotbarById("lobby"));
          profile.refresh();
          Murder.load(config.getFile(), () -> player.sendMessage("§aSala criada com sucesso."));
        }, 60);
        break;
      }
    }
  }
}
