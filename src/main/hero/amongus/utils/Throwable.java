package hero.amongus.utils;

import hero.amongus.Main;
import hero.amongus.cosmetics.types.Knife;
import hero.amongus.game.types.AssassinsMurder;
import hero.amongus.game.types.ClassicMurder;
import net.hero.services.player.Profile;
import net.hero.services.utils.particles.ParticleEffect;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.CosmeticType;

import java.util.Arrays;
import java.util.List;

public class Throwable {

  private final static List<Material> passable = Arrays
    .asList(Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.WALL_BANNER, Material.WALL_SIGN, Material.CARPET, Material.CARROT_ITEM, Material.CROPS,
      Material.DEAD_BUSH, Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.TORCH,
      Material.DOUBLE_PLANT, Material.LONG_GRASS);

  public static void throwSword(AssassinsMurder game, final Profile profile, final ItemStack item) {
    final Player player = profile.getPlayer();
    Vector vec = player.getLocation().getDirection();
    Location frontLocation = player.getLocation().add(vec);

    ItemStack hand = item;
    Knife knife = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.KNIFE, Knife.class);
    if (knife != null) {
      hand = knife.getItem();
    }

    final ArmorStand armorStand = player.getEyeLocation().add(0, 1.0, 0.0).getWorld().spawn(frontLocation, ArmorStand.class);
    armorStand.setMetadata("MURDER", new FixedMetadataValue(Main.getInstance(), true));
    armorStand.setArms(true);
    armorStand.setGravity(false);
    armorStand.setItemInHand(hand);
    armorStand.setVisible(false);
    armorStand.setRightArmPose(new EulerAngle(Math.toRadians(0.0), Math.toRadians(-player.getLocation().getPitch()), Math.toRadians(0.0)));
    armorStand.setVelocity(player.getLocation().getDirection().multiply(0.8));

    final long end = System.currentTimeMillis() + 1500;
    new BukkitRunnable() {

      @Override
      public void run() {
        if (end < System.currentTimeMillis() || !game.equals(profile.getGame())) {
          armorStand.remove();
          ParticleEffect.EXPLOSION_NORMAL.display(0F, 0F, 0F, 0.1F, 10, armorStand.getLocation().clone().add(0, 1.5, 0), 64);
          cancel();
          return;
        }

        Vector vec = armorStand.getLocation().getDirection();
        Location frontLocation = armorStand.getLocation().add(0.0, 1.5, 0.0).add(vec);
        if (armorStand.getLocation().getWorld().getNearbyEntities(frontLocation, 0.3, 0.3, 0.3) != null) {
          for (Entity en : armorStand.getLocation().getWorld().getNearbyEntities(frontLocation, 0.3, 0.3, 0.3)) {
            if (en instanceof Player && !en.hasMetadata("NPC")) {
              Player target = (Player) en;
              Profile killed;
              if (en.equals(player) || game.isSpectator(target) || (killed = Profile.getProfile(target.getName())) == null) {
                continue;
              }

              armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(0, 1.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
              armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(0, 1.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
              armorStand.remove();
              game.thrownKill(killed, profile);
              cancel();
              break;
            }
          }
        }

        if (!passable.contains(armorStand.getLocation().add(0.0, 1.5, 0.0).getBlock().getType())) {
          armorStand.remove();
          ParticleEffect.EXPLOSION_NORMAL.display(0F, 0F, 0F, 0.1F, 10, armorStand.getLocation().clone().add(0, 1.5, 0), 64);
          cancel();
          return;
        }

        vec = armorStand.getLocation().getDirection().multiply(0.8);
        frontLocation = armorStand.getLocation().add(vec);
        armorStand.teleport(frontLocation);
      }
    }.runTaskTimer(Main.getInstance(), 0, 1);
  }

  public static void throwSword(ClassicMurder game, final Profile profile, final ItemStack item) {
    final Player player = profile.getPlayer();
    Vector vec = player.getLocation().getDirection();
    Location frontLocation = player.getLocation().add(vec);

    ItemStack hand = item;
    Knife knife = profile.getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class).getSelected(CosmeticType.KNIFE, Knife.class);
    if (knife != null) {
      hand = knife.getItem();
    }

    final ArmorStand armorStand = player.getEyeLocation().add(0, 1.0, 0.0).getWorld().spawn(frontLocation, ArmorStand.class);
    armorStand.setMetadata("MURDER", new FixedMetadataValue(Main.getInstance(), true));
    armorStand.setArms(true);
    armorStand.setGravity(false);
    armorStand.setItemInHand(hand);
    armorStand.setVisible(false);
    armorStand.setRightArmPose(new EulerAngle(Math.toRadians(0.0), Math.toRadians(-player.getLocation().getPitch()), Math.toRadians(0.0)));
    armorStand.setVelocity(player.getLocation().getDirection().multiply(0.8));

    final long end = System.currentTimeMillis() + 1500;
    new BukkitRunnable() {

      @Override
      public void run() {
        if (end < System.currentTimeMillis() || !game.equals(profile.getGame())) {
          armorStand.remove();
          ParticleEffect.EXPLOSION_NORMAL.display(0F, 0F, 0F, 0.1F, 10, armorStand.getLocation().clone().add(0, 1.5, 0), 64);
          game.setSwordDrop(armorStand.getWorld().dropItem(armorStand.getLocation().clone().add(0, 1.5, 0), item));
          cancel();
          return;
        }

        Vector vec = armorStand.getLocation().getDirection();
        Location frontLocation = armorStand.getLocation().add(0.0, 1.5, 0.0).add(vec);
        if (armorStand.getLocation().getWorld().getNearbyEntities(frontLocation, 0.3, 0.3, 0.3) != null) {
          for (Entity en : armorStand.getLocation().getWorld().getNearbyEntities(frontLocation, 0.3, 0.3, 0.3)) {
            if (en instanceof Player && !en.hasMetadata("NPC")) {
              Player target = (Player) en;
              Profile killed;
              if (en.equals(player) || game.isSpectator(target) || (killed = Profile.getProfile(target.getName())) == null) {
                continue;
              }

              armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(0, 1.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
              armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(0, 1.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
              armorStand.remove();
              game.setSwordDrop(armorStand.getWorld().dropItem(armorStand.getLocation().clone().add(0, 1.5, 0), item));
              game.thrownKill(killed, profile);
              cancel();
              break;
            }
          }
        }

        if (!passable.contains(armorStand.getLocation().add(0.0, 1.5, 0.0).getBlock().getType())) {
          armorStand.remove();
          ParticleEffect.EXPLOSION_NORMAL.display(0F, 0F, 0F, 0.1F, 10, armorStand.getLocation().clone().add(0, 1.5, 0), 64);
          game.setSwordDrop(armorStand.getWorld().dropItem(armorStand.getLocation().clone().add(0, 1.5, 0), item));
          cancel();
          return;
        }

        vec = armorStand.getLocation().getDirection().multiply(0.8);
        frontLocation = armorStand.getLocation().add(vec);
        armorStand.teleport(frontLocation);
      }
    }.runTaskTimer(Main.getInstance(), 0, 1);
  }
}
