package hero.amongus.menus.cosmetics;

import net.hero.services.Core;
import net.hero.services.cash.CashManager;
import net.hero.services.libraries.menu.PagedPlayerMenu;
import hero.amongus.cosmetics.types.DeathMessage;
import net.hero.services.player.Profile;
import net.hero.services.player.role.Role;
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
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.Cosmetic;
import hero.amongus.cosmetics.CosmeticType;
import hero.amongus.cosmetics.types.DeathCry;
import hero.amongus.menus.MenuShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuCosmetics<T extends Cosmetic> extends PagedPlayerMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getCurrentInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked().equals(this.player)) {
        Profile profile = Profile.getProfile(this.player.getName());
        if (profile == null) {
          this.player.closeInventory();
          return;
        }

        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getCurrentInventory())) {
          ItemStack item = evt.getCurrentItem();

          if (item != null && item.getType() != Material.AIR) {
            if (evt.getSlot() == this.previousPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openPrevious();
            } else if (evt.getSlot() == this.nextPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openNext();
            } else if (evt.getSlot() == (this.rows * 9) - 5) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuShop(profile);
            } else {
              T cosmetic = this.cosmetics.get(item);
              if (cosmetic != null) {
                if (evt.isRightClick()) {
                  if (cosmetic.getType() == CosmeticType.DEATH_CRY) {
                    ((DeathCry) cosmetic).getSound().play(this.player, ((DeathCry) cosmetic).getVolume(), ((DeathCry) cosmetic).getSpeed());
                    return;
                  } else if (cosmetic.getType() == CosmeticType.DEATH_MESSAGE) {
                    player.sendMessage("\n§eMensagens que poderão ser exibidas ao abater seu oponente:\n  \n");
                    ((DeathMessage) cosmetic).getMessages().forEach(message -> {
                      player.sendMessage(" §8▪ " + StringUtils.formatColors(message.replace("{name}", "§7Jogador").replace("{killer}", Role.getColored(player.getName()))));
                    });
                    player.sendMessage("");
                    return;
                  }
                }

                if (!cosmetic.has(profile)) {
                  if (!cosmetic.canBuy(this.player) || (profile.getCoins("HeroCoreMurder") < cosmetic.getCoins() && (CashManager.CASH && profile
                    .getStats("HeroCoreProfile", "cash") < cosmetic.getCash()))) {
                    EnumSound.ENDERMAN_TELEPORT.play(this.player, 0.5F, 1.0F);
                    return;
                  }

                  EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
                  if (!CashManager.CASH || cosmetic.getCash() == 0) {
                    new MenuBuyCosmetic<>(profile, this.name.replace("Murder - ", ""), cosmetic, this.cosmeticClass);
                  } else {
                    new MenuBuyCashCosmetic<>(profile, this.name.replace("Murder - ", ""), cosmetic, this.cosmeticClass);
                  }
                  return;
                }

                if (!cosmetic.canBuy(this.player)) {
                  EnumSound.ENDERMAN_TELEPORT.play(this.player, 0.5F, 1.0F);
                  this.player.sendMessage("§cVocê não possui permissão suficiente para continuar.");
                  return;
                }

                EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
                if (cosmetic.isSelected(profile)) {
                  profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).setSelected(cosmetic.getType(), 0);
                } else {
                  profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).setSelected(cosmetic);
                }

                new MenuCosmetics<>(profile, this.name.replace("Murder - ", ""), this.cosmeticClass);
              }
            }
          }
        }
      }
    }
  }

  private Class<T> cosmeticClass;
  private Map<ItemStack, T> cosmetics = new HashMap<>();

  public MenuCosmetics(Profile profile, String name, Class<T> cosmeticClass) {
    super(profile.getPlayer(), "Murder - " + name, (Cosmetic.listByType(cosmeticClass).size() / 7) + 4);
    this.cosmeticClass = cosmeticClass;
    this.previousPage = (this.rows * 9) - 9;
    this.nextPage = (this.rows * 9) - 1;
    this.onlySlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    this.removeSlotsWith(BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar : desc>&7Para a Loja."), (this.rows * 9) - 5);

    List<ItemStack> items = new ArrayList<>();
    List<T> cosmetics = Cosmetic.listByType(cosmeticClass);
    for (T cosmetic : cosmetics) {
      ItemStack icon = cosmetic.getIcon(profile);
      items.add(icon);
      this.cosmetics.put(icon, cosmetic);
    }

    this.setItems(items);
    cosmetics.clear();
    items.clear();

    this.register(Core.getInstance());
    this.open();
  }

  public void cancel() {
    HandlerList.unregisterAll(this);
    this.cosmeticClass = null;
    this.cosmetics.clear();
    this.cosmetics = null;
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
