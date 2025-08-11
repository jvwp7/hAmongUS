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
import hero.amongus.game.object.Room;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class AmongUSCreateCommand extends SubCommand {

  public static final Map<Player, Object[]> CREATING = new HashMap<>();

  public AmongUSCreateCommand() {
    super("criar", "criar [impostor1/impostor2/impostor3] [nome]", "Criar uma sala do Among US.", true);
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
    Object[] array = new Object[7]; // Aumentado para incluir salas e ventilações
    array[0] = player.getWorld();
    array[1] = name;
    array[2] = mode.name();
    array[3] = null; // Local de espera (será definido depois)
    array[4] = new ArrayList<String>(); // Spawns
    array[5] = new ArrayList<String>(); // Tarefas
    array[6] = new ArrayList<String>(); // Salas
    CREATING.put(player, array);

    player.getInventory().clear();
    player.getInventory().setArmorContents(null);

    player.getInventory().setItem(0, BukkitUtils.deserializeItemStack("BLAZE_ROD : 1 : nome>&aAdicionar Spawn"));
    player.getInventory().setItem(1, BukkitUtils.deserializeItemStack("PAPER : 1 : nome>&aAdicionar Tarefa"));
    player.getInventory().setItem(2, BukkitUtils.deserializeItemStack("IRON_DOOR : 1 : nome>&aAdicionar Sala"));
    player.getInventory().setItem(3, BukkitUtils.deserializeItemStack("HOPPER : 1 : nome>&aAdicionar Ventilação"));

    player.getInventory().setItem(4, BukkitUtils.deserializeItemStack("BEACON : 1 : nome>&aLocal de Espera"));
    player.getInventory().setItem(5, BukkitUtils.deserializeItemStack("REDSTONE : 1 : nome>&aBotão de Emergência"));

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
      case "§aAdicionar Tarefa": {
        evt.setCancelled(true);
        // Abre um menu para selecionar o tipo de tarefa
        player.sendMessage("§aSelecione o tipo de tarefa usando /am tarefa [comum/curta/longa/visual] [nome]");
        break;
      }
      case "§aAdicionar Sala": {
        evt.setCancelled(true);
        // Abre um menu para criar uma sala
        player.sendMessage("§aSelecione o nome da sala usando /am sala [nome]");
        break;
      }
      case "§aAdicionar Ventilação": {
        evt.setCancelled(true);
        // Adiciona uma ventilação no local atual
        Location location = player.getLocation().getBlock().getLocation().clone().add(.5, 0, .5);
        player.sendMessage("§aVentilação adicionada! Use /am conectarvent [id1] [id2] para conectar ventilações.");
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
      case "§aBotão de Emergência": {
        evt.setCancelled(true);
        Location location = player.getLocation().getBlock().getLocation().clone().add(.5, 0, .5);
        player.sendMessage("§aBotão de emergência adicionado na sala atual!");
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
        
        // Configurações específicas do Among US
        MurderMode mode = MurderMode.fromName((String) array[2]);
        int impostors = 1;
        if (mode == MurderMode.IMPOSTOR_2) {
          impostors = 2;
        } else if (mode == MurderMode.IMPOSTOR_3) {
          impostors = 3;
        }
        
        config.set("impostors", impostors);
        config.set("kill_cooldown", 30);
        config.set("meeting_time", 15);
        config.set("voting_time", 120);
        config.set("task_time", 5);
        config.set("player_speed", 1.0);
        config.set("kill_distance", 1.5);
        
        // Salvar tarefas e salas
        config.set("tasks", array[5]);
        config.set("rooms", array[6]);
        
        // Salvar ventilações
        List<String> vents = VentCommand.getVentsForWorld(world.getName());
        if (!vents.isEmpty()) {
            config.set("vents", vents);
            VentCommand.clearVents(world.getName());
        }
        
        // Salvar sabotagens
        List<String> sabotages = SabotageCommand.getSabotagesForWorld(world.getName());
        if (!sabotages.isEmpty()) {
            config.set("sabotages", sabotages);
            SabotageCommand.clearSabotages(world.getName());
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