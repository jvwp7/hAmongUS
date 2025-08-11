package hero.amongus.cosmetics.types;

import net.hero.services.cash.CashManager;
import net.hero.services.player.Profile;
import net.hero.services.player.role.Role;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumRarity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.Cosmetic;
import hero.amongus.cosmetics.CosmeticType;

public class Hat extends Cosmetic {

    protected String name;
    protected String texture;
    protected String icon;

    public Hat(long id, EnumRarity rarity, double coins, long cash, String permission, String name, String texture, String icon) {
        super(id, CosmeticType.HAT, coins, permission);
        this.name = name;
        this.texture = texture;
        this.rarity = rarity;
        this.icon = icon;
        this.cash = cash;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void equip(Player player) {
        player.getInventory().setHelmet(BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : nome>&a" + this.name + " : skin>" + this.texture));
    }

    public String getTexture() {
        return this.texture;
    }

    @Override
    public ItemStack getIcon(Profile profile) {
        double coins = profile.getCoins("HeroCoreMurder");
        long cash = profile.getStats("HeroCoreProfile", "cash");
        boolean has = this.has(profile);
        boolean canBuy = this.canBuy(profile.getPlayer());
        boolean isSelected = this.isSelected(profile);
        if (isSelected && !canBuy) {
            isSelected = false;
            profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).setSelected(getType(), 0);
        }

        Role role = Role.getRoleByPermission(this.getPermission());
        String color = has ?
                (isSelected ? Language.cosmetics$color$selected : Language.cosmetics$color$unlocked) :
                (coins >= this.getCoins() || (CashManager.CASH && cash >= this.getCash())) && canBuy ? Language.cosmetics$color$canbuy : Language.cosmetics$color$locked;
        String desc = (has && canBuy ?
                Language.cosmetics$hat$icon$has_desc$start.replace("{has_desc_status}", isSelected ? Language.cosmetics$icon$has_desc$selected : Language.cosmetics$icon$has_desc$select) :
                canBuy ?
                        Language.cosmetics$hat$icon$buy_desc$start.replace("{buy_desc_status}",
                                (coins >= this.getCoins() || (CashManager.CASH && cash >= this.getCash())) ? Language.cosmetics$icon$buy_desc$click_to_buy : Language.cosmetics$icon$buy_desc$enough) :
                        Language.cosmetics$hat$icon$perm_desc$start
                                .replace("{perm_desc_status}", (role == null ? Language.cosmetics$icon$perm_desc$common : Language.cosmetics$icon$perm_desc$role.replace("{role}", role.getName()))))
                .replace("{name}", this.name).replace("{rarity}", this.getRarity().getName()).replace("{coins}", StringUtils.formatNumber(this.getCoins()))
                .replace("{cash}", StringUtils.formatNumber(this.getCash()));
        ItemStack item = BukkitUtils.deserializeItemStack(this.icon + " : nome>" + color + this.name + " : desc>" + desc);
        if (isSelected) {
            BukkitUtils.putGlowEnchantment(item);
        }

        return item;
    }

    public static void setupHats() {
        KConfig config = Main.getInstance().getConfig("cosmetics", "hats");

        for (String key : config.getKeys(false)) {
            long id = config.getInt(key + ".id");
            double coins = config.getDouble(key + ".coins");
            long cash = config.getInt(key + ".cash", 0);
            String permission = config.getString(key + ".permission");
            String name = config.getString(key + ".name");
            String icon = config.getString(key + ".icon");
            String texture = config.getString(key + ".texture");
            if (!texture.startsWith("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv")) {
                texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" + texture;
            }

            new Hat(id, EnumRarity.fromName(config.getString(key + ".rarity")), coins, cash, permission, name, texture, icon);
        }
    }
}