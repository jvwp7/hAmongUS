package hero.amongus.menus.cosmetics;

import net.hero.services.libraries.menu.PlayerMenu;
import hero.amongus.Main;
import hero.amongus.container.HotbarContainer;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import static hero.amongus.menus.cosmetics.MenuHotbarConfig.*;

public class MenuHotbarConfigSlot extends PlayerMenu {

    @EventHandler(priority = EventPriority.LOW)
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
                        if (evt.getSlot() == 49) {
                            EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
                            new MenuHotbarConfig(profile, type);
                        } else if (item.getType() == Material.STAINED_GLASS_PANE) {
                            if (item.getDurability() != 5) {
                                EnumSound.VILLAGER_NO.play(this.player, 1.0F, 1.0F);
                                return;
                            }

                            EnumSound.NOTE_PLING.play(this.player, 0.5F, 1.0F);
                            this.config.set(this.name, HotbarContainer.convertInventorySlot(evt.getSlot()));
                            new MenuHotbarConfig(profile, type);
                        }
                    }
                }
            }
        }
    }

    private String name;
    protected String type;
    private HotbarContainer config;

    public MenuHotbarConfigSlot(Profile profile, String name, String type) {
        super(profile.getPlayer(), "Escolher slot", 6);
        this.type = type;
        this.name = name;
        this.config = profile.getAbstractContainer("HeroCoreMurder", formatToT(type), HotbarContainer.class);

        for (ItemStack item : (type.equals("murder") ? ITEMS_MURDER : type.equals("inocente") ? ITEMS_INNOCENT : ITEMS_DETECTIVE)) {
            String itemName = item.getType().name().substring(0, 2) + item.getDurability();
            int slot = config.get(itemName, (type.equals("murder") ?
                    DEFAULT_MURDER : type.equals("inocente") ? DEFAULT : DEFAULT_DT).get(item));
            this.setItem(HotbarContainer.convertConfigSlot(slot), BukkitUtils.deserializeItemStack("STAINED_GLASS_PANE:14 : 1 : nome>&cSlot em uso!"));
        }

        for (int glass = 0; glass < 45; glass++) {
            if (glass >= 27 && glass < 36) {
                this.setItem(glass, BukkitUtils.deserializeItemStack("STAINED_GLASS_PANE:14 : 1 : nome>&8↑ Inventário : desc>&8↓ Hotbar"));
                continue;
            }

            ItemStack current = getItem(glass);
            if (current != null && current.getType() != Material.AIR) {
                continue;
            }

            this.setItem(glass, BukkitUtils.deserializeItemStack("STAINED_GLASS_PANE:5 : 1 : nome>&aSelecionar slot"));
        }

        this.setItem(49, BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar : desc>&7Para a Customização de Itens."));

        this.register(Main.getInstance());
        this.open();
    }

    public void cancel() {
        HandlerList.unregisterAll(this);
        this.name = null;
        this.config = null;
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

    protected String formatToT(String type) {
        return (type.equals("inocente") ? "innocent" : type.equals("murder") ? "murder" : "detective") + "Hotbar";
    }
}
