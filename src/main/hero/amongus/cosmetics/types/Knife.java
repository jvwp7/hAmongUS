package hero.amongus.cosmetics.types;

import net.hero.services.cash.CashManager;
import net.hero.services.player.Profile;
import net.hero.services.player.role.Role;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumRarity;
import org.bukkit.inventory.ItemStack;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.Cosmetic;
import hero.amongus.cosmetics.CosmeticType;

public class Knife extends Cosmetic {

  private String name;
  private String icon;
  private ItemStack item;

  public Knife(long id, EnumRarity rarity, double coins, long cash, String permission, String name, String icon) {
    super(id, CosmeticType.KNIFE, coins, permission);
    this.name = name;
    this.icon = icon;
    this.item = BukkitUtils.deserializeItemStack(icon);
    this.rarity = rarity;
    this.cash = cash;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public ItemStack getItem() {
    return this.item;
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
      Language.cosmetics$knife$icon$has_desc$start.replace("{has_desc_status}", isSelected ? Language.cosmetics$icon$has_desc$selected : Language.cosmetics$icon$has_desc$select) :
      canBuy ?
        Language.cosmetics$knife$icon$buy_desc$start.replace("{buy_desc_status}",
          (coins >= this.getCoins() || (CashManager.CASH && cash >= this.getCash())) ? Language.cosmetics$icon$buy_desc$click_to_buy : Language.cosmetics$icon$buy_desc$enough) :
        Language.cosmetics$knife$icon$perm_desc$start
          .replace("{perm_desc_status}", (role == null ? Language.cosmetics$icon$perm_desc$common : Language.cosmetics$icon$perm_desc$role.replace("{role}", role.getName()))))
      .replace("{name}", this.name).replace("{rarity}", this.getRarity().getName()).replace("{coins}", StringUtils.formatNumber(this.getCoins()))
      .replace("{cash}", StringUtils.formatNumber(this.getCash()));
    ItemStack item = BukkitUtils.deserializeItemStack(this.icon + " : nome>" + color + this.name + " : desc>" + desc);
    if (isSelected) {
      BukkitUtils.putGlowEnchantment(item);
    }

    return item;
  }

  public static void setupKnifes() {
    KConfig config = Main.getInstance().getConfig("cosmetics", "knifes");

    for (String key : config.getKeys(false)) {
      long id = config.getInt(key + ".id");
      double coins = config.getDouble(key + ".coins");
      long cash = config.getInt(key + ".cash", 0);
      String permission = config.getString(key + ".permission");
      String name = config.getString(key + ".name");
      String icon = config.getString(key + ".icon");

      new Knife(id, EnumRarity.fromName(config.getString(key + ".rarity")), coins, cash, permission, name, icon);
    }
  }
}
