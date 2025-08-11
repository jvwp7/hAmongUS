package hero.amongus.menus.shop;

import net.hero.services.libraries.menu.PlayerMenu;
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
import hero.amongus.Main;
import hero.amongus.menus.MenuShop;

public class MenuClassicUpgrades extends PlayerMenu {

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
            if (evt.getSlot() == 11 || evt.getSlot() == 15) {
              long chance = profile.getStats("HeroCoreMurder", evt.getSlot() == 11 ? "clchancekiller" : "clchancedetective");
              if (chance >= 5 || profile.getCoins("HeroCoreMurder") < (1000 * chance)) {
                EnumSound.ENDERMAN_TELEPORT.play(player, 1.0F, 1.0F);
                return;
              }

              EnumSound.LEVEL_UP.play(this.player, 0.5F, 1.0F);
              profile.removeCoins("HeroCoreMurder", 1000 * chance);
              profile.setStats("HeroCoreMurder", chance + 1, evt.getSlot() == 11 ? "clchancekiller" : "clchancedetective");
              this.player.sendMessage("§aVocê melhorou a chance de receber a função de " + (evt.getSlot() == 11 ? "§cAssassino" : "§6Detetive") + "§a.");
              new MenuClassicUpgrades(profile);
            } else if (evt.getSlot() == 31) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuShop(profile);
            }
          }
        }
      }
    }
  }

  public MenuClassicUpgrades(Profile profile) {
    super(profile.getPlayer(), "Melhorias - Clássico", 4);

    long chance = profile.getStats("HeroCoreMurder", "clchancekiller");
    String click = chance >= 5 ?
      "&cNível máximo atingido!" :
      profile.getCoins("HeroCoreMurder") >= (1000 * chance) ?
        "&fCusto: &6" + StringUtils.formatNumber(1000 * chance) + "\n \n&eClique para melhorar!" :
        ("&fCusto: &6" + StringUtils.formatNumber(1000 * chance) + "\n \n&cVocê não possui coins suficientes.");
    this.setItem(11, BukkitUtils.deserializeItemStack(
      "DIAMOND_SWORD : 1 : esconder>tudo : nome>&aAssassino &7(" + chance + "/5) : desc>&7Aumente sua chance de ser escolhido\n&7para a função de &cAssassino&7.\n \n" + click));

    chance = profile.getStats("HeroCoreMurder", "clchancedetective");
    click = chance >= 5 ?
      "&cNível máximo atingido!" :
      profile.getCoins("HeroCoreMurder") >= (1000 * chance) ?
        "&fCusto: &6" + StringUtils.formatNumber(1000 * chance) + "\n \n&eClique para melhorar!" :
        ("&fCusto: &6" + StringUtils.formatNumber(1000 * chance) + "\n \n&cVocê não possui coins suficientes.");
    this.setItem(15, BukkitUtils
      .deserializeItemStack("BOW : 1 : nome>&aDetetive &7(" + chance + "/5) : desc>&7Aumente sua chance de ser escolhido\n&7para a função de &6Detetive&7.\n \n" + click));

    this.setItem(31, BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar : desc>&7Para a Loja."));

    this.register(Main.getInstance());
    this.open();
  }

  public void cancel() {
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
