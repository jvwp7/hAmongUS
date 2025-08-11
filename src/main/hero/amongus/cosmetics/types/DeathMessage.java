package hero.amongus.cosmetics.types;

import net.hero.services.cash.CashManager;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.Cosmetic;
import hero.amongus.cosmetics.CosmeticType;
import net.hero.services.player.Profile;
import net.hero.services.player.role.Role;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.plugin.logger.KLogger;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumRarity;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeathMessage extends Cosmetic {

    private String name;
    private String icon;
    private List<String> messages;

    public DeathMessage(long id, EnumRarity rarity, double coins, long cash, String permission, String name, String icon, List<String> messages) {
        super(id, CosmeticType.DEATH_MESSAGE, coins, permission);
        this.name = name;
        this.icon = icon;
        this.messages = messages;
        this.rarity = rarity;
        this.cash = cash;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getRandomMessage() {
        return StringUtils.formatColors(messages.get(ThreadLocalRandom.current().nextInt(messages.size())));
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
                Language.cosmetics$deathmessage$icon$has_desc$start.replace("{has_desc_status}", isSelected ? Language.cosmetics$icon$has_desc$selected : Language.cosmetics$icon$has_desc$select) :
                canBuy ?
                        Language.cosmetics$deathmessage$icon$buy_desc$start
                                .replace("{buy_desc_status}", (coins >= this.getCoins() || (CashManager.CASH && cash >= this.getCash())) ? Language.cosmetics$icon$buy_desc$click_to_buy : Language.cosmetics$icon$buy_desc$enough) :
                        Language.cosmetics$deathmessage$icon$perm_desc$start
                                .replace("{perm_desc_status}", (role == null ? Language.cosmetics$icon$perm_desc$common : Language.cosmetics$icon$perm_desc$role.replace("{role}", role.getName()))))
                .replace("{name}", this.name).replace("{rarity}", this.getRarity().getName()).replace("{coins}", StringUtils.formatNumber(this.getCoins())).replace("{cash}", StringUtils.formatNumber(this.getCash()));
        ItemStack item = BukkitUtils.deserializeItemStack(this.icon + " : nome>" + color + this.name + " : desc>" + desc);
        if (isSelected) {
            BukkitUtils.putGlowEnchantment(item);
        }

        return item;
    }

    public static final KLogger LOGGER = ((KLogger) Main.getInstance().getLogger()).getModule("DEATH_CRY");

    public static void setupDeathMessages() {
        KConfig config = Main.getInstance().getConfig("cosmetics", "deathmessages");

        for (String key : config.getKeys(false)) {
            long id = config.getInt(key + ".id");
            double coins = config.getDouble(key + ".coins");
            if (!config.contains(key + ".cash")) {
                config.set(key + ".cash", getAbsentProperty("deathmessages", key + ".cash"));
            }
            long cash = config.getInt(key + ".cash", 0);
            String permission = config.getString(key + ".permission");
            String name = config.getString(key + ".name");
            String icon = config.getString(key + ".icon");
            if (!config.contains(key + ".rarity")) {
                config.set(key + ".rarity", getAbsentProperty("deathmessages", key + ".rarity"));
            }
            List<String> sound = config.getStringList(key + ".messages");

            new DeathMessage(id, EnumRarity.fromName(config.getString(key + ".rarity")), coins, cash, permission, name, icon, sound);
        }
    }
}
