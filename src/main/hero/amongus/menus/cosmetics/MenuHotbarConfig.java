package hero.amongus.menus.cosmetics;

import net.hero.services.libraries.menu.PlayerMenu;
import hero.amongus.Main;
import hero.amongus.container.HotbarContainer;
import hero.amongus.menus.MenuShop;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MenuHotbarConfig extends PlayerMenu {

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
                            new MenuHotbarConfigSelect(profile);
                        } else if (this.itemIndex.containsKey(item)) {
                            EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
                            new MenuHotbarConfigSlot(profile, this.itemIndex.get(item), type);
                        }
                    }
                }
            }
        }
    }

    private HotbarContainer config;
    private Map<ItemStack, String> itemIndex;

    protected static final List<ItemStack> ITEMS_DETECTIVE = Arrays
            .asList(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 1));
    protected static final List<ItemStack> ITEMS_MURDER = Arrays
            .asList(new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.COMPASS), new ItemStack(Material.GOLD_INGOT));
    protected static final List<ItemStack> ITEMS_INNOCENT = Collections.singletonList(new ItemStack(Material.GOLD_INGOT));
    protected static final Map<ItemStack, Integer> DEFAULT = new HashMap<>(), DEFAULT_MURDER = new HashMap<>(), DEFAULT_DT = new HashMap<>();

    static {
        DEFAULT_MURDER.put(ITEMS_MURDER.get(0), 0);
        DEFAULT_MURDER.put(ITEMS_MURDER.get(1), 1);
        DEFAULT_DT.put(ITEMS_DETECTIVE.get(0), 0);
        DEFAULT_DT.put(ITEMS_DETECTIVE.get(1), 26);
        DEFAULT.put(ITEMS_INNOCENT.get(0), 8);
        DEFAULT_MURDER.put(ITEMS_MURDER.get(2), 8);
    }

    protected String type;

    public MenuHotbarConfig(Profile profile, String type) {
        super(profile.getPlayer(), "Customizar itens - " + StringUtils.capitalise(type.equals("murder") ? "assassino" : type), 6);

        this.type = type;
        this.config = profile.getAbstractContainer("HeroCoreMurder", formatToT(type), HotbarContainer.class);
        this.itemIndex = new HashMap<>();

        for (ItemStack item : (type.equals("murder") ? ITEMS_MURDER : type.equals("inocente") ? ITEMS_INNOCENT : ITEMS_DETECTIVE)) {
            String name = item.getType().name().substring(0, 2) + item.getDurability();
            int slot = config.get(name, (type.equals("murder") ?
                    DEFAULT_MURDER : type.equals("inocente") ? DEFAULT : DEFAULT_DT).get(item));
            this.setItem(HotbarContainer.convertConfigSlot(slot), item);
            this.itemIndex.put(item, name);
        }

        for (int glass = 27; glass < 36; glass++) {
            this.setItem(glass, BukkitUtils.deserializeItemStack("STAINED_GLASS_PANE:14 : 1 : nome>&8↑ Inventário : desc>&8↓ Hotbar"));
        }

        this.setItem(49, BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar"));

        this.register(Main.getInstance());
        this.open();
    }

    public void cancel() {
        HandlerList.unregisterAll(this);
        this.config = null;
        this.itemIndex.clear();
        this.itemIndex = null;
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
