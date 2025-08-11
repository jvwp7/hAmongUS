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

/**
 * NPC para os modos de jogo com diferentes quantidades de impostores
 */
public class ImpostorNPC {

  private static final List<ImpostorNPC> NPCS = new ArrayList<>();
  private static final KConfig CONFIG = Main.getInstance().getConfig("npcs");

  private Location location;
  private MurderMode mode;
  private NPC npc;
  private Hologram hologram;

  /**
   * Cria um novo NPC para um modo de jogo
   * @param location Localização do NPC
   * @param mode Modo de jogo
   */
  public ImpostorNPC(Location location, MurderMode mode) {
    this.location = location;
    this.mode = mode;
  }

  /**
   * Spawna o NPC no mundo
   */
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
    
    // Adiciona as linhas do holograma com base no modo de jogo
    List<String> hologramLines = new ArrayList<>();
    
    switch (this.mode) {
      case IMPOSTOR_1:
        hologramLines.add("§c§lImpostor 1");
        hologramLines.add("§71 Impostor");
        hologramLines.add("§78 Jogadores");
        break;
      case IMPOSTOR_2:
        hologramLines.add("§c§lImpostor 2");
        hologramLines.add("§72 Impostores");
        hologramLines.add("§712 Jogadores");
        break;
      case IMPOSTOR_3:
        hologramLines.add("§c§lImpostor 3");
        hologramLines.add("§73 Impostores");
        hologramLines.add("§716 Jogadores");
        break;
      default:
        hologramLines.add("§c§lImpostor");
        hologramLines.add("§7Modo de jogo");
        break;
    }
    
    hologramLines.add("§7Jogadores: §f" + StringUtils.formatNumber(Murder.getWaiting(this.mode) + Murder.getPlaying(this.mode)));
    hologramLines.add("§aClique para jogar!");
    
    for (int index = hologramLines.size(); index > 0; index--) {
      this.hologram.withLine(hologramLines.get(index - 1));
    }

    this.npc = NPCLibrary.createNPC(EntityType.PLAYER, "§8[NPC] ");
    this.npc.data().set("impostor-npc", this.mode.name());
    this.npc.data().set(NPC.HIDE_BY_TEAMS_KEY, true);
    
    // Adiciona a skin com base no modo de jogo
    switch (this.mode) {
      case IMPOSTOR_1:
        this.npc.addTrait(new NPCSkinTrait(this.npc, 
            "ewogICJ0aW1lc3RhbXAiIDogMTU5MzcxOTkwNjkxMSwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzRlZGUyMTQ0YmFkYTBiYjk4YTUyNWNkMjE5NzZjYjY1ZGEwZmUyMzYxOTdmZmM5ZDQzYjI4YTdkMWZkNmNmZGQiCiAgICB9CiAgfQp9", 
            "OKYGdZOZwR+4Q1bfzb1enXXUIT0Ru4WxadpXZoR2R/o8OJobBJAO205MuwHj4dNQNRE0L/ggPoE/iWvQXn3elcRdnJE4I4oVwb6DVZX1KLKtP1v2RM3QLcvZ6I2ogNa/cIln2BuxfV1u+wR39RahzrsbwWFz/U6MTaXw2dYIZmTkllHOkrLrRdeqAjJh9EsBizKcoWDCzdqXgOxOl0j+0JzG6InA7wgvf98IHZcZga0ihWebGDV3/TD4gocML1oTvrLW0ecPjOO1hCoKlk5niFobT6fJWR7XM0feKXXGfKkHuEJA/E3XvjvQYe9/y4Qr6xqQRZE4JhyG9QYjW+K9GA=="));
        break;
      case IMPOSTOR_2:
        this.npc.addTrait(new NPCSkinTrait(this.npc, 
            "ewogICJ0aW1lc3RhbXAiIDogMTYwMDU1MjU2NTk4NywKICAicHJvZmlsZUlkIiA6ICI1NjY3NWIyMjMyZjA0ZWUwODkxNzllOWM5MjA2Y2ZlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVJbmRyYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMTdmMWI3ZTg5NDdmMzM3YTVlYTYxZGI4NTZlYzVmMjY1YzljMzc5ZmI1OWQ1M2I2ZGZiYzY4YzY0NDk4ZGE4IgogICAgfQogIH0KfQ==", 
            "aJ7/qBVQm0UKiFDENFBNiWnsim+bS6SXQACBBQ11j5cm/gYe1qU/SeuAFe9G5JPl2eKKrb9SspL5nHlvV2TFNU0+NmZLi+6feBFAdiifhtGphTUCYKQIvqLOJWvEe4xSu1qBnec9fHM7nkxB1P+KppoWHyEHZT5q4s1czpI5ATO9+M4qBzWghS8232FAaklvNs9qNLZfAoL9MEa3QO4LuX5fXIt5GnWHr7FDyNUPcUgNTNVNvKphsCfMwLLNOPItEbB9c9PzUMtXigBffK6eF9aPc6NqAwU80pBmqZdvnZ8iYFG5N8ywHa5QXDXhbMcJODFxJXoFWQGnslmK9j+Ouw=="));
        break;
      case IMPOSTOR_3:
        this.npc.addTrait(new NPCSkinTrait(this.npc, 
            "ewogICJ0aW1lc3RhbXAiIDogMTU5MzQ4NDQ3MzUxNiwKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI1NmI5ZTk5MmM4YzlmMTIzMDQ5OGNkODRlZTdiYzk5OWM1YjIzNDEzMzFmYzNjZmM0MzYzMDEyMmFiNzY3ZTAiCiAgICB9CiAgfQp9", 
            "Ey/oDXlZQXOQJsKGpXKVCWnFzDdvzxjQxKJMlKKYi8+Fy/JYY/nwh/qXJKe1QzW3HR/akYl2mlFcgtuYQgEDLpbwXJJZP1a5sLEVvYQVQXHNFvgWLWQJUYQDlST+8a2p/7jQmAW9BbLmNUd3xf+Wl4zRrJP8M6oLSoJnVLgLYQQPZbfFQQGZdLWKCgKpzQQRQSdpTnr1/5QKpEbLOEiOp0c8dgLLUcZ5HoSQ4aqrGFx+FWGZ1mMk9YYFMfzxrGSDFUvYhXdgzKE+0QCpI7FeQnL7sLT+q5PqQzlsJyoGj+W/cTFsWQGBRpA+KZXsOZVbQVX+KpQnQDOGRQnGgQ=="));
        break;
      default:
        // Skin padrão
        this.npc.addTrait(new NPCSkinTrait(this.npc, 
            "ewogICJ0aW1lc3RhbXAiIDogMTU5MzQ4NDQ3MzUxNiwKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI1NmI5ZTk5MmM4YzlmMTIzMDQ5OGNkODRlZTdiYzk5OWM1YjIzNDEzMzFmYzNjZmM0MzYzMDEyMmFiNzY3ZTAiCiAgICB9CiAgfQp9", 
            "Ey/oDXlZQXOQJsKGpXKVCWnFzDdvzxjQxKJMlKKYi8+Fy/JYY/nwh/qXJKe1QzW3HR/akYl2mlFcgtuYQgEDLpbwXJJZP1a5sLEVvYQVQXHNFvgWLWQJUYQDlST+8a2p/7jQmAW9BbLmNUd3xf+Wl4zRrJP8M6oLSoJnVLgLYQQPZbfFQQGZdLWKCgKpzQQRQSdpTnr1/5QKpEbLOEiOp0c8dgLLUcZ5HoSQ4aqrGFx+FWGZ1mMk9YYFMfzxrGSDFUvYhXdgzKE+0QCpI7FeQnL7sLT+q5PqQzlsJyoGj+W/cTFsWQGBRpA+KZXsOZVbQVX+KpQnQDOGRQnGgQ=="));
        break;
    }
    
    this.npc.spawn(this.location);
  }

  /**
   * Atualiza o holograma do NPC
   */
  public void update() {
    if (this.hologram == null) {
      return;
    }
    
    // Atualiza a linha de jogadores
    // Corrigido para usar o método correto
    this.hologram.updateLine(1, "§7Jogadores: §f" + StringUtils.formatNumber(Murder.getWaiting(this.mode) + Murder.getPlaying(this.mode)));
  }

  /**
   * Destrói o NPC e o holograma
   */
  public void destroy() {
    if (this.npc != null) {
      this.npc.destroy();
      this.npc = null;
    }

    if (this.hologram != null) {
      HologramLibrary.removeHologram(this.hologram);
      this.hologram = null;
    }
  }

  /**
   * Configura os NPCs a partir do arquivo de configuração
   */
  public static void setupNPCs() {
    try {
      // Verificar se há mundos disponíveis
      if (Bukkit.getWorlds().isEmpty()) {
        Main.getInstance().getLogger().warning("Nenhum mundo disponível para configurar NPCs!");
        return;
      }

      // Corrigido para usar os métodos corretos
      if (!CONFIG.contains("impostor1")) {
        // Criar uma localização padrão com yaw e pitch
        Location defaultLocation = new Location(Bukkit.getWorlds().get(0), 0, 64, 0, 0, 0);
        CONFIG.set("impostor1", BukkitUtils.serializeLocation(defaultLocation));
        CONFIG.save();
      }
      
      if (!CONFIG.contains("impostor2")) {
        // Criar uma localização padrão com yaw e pitch
        Location defaultLocation = new Location(Bukkit.getWorlds().get(0), 2, 64, 0, 0, 0);
        CONFIG.set("impostor2", BukkitUtils.serializeLocation(defaultLocation));
        CONFIG.save();
      }
      
      if (!CONFIG.contains("impostor3")) {
        // Criar uma localização padrão com yaw e pitch
        Location defaultLocation = new Location(Bukkit.getWorlds().get(0), 4, 64, 0, 0, 0);
        CONFIG.set("impostor3", BukkitUtils.serializeLocation(defaultLocation));
        CONFIG.save();
      }

      for (ImpostorNPC npc : NPCS) {
        npc.destroy();
      }
      NPCS.clear();

      // Carregar NPCs com tratamento de erro
      if (CONFIG.contains("impostor1")) {
        try {
          String locationString = CONFIG.getString("impostor1");
          if (locationString != null && !locationString.isEmpty()) {
            Location location = BukkitUtils.deserializeLocation(locationString);
            if (location != null && location.getWorld() != null) {
              NPCS.add(new ImpostorNPC(location, MurderMode.IMPOSTOR_1));
            }
          }
        } catch (Exception e) {
          Main.getInstance().getLogger().warning("Erro ao carregar NPC Impostor 1: " + e.getMessage());
        }
      }
      
      if (CONFIG.contains("impostor2")) {
        try {
          String locationString = CONFIG.getString("impostor2");
          if (locationString != null && !locationString.isEmpty()) {
            Location location = BukkitUtils.deserializeLocation(locationString);
            if (location != null && location.getWorld() != null) {
              NPCS.add(new ImpostorNPC(location, MurderMode.IMPOSTOR_2));
            }
          }
        } catch (Exception e) {
          Main.getInstance().getLogger().warning("Erro ao carregar NPC Impostor 2: " + e.getMessage());
        }
      }
      
      if (CONFIG.contains("impostor3")) {
        try {
          String locationString = CONFIG.getString("impostor3");
          if (locationString != null && !locationString.isEmpty()) {
            Location location = BukkitUtils.deserializeLocation(locationString);
            if (location != null && location.getWorld() != null) {
              NPCS.add(new ImpostorNPC(location, MurderMode.IMPOSTOR_3));
            }
          }
        } catch (Exception e) {
          Main.getInstance().getLogger().warning("Erro ao carregar NPC Impostor 3: " + e.getMessage());
        }
      }

      // Spawnar NPCs com tratamento de erro
      for (ImpostorNPC npc : NPCS) {
        try {
          npc.spawn();
        } catch (Exception e) {
          Main.getInstance().getLogger().warning("Erro ao spawnar NPC: " + e.getMessage());
        }
      }

      Main.getInstance().getLogger().info("NPCs configurados com sucesso: " + NPCS.size() + " NPCs criados.");
      
    } catch (Exception e) {
      Main.getInstance().getLogger().severe("Erro crítico ao configurar NPCs: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Adiciona um NPC para um modo específico
   * @param mode Modo do jogo
   * @param location Localização do NPC
   */
  public static void add(MurderMode mode, Location location) {
    NPCS.add(new ImpostorNPC(location, mode));
    
    // Salvar no config
    String configKey = mode.name().toLowerCase().replace("_", "");
    CONFIG.set(configKey, BukkitUtils.serializeLocation(location));
    CONFIG.save();
  }

  /**
   * Remove um NPC para um modo específico
   * @param mode Modo do jogo
   */
  public static void remove(MurderMode mode) {
    NPCS.removeIf(npc -> npc.getMode() == mode);
    
    // Remover do config
    String configKey = mode.name().toLowerCase().replace("_", "");
    CONFIG.set(configKey, null);
    CONFIG.save();
  }

  /**
   * Obtém o modo do NPC
   * @return Modo do jogo
   */
  public MurderMode getMode() {
    return this.mode;
  }

  /**
   * Atualiza todos os NPCs
   */
  public static void updateNPCs() {
    for (ImpostorNPC npc : NPCS) {
      npc.update();
    }
  }

  /**
   * Obtém todos os NPCs
   * @return Lista de NPCs
   */
  public static Collection<ImpostorNPC> listNPCs() {
    return NPCS;
  }
}