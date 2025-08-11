package hero.amongus.listeners.player;

import net.hero.services.game.GameState;
import net.hero.services.libraries.npclib.api.event.NPCLeftClickEvent;
import net.hero.services.libraries.npclib.api.event.NPCRightClickEvent;
import net.hero.services.libraries.npclib.api.npc.NPC;
import net.hero.services.menus.MenuDeliveries;
import hero.amongus.cmd.mm.BuildCommand;
import hero.amongus.cmd.mm.CreateCommand;
import hero.amongus.cmd.mm.AmongUSCreateCommand;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.CosmeticType;
import hero.amongus.cosmetics.types.Knife;
import hero.amongus.game.enums.MurderMode;
import hero.amongus.game.types.AssassinsMurder;
import hero.amongus.game.types.ClassicMurder;
import net.hero.services.player.Profile;
import net.minecraft.server.v1_8_R3.DamageSource;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import hero.amongus.game.Murder;
import hero.amongus.menus.MenuPlay;
import hero.amongus.menus.MenuStatsNPC;
import hero.amongus.utils.Throwable;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import hero.amongus.Main;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Bukkit;

public class PlayerInteractListener implements Listener {

  @EventHandler
  public void onNPCLeftClick(NPCLeftClickEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());

    if (profile != null) {
      NPC npc = evt.getNPC();
      if (npc.data().has("play-npc")) {
        new MenuPlay(profile, MurderMode.fromName(npc.data().get("play-npc")));
      }
    }
  }

  @EventHandler
  public void onNPCRightClick(NPCRightClickEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());

    if (profile != null) {
      NPC npc = evt.getNPC();
      if (npc.data().has("play-npc")) {
        new MenuPlay(profile, MurderMode.fromName(npc.data().get("play-npc")));
      } else if (npc.data().has("delivery-npc")) {
        new MenuDeliveries(profile);
      } else if (npc.data().has("stats-npc")) {
        new MenuStatsNPC(profile);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent evt) {
    if (evt.getRightClicked().hasMetadata("MURDER")) {
      evt.setCancelled(true);
    }
  }

  public static Player getMoreNearby(Player player, List<Player> targets) {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < targets.size(); i++) {
      list.add(i + " : " + targets.get(i).getLocation().distance(player.getLocation()));
    }

    list.sort((o1, o2) -> {
      double i1 = Double.parseDouble(o1.split(" : ")[1]);
      double i2 = Double.parseDouble(o2.split(" : ")[1]);
      return Double.compare(i1, i2);
    });

    return targets.get(Integer.parseInt(list.get(0).split(" : ")[0]));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerInteract(PlayerInteractEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());

    if (profile != null) {
      if (AmongUSCreateCommand.CREATING.containsKey(player) && AmongUSCreateCommand.CREATING.get(player)[0].equals(player.getWorld())) {
        ItemStack item = player.getItemInHand();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
          AmongUSCreateCommand.handleClick(profile, item.getItemMeta().getDisplayName(), evt);
        }
      } else {
        Murder murder = profile.getGame(Murder.class);
        if (murder == null && !BuildCommand.hasBuilder(player)) {
          evt.setCancelled(true);
        } else if (murder != null) {
          evt.setCancelled(true);
          if (murder.isSpectator(player)) {
            return;
          }

          if (murder.getMode() == MurderMode.IMPOSTOR_1 || murder.getMode() == MurderMode.IMPOSTOR_2 || murder.getMode() == MurderMode.IMPOSTOR_3) {
            ItemStack item = player.getItemInHand();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
              Knife knife = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).
                      getSelected(CosmeticType.KNIFE, Knife.class);
              if (item.getType() == Material.COMPASS) {
                // Lógica para bússola nos modos Among Us
                List<Player> players = murder.listPlayers(false);
                players.remove(player);
                Player target = getMoreNearby(player, players);
                if (target != null) {
                  player.setCompassTarget(target.getLocation());
                  player.sendMessage("§aBússola apontando para " + target.getName());
                }
              } else if (item.getType() == Material.DIAMOND_SWORD || knife != null && item.getType() == knife.getItem().getType()) {
                if (evt.getAction().name().contains("RIGHT")) {
                  // Lógica para matar nos modos Among Us
                  player.getInventory().remove(item);
                  player.updateInventory();
                  // Implementar lógica de kill específica para Among Us
                  player.sendMessage("§cVocê tentou matar alguém!");
                }
              }
            }
          } else if (evt.getAction().name().contains("RIGHT")) {
            ItemStack item = player.getItemInHand();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
              evt.setCancelled(false);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent evt) {
    if (evt.getTo().getBlockY() != evt.getFrom().getBlockY() && evt.getTo().getBlockY() < 0) {
      Player player = evt.getPlayer();
      Profile profile = Profile.getProfile(player.getName());

      if (profile != null) {
        Murder game = profile.getGame(Murder.class);
        if (game == null) {
          // Substituindo Core.getLobby() por Main.getInstance().getConfig().getLocation("spawn")
          String spawnString = Main.getInstance().getConfig().getString("spawn");
          if (spawnString != null && !spawnString.isEmpty()) {
            try {
              Location spawnLocation = BukkitUtils.deserializeLocation(spawnString);
              if (spawnLocation != null && spawnLocation.getWorld() != null) {
                player.teleport(spawnLocation);
              } else {
                // Fallback para spawn padrão se a localização for inválida
                Location defaultSpawn = new Location(Bukkit.getWorlds().get(0), 0, 64, 0, 0, 0);
                player.teleport(defaultSpawn);
              }
            } catch (Exception ex) {
              // Fallback para spawn padrão se houver erro na deserialização
              Main.getInstance().getLogger().warning("Erro ao deserializar spawn: " + ex.getMessage());
              Location defaultSpawn = new Location(Bukkit.getWorlds().get(0), 0, 64, 0, 0, 0);
              player.teleport(defaultSpawn);
            }
          } else {
            // Fallback para spawn padrão se não houver spawn configurado
            Location defaultSpawn = new Location(Bukkit.getWorlds().get(0), 0, 64, 0, 0, 0);
            player.teleport(defaultSpawn);
          }
        } else {
          if (game.getState() != GameState.EMJOGO || game.isSpectator(player)) {
            player.teleport(game.getConfig().getSpawnLocation());
          } else {
            ((CraftPlayer) player).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, (float) player.getMaxHealth());
          }
        }
      }
    }
  }
}
