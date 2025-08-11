package hero.amongus.menus;

import net.hero.services.libraries.menu.UpdatablePlayerPagedMenu;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.enums.MurderMode;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.TimeUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuMapSelector extends UpdatablePlayerPagedMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getCurrentInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked().equals(this.player)) {
        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getCurrentInventory())) {
          ItemStack item = evt.getCurrentItem();

          if (item != null && item.getType() != Material.AIR) {
            if (evt.getSlot() == this.previousPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openPrevious();
            } else if (evt.getSlot() == this.nextPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openNext();
            } else if (evt.getSlot() == 30) {
              EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
            } else if (evt.getSlot() == 49) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuPlay(this.profile, this.mode);
            } else {
              String mapName = this.maps.get(item);
              if (mapName != null && this.can) {
                EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
                for (Murder game : this.games.get(mapName)) {
                  if (game.getState().canJoin() && game.getOnline() < game.getMaxPlayers()) {
                    this.player.sendMessage(Language.lobby$npc$play$connect);
                    this.profile.setStats("HeroCoreMurder", System.currentTimeMillis() + TimeUtils.getExpireIn(1), "lastmap");
                    game.join(this.profile);
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private Profile profile;
  private MurderMode mode;
  private boolean can = true;
  private Map<ItemStack, String> maps = new HashMap<>();
  private Map<String, List<Murder>> games = new HashMap<>();

  public MenuMapSelector(Profile profile, MurderMode mode) {
    super(profile.getPlayer(), "Mapas - Modo " + mode.getName(), 6);
    this.profile = profile;
    this.mode = mode;
    this.previousPage = 19;
    this.nextPage = 26;
    this.onlySlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    this.removeSlotsWith(BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar"), 49);

    this.update();
    this.register(Main.getInstance(), 20);
    this.open();
  }

  @Override
  public void update() {
    if (!this.player.hasPermission(".menu.selector") && profile.getStats("HeroCoreMurder", "lastmap") >= System.currentTimeMillis()) {
      this.can = false;
    }

    List<ItemStack> items = new ArrayList<>();
    this.games = Murder.getAsMap(this.mode);
    for (Map.Entry<String, List<Murder>> entry : this.games.entrySet()) {
      List<Murder> games = entry.getValue();
      ItemStack item = BukkitUtils.deserializeItemStack(
        "EMPTY_MAP : 1 : nome>&b" + entry.getKey() + " : desc>&8Modo " + this.mode.getName() + "\n \n&7Salas disponíveis: &a" + games
          .size() + "\n \n&eClique com o botão esquerdo para jogar!");
      items.add(item);
      this.maps.put(item, entry.getKey());
    }

    if (this.lastListSize != -1 && this.lastListSize != items.size()) {
      items.clear();
      new MenuMapSelector(this.profile, this.mode);
      return;
    }

    this.removeSlotsWith(BukkitUtils.deserializeItemStack(Language.lobby$npc$play$menu$info$item.replace("{desc}", this.player.hasPermission(".menu.selector") ?
      Language.lobby$npc$play$menu$info$desc_not_limit :
      Language.lobby$npc$play$menu$info$desc_limit.replace("{limit}", this.can ? "0/1" : "1/1"))), 48);
    this.setItems(items);
  }

  public void cancel() {
    super.cancel();
    HandlerList.unregisterAll(this);
    this.profile = null;
    this.mode = null;
    this.maps.clear();
    this.maps = null;
    this.games.values().forEach(List::clear);
    this.games.clear();
    this.games = null;
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(this.player)) {
      this.cancel();
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getCurrentInventory())) {
      this.cancel();
    }
  }
}
