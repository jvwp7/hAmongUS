package hero.amongus.hook;

import com.comphenix.protocol.ProtocolLibrary;
// Removendo import de Core
import net.hero.services.achievements.Achievement;
import net.hero.services.achievements.types.MurderAchievement;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.MurderTeam;
import hero.amongus.game.enums.MurderMode;
import hero.amongus.game.enums.MurderRole;
import hero.amongus.game.types.AssassinsMurder;
import hero.amongus.game.types.ClassicMurder;
import hero.amongus.hook.hotbar.MMHotbarActionType;
import hero.amongus.hook.protocollib.HologramAdapter;
import net.hero.services.player.Profile;
import net.hero.services.player.hotbar.Hotbar;
import net.hero.services.player.hotbar.HotbarAction;
import net.hero.services.player.hotbar.HotbarActionType;
import net.hero.services.player.hotbar.HotbarButton;
import net.hero.services.player.scoreboard.KScoreboard;
import net.hero.services.player.scoreboard.scroller.ScoreboardScroller;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

public class MMCoreHook {

  public static void setupHook() {
    // Substituindo Core.minigame por uma variável local ou configuração
    // Core.minigame = "Murder";

    setupHotbars();
    new BukkitRunnable() {
      @Override
      public void run() {
        Profile.listProfiles().forEach(profile -> {
          if (profile.getScoreboard() != null) {
            profile.getScoreboard().scroll();
          }
        });
      }
    }.runTaskTimerAsynchronously(Main.getInstance(), 0, Language.scoreboards$scroller$every_tick);

    new BukkitRunnable() {
      @Override
      public void run() {
        Profile.listProfiles().forEach(profile -> {
          if (!profile.playingGame() && profile.getScoreboard() != null) {
            profile.update();
          }
        });
      }
    }.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);

    ProtocolLibrary.getProtocolManager().addPacketListener(new HologramAdapter());
  }

  public static void checkAchievements(final Profile profile) {
    Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
      Achievement.listAchievements(MurderAchievement.class).stream().filter(ma -> ma.canComplete(profile)).forEach(ma -> {
        ma.complete(profile);
        profile.getPlayer().sendMessage(Language.lobby$achievement.replace("{name}", ma.getName()));
      });
    });
  }

  private static final SimpleDateFormat SDF = new SimpleDateFormat("mm:ss");

  public static void reloadScoreboard(Profile profile) {
    if (!profile.playingGame()) {
      checkAchievements(profile);
    }
    Player player = profile.getPlayer();
    Murder game = profile.getGame(Murder.class);
    List<String> lines = game == null ? Language.scoreboards$lobby : game.getState().canJoin() ? Language.scoreboards$waiting : game.getMode() == MurderMode.IMPOSTOR_1 ? Language.scoreboards$classic : Language.scoreboards$assassins;
    profile.setScoreboard(new KScoreboard() {
      @Override
      public void update() {
        for (int index = 0; index < Math.min(lines.size(), 15); index++) {
          String line = lines.get(index);

          if (game != null) {
            if (game instanceof ClassicMurder) {
              line =
                line
                  .replace("{innocents}", StringUtils.formatNumber(((ClassicMurder) game).getInnocents()))
                  .replace("{detective}", game.listTeams().stream().anyMatch(mt -> mt.getRole() == MurderRole.IMPOSTOR && mt.isAlive()) ? "§aVivo" : "§cMorto");
            } else {
              MurderTeam team = game.getTeam(player);
              String contract = ((AssassinsMurder) game).getContract(player);
              line = line
                .replace("{bounty}", "§7" + (contract.isEmpty() ? "Nenhum" : contract))
                .replace("{kills}", StringUtils.formatNumber(team != null ? team.getKills() : 0));
            }
            line = line.replace("{timeLeft}", SDF.format(game.getTimer() * 1000))
              .replace("{role}", game.getTeam(player) == null ? "§7Morto" : game.getTeam(player).getRole().getName())
              .replace("{map}", game.getMapName())
              .replace("{server}", game.getGameName())
              .replace("{mode}", game.getMode().getName())
              .replace("{players}", StringUtils.formatNumber(game.getOnline()))
              .replace("{max_players}", StringUtils.formatNumber(game.getMaxPlayers()))
              .replace("{time}", game.getTimer() == 46 ? Language.scoreboards$time$waiting : Language.scoreboards$time$starting.replace("{time}", StringUtils.formatNumber(game.getTimer())));
          } else {
            line = PlaceholderAPI.setPlaceholders(player, line);
          }

          this.add(15 - index, line);
        }
      }
    }.scroller(new ScoreboardScroller(Language.scoreboards$scroller$titles)).to(player).build());
    profile.update();
    profile.getScoreboard().scroll();
  }

  private static void setupHotbars() {
    HotbarActionType.addActionType("murder", new MMHotbarActionType());

    KConfig config = Main.getInstance().getConfig("hotbar");
    for (String id : new String[] {"lobby", "waiting", "spectator"}) {
      Hotbar hotbar = new Hotbar(id);

      ConfigurationSection hb = config.getSection(id);
      for (String button : hb.getKeys(false)) {
        try {
          hotbar.getButtons().add(new HotbarButton(hb.getInt(button + ".slot"), new HotbarAction(hb.getString(button + ".execute")), hb.getString(button + ".icon")));
        } catch (Exception ex) {
          Main.getInstance().getLogger().log(Level.WARNING, "Falha ao carregar o botao \"" + button + "\" da hotbar \"" + id + "\": ", ex);
        }
      }

      Hotbar.addHotbar(hotbar);
    }
  }
}
