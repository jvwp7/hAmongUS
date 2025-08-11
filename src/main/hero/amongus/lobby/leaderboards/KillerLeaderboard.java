package hero.amongus.lobby.leaderboards;

import net.hero.services.database.Database;
import org.bukkit.Location;
import hero.amongus.Language;
import hero.amongus.lobby.Leaderboard;

import java.util.List;

public class KillerLeaderboard extends Leaderboard {

  public KillerLeaderboard(Location location, String id) {
    super(location, id);
  }

  @Override
  public String getType() {
    return "assassino";
  }

  @Override
  public List<String[]> getSplitted() {
    List<String[]> list = Database.getInstance().getLeaderBoard("HeroCoreMurder", "clkillerwins");
    while (list.size() < 10) {
      list.add(new String[] {Language.lobby$leaderboard$empty, "0"});
    }
    return list;
  }

  @Override
  public List<String> getHologramLines() {
    return Language.lobby$leaderboard$wins_as_murder$hologram;
  }
}
