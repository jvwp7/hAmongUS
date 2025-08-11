package hero.amongus;

import net.hero.services.Core;
import net.hero.services.libraries.MinecraftVersion;
import hero.amongus.lobby.*;
import net.hero.services.plugin.KPlugin;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Bukkit;
import hero.amongus.cmd.Commands;
import hero.amongus.cosmetics.Cosmetic;
import hero.amongus.game.Murder;
import hero.amongus.hook.MMCoreHook;
import hero.amongus.listeners.Listeners;
import hero.amongus.tagger.TagUtils;
import hero.amongus.tagger.FakeManager;

import java.io.File;
import java.io.FileInputStream;

public class Main extends KPlugin {

  private static Main instance;
  private static boolean validInit;
  public static String currentServerName;

  @Override
  public void start() {
    instance = this;
  }

  @Override
  public void load() {}

  @Override
  public void enable() {
    if (MinecraftVersion.getCurrentVersion().getCompareId() != 183) {
      this.setEnabled(false);
      this.getLogger().warning("O plugin apenas funciona na versao 1_8_R3 (Atual: " + MinecraftVersion.getCurrentVersion().getVersion() + ")");
      return;
    }

    saveDefaultConfig();
    
    // Configurar spawn padrão se não existir
    if (!getConfig().contains("spawn")) {
      getConfig().set("spawn", "world,0,64,0,0,0");
      saveConfig();
      getLogger().info("Spawn padrão configurado: world,0,64,0,0,0");
    }
    
    currentServerName = getConfig().getString("lobby");
    // Não precisamos mais usar Core.setLobby() pois estamos usando Main.getInstance().getConfig().getLocation("spawn")
    // em vez de Core.getLobby() em todo o código

    Murder.setupGames();

    MMCoreHook.setupHook();
    Lobby.setupLobbies();
    Cosmetic.setupCosmetics();

    PlayNPC.setupNPCs();
    StatsNPC.setupNPCs();
    Language.setupLanguage();
    DeliveryNPC.setupNPCs();
    ImpostorNPC.setupNPCs();
    Leaderboard.setupLeaderboards();

    Listeners.setupListeners();
    Commands.setupCommands();

    validInit = true;
    this.getLogger().info("O plugin foi ativado.");
  }

  @Override
  public void disable() {
    if (validInit) {
      DeliveryNPC.listNPCs().forEach(DeliveryNPC::destroy);
      PlayNPC.listNPCs().forEach(PlayNPC::destroy);
      StatsNPC.listNPCs().forEach(StatsNPC::destroy);
      ImpostorNPC.listNPCs().forEach(ImpostorNPC::destroy);
      Leaderboard.listLeaderboards().forEach(Leaderboard::destroy);
      TagUtils.reset();
      FakeManager.reset();
    }

    File update = new File("plugins/update", ".jar");
    if (update.exists()) {
      try {
        this.getFileUtils().deleteFile(new File("plugins/" + update.getName()));
        this.getFileUtils().copyFile(new FileInputStream(update), new File("plugins/" + update.getName()));
        this.getFileUtils().deleteFile(update.getParentFile());
        this.getLogger().info("Update do  aplicada.");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    this.getLogger().info("O plugin foi desativado.");
  }

  public static Main getInstance() {
    return instance;
  }
}
