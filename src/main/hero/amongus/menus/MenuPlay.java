package hero.amongus.menus;

import net.hero.services.libraries.menu.UpdatablePlayerMenu;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.enums.MurderMode;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MenuPlay extends UpdatablePlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked().equals(this.player)) {
        Profile profile = Profile.getProfile(this.player.getName());
        if (profile == null) {
          this.player.closeInventory();
          return;
        }

        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getInventory())) {
          ItemStack item = evt.getCurrentItem();

          if (item != null && item.getType() != Material.AIR) {
            if (evt.getSlot() == 12) {
              EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
              Murder game = Murder.findRandom(this.mode);
              if (game != null) {
                this.player.sendMessage(Language.lobby$npc$play$connect);
                game.join(profile);
              }
            } else if (evt.getSlot() == 14) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuMapSelector(profile, this.mode);
            }
          }
        }
      }
    }
  }

  private MurderMode mode;

  public MenuPlay(Profile profile, MurderMode mode) {
    super(profile.getPlayer(), "Modo " + mode.getName(), 3);
    this.mode = mode;

    this.update();
    this.register(Main.getInstance(), 20);
    this.open();
  }

  @Override
  public void update() {
    int players = this.mode.getSize();
    int waiting = Murder.getWaiting(this.mode);
    int playing = Murder.getPlaying(this.mode);

    // Lógica genérica para modos Among Us
    this.setItem(12, BukkitUtils.deserializeItemStack(
      "ENDER_PEARL : 1 : nome>&a" + this.mode.getName() + " : desc>&7Modo Among Us\n&7" + this.mode.getSize() + " jogadores\n \n&fEm espera: &7" + StringUtils
        .formatNumber(waiting) + "\n&fJogando: &7" + StringUtils.formatNumber(playing) + "\n \n&eClique para jogar!"));

    this.setItem(14, BukkitUtils.deserializeItemStack("MAP : 1 : nome>&aSelecione um Mapa : desc>&eClique para jogar em um mapa específico."));
  }

  public void cancel() {
    super.cancel();
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(this.player)) {
      this.cancel();
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getInventory())) {
      this.cancel();
    }
  }
}
