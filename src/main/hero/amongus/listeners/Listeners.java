package hero.amongus.listeners;

import hero.amongus.Main;
import hero.amongus.listeners.entity.EntityListener;
import hero.amongus.listeners.server.ServerListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import hero.amongus.listeners.player.AsyncPlayerChatListener;
import hero.amongus.listeners.player.InventoryClickListener;
import hero.amongus.listeners.player.PlayerDeathListener;
import hero.amongus.listeners.player.PlayerInteractListener;
import hero.amongus.listeners.player.PlayerJoinListener;
import hero.amongus.listeners.player.PlayerQuitListener;
import hero.amongus.listeners.player.PlayerRestListener;
import hero.amongus.listeners.player.VentInteractListener;

public class Listeners {

  public static void setupListeners() {
    try {
      PluginManager pm = Bukkit.getPluginManager();

      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new EntityListener(), Main.getInstance());

      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new AsyncPlayerChatListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new InventoryClickListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new PlayerDeathListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new PlayerInteractListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new PlayerJoinListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new PlayerQuitListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new PlayerRestListener(), Main.getInstance());
      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new VentInteractListener(), Main.getInstance());

      pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, new ServerListener(), Main.getInstance());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
