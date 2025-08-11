package hero.amongus.lobby;

import net.hero.services.libraries.holograms.HologramLibrary;
import net.hero.services.libraries.holograms.api.Hologram;
import net.hero.services.libraries.npclib.NPCLibrary;
import net.hero.services.libraries.npclib.api.npc.NPC;
import net.hero.services.libraries.npclib.trait.NPCTrait;
import hero.amongus.Language;
import hero.amongus.Main;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatsNPC {

  private String id;
  private Location location;

  private NPC npc;
  private Hologram hologram;

  public StatsNPC(Location location, String id) {
    this.location = location;
    this.id = id;
    if (!this.location.getChunk().isLoaded()) {
      this.location.getChunk().load(true);
    }

    this.spawn();
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
    for (int index = Language.lobby$npc$stats$hologram.size(); index > 0; index--) {
      this.hologram.withLine(Language.lobby$npc$stats$hologram.get(index - 1));
    }

    this.npc = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
    this.npc.data().set("stats-npc", true);
    this.npc.data().set(NPC.COPY_PLAYER_SKIN, true);
    this.npc.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    this.npc.addTrait(new NPCTrait(this.npc) {
      @Override
      public void onSpawn() {
        ((Player) getNPC().getEntity()).setItemInHand(new ItemStack(Material.PAPER));
      }
    });
    this.npc.spawn(this.location);
  }

  public void destroy() {
    this.id = null;
    this.location = null;

    this.npc.destroy();
    this.npc = null;
    HologramLibrary.removeHologram(this.hologram);
    this.hologram = null;
  }

  public String getId() {
    return id;
  }

  public Location getLocation() {
    return this.location;
  }

  private static final KConfig CONFIG = Main.getInstance().getConfig("npcs");
  private static final List<StatsNPC> NPCS = new ArrayList<>();

  public static void setupNPCs() {
    if (!CONFIG.contains("statsnpc")) {
      CONFIG.set("statsnpc", new ArrayList<>());
    }

    for (String serialized : CONFIG.getStringList("statsnpc")) {
      if (serialized.split("; ").length > 6) {
        String id = serialized.split("; ")[6];

        NPCS.add(new StatsNPC(BukkitUtils.deserializeLocation(serialized), id));
      }
    }
  }

  public static void add(String id, Location location) {
    NPCS.add(new StatsNPC(location, id));
    List<String> list = CONFIG.getStringList("statsnpc");
    list.add(BukkitUtils.serializeLocation(location) + "; " + id);
    CONFIG.set("statsnpc", list);
  }

  public static void remove(StatsNPC npc) {
    NPCS.remove(npc);
    List<String> list = CONFIG.getStringList("statsnpc");
    list.remove(BukkitUtils.serializeLocation(npc.getLocation()) + "; " + npc.getId());
    CONFIG.set("statsnpc", list);

    npc.destroy();
  }

  public static StatsNPC getById(String id) {
    return NPCS.stream().filter(npc -> npc.getId().equals(id)).findFirst().orElse(null);
  }

  public static Collection<StatsNPC> listNPCs() {
    return NPCS;
  }
}
