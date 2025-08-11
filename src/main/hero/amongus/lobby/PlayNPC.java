package hero.amongus.lobby;

import net.hero.services.libraries.holograms.HologramLibrary;
import net.hero.services.libraries.holograms.api.Hologram;
import net.hero.services.libraries.npclib.NPCLibrary;
import net.hero.services.libraries.npclib.api.npc.NPC;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.enums.MurderMode;
import hero.amongus.lobby.trait.NPCSkinTrait;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayNPC {

  private String id;
  private MurderMode mode;
  private Location location;

  private NPC npc;
  private Hologram hologram;

  public PlayNPC(Location location, String id, MurderMode mode) {
    this.location = location;
    this.id = id;
    this.mode = mode;
    if (!this.location.getChunk().isLoaded()) {
      this.location.getChunk().load(true);
    }

    Main.getInstance().getLogger().info("Criando NPC: " + id + " - " + mode.name());
    this.spawn();
    Main.getInstance().getLogger().info("NPC criado com sucesso: " + id);
  }

  public void spawn() {
    if (this.npc != null) {
      this.npc.destroy();
      this.npc = null;
    }

    if (this.hologram != null) {
      HologramLibrary.removeHologram(this.hologram);
      this.hologram = null;
    }

    this.hologram = HologramLibrary.createHologram(this.location.clone().add(0, 0.5, 0));
    this.hologram.withLine("§aClique para jogar: " + this.mode.getName());
    this.hologram.withLine("§7Jogadores: " + StringUtils.formatNumber(Murder.getWaiting(this.mode) + Murder.getPlaying(this.mode)));

    this.npc = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
    this.npc.data().set("play-npc", this.mode.name());
    this.npc.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    // Usar skin padrão válida para Among Us
    this.npc.addTrait(new NPCSkinTrait(this.npc, 
        "ewogICJ0aW1lc3RhbXAiIDogMTU5MzcxOTkwNjkxMSwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUyMjg3ZmQ5MDhkMjlhYjMwMDQyNjRjODk4ZGIwMDY1MjkyZDdiOGU5NmJlMzY2YmMwZWIwYzM5ZDMxNjY1NWEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "OKYGdZOZwR+4Q1bfzb1enXXUIT0Ru4WxadpXZoR2R/o8OJobBJAO205MuwHj4dNQNRE0L/ggPoE/iWvQXn3elcRdnJE4I4oVwb6DVZX1KLKtP1v2RM3QLcvZ6I2ogNa/cIln2BuxfV1u+wR39RahzrsbwWFz/U6MTaXw2dYIZmTkllHOkrLrRdeqAjJh9EsBizKcoWDCzdqXgOxOl0j+0JzG6InA7wgvf98IHZcZga0ihWebGDV3/TD4gocML1oTvrLW0ecPjOO1hCoKlk5niFobT6fJWR7XM0feKJsNdPlTOqAY0jlzm/w/hBW1gbTVDtK/lO2K6IfIijq2MIUnGF4uIkhQ3U9quVWTbK9/KqoJYKdx9Lkta+NOUkx5zT72Pske5O/taQsVYHST4ALicfGmvV7P1ohczHsibvguqzs0+sVgr0pb2jXiYJTRSgBvr2/X6esoBIy5DgvAbH9XM1adVqIj1zIhq1Q+YQ2iCV++eoUZ56uyy2sHlnzMA/Jj6+vODDKvpnYTXIyJ/qnWmxfYmFbf0Zd95A85rGhTfjvwP3DOcN3GZdwIe09ELSumDjHTlqv7MV+yLP+DKIcOqhQm4SQE7SlACW4Yw3r5UyiLfovpmTg8SBr/WwGJs8AhIY4LuF7PtPVNhs/Xe9cfH8KVUbd4PxOeLyNPq7+ibt8="));
    this.npc.spawn(this.location);
  }

  public void update() {
    this.hologram.updateLine(1, "§aClique para jogar: " + this.mode.getName());
    this.hologram.updateLine(2, "§7Jogadores: " + StringUtils.formatNumber(Murder.getWaiting(this.mode) + Murder.getPlaying(this.mode)));
  }

  public void destroy() {
    this.id = null;
    this.mode = null;
    this.location = null;

    this.npc.destroy();
    this.npc = null;
    HologramLibrary.removeHologram(this.hologram);
    this.hologram = null;
  }

  public String getId() {
    return id;
  }

  public MurderMode getMode() {
    return this.mode;
  }

  public Location getLocation() {
    return this.location;
  }

  private static final KConfig CONFIG = Main.getInstance().getConfig("npcs");
  private static final List<PlayNPC> NPCS = new ArrayList<>();

  public static void setupNPCs() {
    if (!CONFIG.contains("play")) {
      CONFIG.set("play", new ArrayList<>());
    }

    for (String serialized : CONFIG.getStringList("play")) {
      if (serialized.split("; ").length > 6) {
        String id = serialized.split("; ")[6];
        MurderMode mode = MurderMode.fromName(serialized.split("; ")[7]);
        if (mode == null) {
          continue;
        }

        NPCS.add(new PlayNPC(BukkitUtils.deserializeLocation(serialized), id, mode));
      }
    }

    Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> listNPCs().forEach(PlayNPC::update), 20, 20);
  }

  public static void add(String id, Location location, MurderMode mode) {
    NPCS.add(new PlayNPC(location, id, mode));
    List<String> list = CONFIG.getStringList("play");
    list.add(BukkitUtils.serializeLocation(location) + "; " + id + "; " + mode);
    CONFIG.set("play", list);
    CONFIG.save(); // Salvar o arquivo
    Main.getInstance().getLogger().info("NPC adicionado: " + id + " - " + mode.name() + " em " + location.toString());
  }

  public static void remove(PlayNPC npc) {
    NPCS.remove(npc);
    List<String> list = CONFIG.getStringList("play");
    list.remove(BukkitUtils.serializeLocation(npc.getLocation()) + "; " + npc.getId() + "; " + npc.getMode());
    CONFIG.set("play", list);

    npc.destroy();
  }

  public static PlayNPC getById(String id) {
    return NPCS.stream().filter(npc -> npc.getId().equals(id)).findFirst().orElse(null);
  }

  public static Collection<PlayNPC> listNPCs() {
    return NPCS;
  }
}
