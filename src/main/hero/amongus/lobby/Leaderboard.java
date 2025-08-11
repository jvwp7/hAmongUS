package hero.amongus.lobby;

import net.hero.services.libraries.holograms.HologramLibrary;
import net.hero.services.libraries.holograms.api.Hologram;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.lobby.leaderboards.DetectiveLeaderboard;
import hero.amongus.lobby.leaderboards.KillerLeaderboard;
import net.hero.services.player.Profile;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Leaderboard {

  private String id;
  private Location location;

  private Hologram hologram;

  public Leaderboard(Location location, String id) {
    this.location = location;
    this.id = id;
  }

  public abstract String getType();

  public abstract List<String[]> getSplitted();

  public abstract List<String> getHologramLines();

  public void update() {
    List<String[]> list = this.getSplitted();

    List<String> lines = new ArrayList<>();
    for (String line : this.getHologramLines()) {
      for (int i = 0; i < list.size(); i++) {
        line = line.replace("{name_" + (i + 1) + "}", list.get(i)[0]).replace("{stats_" + (i + 1) + "}", list.get(i)[1]);
      }
      lines.add(line);
    }
    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
      if (this.hologram == null) {
        this.hologram = HologramLibrary.createHologram(this.location.clone(), lines);
        return;
      }

      int index = 1;
      for (String line : lines) {
        this.hologram.updateLine(index++, line);
      }
    });
  }

  public void destroy() {
    if (this.hologram != null) {
      HologramLibrary.removeHologram(this.hologram);
      this.hologram = null;
    }
  }

  public String getId() {
    return this.id;
  }

  public Location getLocation() {
    return this.location;
  }

  private static final KConfig CONFIG = Main.getInstance().getConfig("leaderboards");
  private static final List<Leaderboard> LEADERBOARDS = new ArrayList<>();

  public static void setupLeaderboards() {
    for (String serialized : CONFIG.getStringList("board-list")) {
      if (serialized.split("; ").length > 6) {
        String id = serialized.split("; ")[6];
        String type = serialized.split("; ")[7];
        Leaderboard board = buildByType(BukkitUtils.deserializeLocation(serialized), id, type);
        if (board == null) {
          return;
        }

        LEADERBOARDS.add(board);
      }
    }

    Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
      Profile.listProfiles().forEach(Profile::saveSync);
      listLeaderboards().forEach(Leaderboard::update);
    }, 0, Language.lobby$leaderboard$minutes * 1200);
  }

  public static void add(Location location, String id, String type) {
    List<String> list = CONFIG.getStringList("board-list");
    list.add(BukkitUtils.serializeLocation(location) + "; " + id + "; " + type.toLowerCase());
    CONFIG.set("board-list", list);

    Leaderboard board = buildByType(location, id, type);
    LEADERBOARDS.add(board);
    board.update();
  }

  public static void remove(Leaderboard board) {
    LEADERBOARDS.remove(board);
    List<String> list = CONFIG.getStringList("board-list");
    list.remove(BukkitUtils.serializeLocation(board.getLocation()) + "; " + board.getId() + "; " + board.getType());
    CONFIG.set("board-list", list);

    board.destroy();
  }

  public static Leaderboard getById(String id) {
    return LEADERBOARDS.stream().filter(board -> board.getId().equals(id)).findFirst().orElse(null);
  }

  public static Collection<Leaderboard> listLeaderboards() {
    return LEADERBOARDS;
  }

  private static Leaderboard buildByType(Location location, String id, String type) {
    if (type.equalsIgnoreCase("detetive")) {
      return new DetectiveLeaderboard(location, id);
    } else if (type.equalsIgnoreCase("assassino")) {
      return new KillerLeaderboard(location, id);
    }

    return null;
  }
}
