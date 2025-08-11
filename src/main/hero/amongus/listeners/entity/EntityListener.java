package hero.amongus.listeners.entity;

import net.hero.services.game.GameState;
import hero.amongus.game.types.AssassinsMurder;
import hero.amongus.game.types.ClassicMurder;
import net.hero.services.player.Profile;
import net.hero.services.player.enums.BloodAndGore;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import hero.amongus.game.Murder;

public class EntityListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent evt) {
    if (evt.isCancelled()) {
      return;
    }

    if (!(evt.getDamager() instanceof Player) && !(evt.getDamager() instanceof Arrow)) {
      evt.setCancelled(true);
      return;
    }

    if (evt.getEntity() instanceof Player) {
      Player player = (Player) evt.getEntity();

      Murder game;
      Profile profile = Profile.getProfile(player.getName());
      if (profile == null || (game = profile.getGame(Murder.class)) == null || game.getState() != GameState.EMJOGO || game.isSpectator(player)) {
        evt.setCancelled(true);
      } else {
        Player damager;
        Profile profile2;
        if (evt.getDamager() instanceof Player) {
          damager = (Player) evt.getDamager();
          profile2 = Profile.getProfile(damager.getName());
          if (profile2 == null || profile2.getGame() == null || !profile2.getGame().equals(game) || game.isSpectator(damager) || damager.equals(player) || !damager.getItemInHand().getType().name().contains("SWORD")) {
            evt.setCancelled(true);
          } else {
            if (game instanceof ClassicMurder && !((ClassicMurder) game).isKiller(damager)) {
              evt.setCancelled(true);
              return;
            } else if (game instanceof AssassinsMurder) {
              AssassinsMurder am = (AssassinsMurder) game;
              if (!am.isContract(damager, player))  {
                evt.setCancelled(true);
                damager.sendMessage("§c§lVOCÊ FOI STUNADO! §cVocê apenas pode assassinar o seu alvo!");
                damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 5));
                return;
              } else if (am.getDelay(damager) != null) {
                evt.setCancelled(true);
                damager.sendMessage("§cVocê não pode assassinar ninguém durante 5 segundos após jogar a sua faca!");
                return;
              }
            }

            if (profile.getPreferencesContainer().getBloodAndGore() == BloodAndGore.ATIVADO) {
              player.playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            }
            if (profile2.getPreferencesContainer().getBloodAndGore() == BloodAndGore.ATIVADO) {
              damager.playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            }
          }
        } else {
          damager = (Player) ((Arrow) evt.getDamager()).getShooter();
          profile2 = Profile.getProfile(damager.getName());
          if (profile2 == null || profile2.getGame() == null || !profile2.getGame().equals(game) || game.isSpectator(damager) || damager.equals(player)) {
            evt.setCancelled(true);
          } else {
            if (profile.getPreferencesContainer().getBloodAndGore() == BloodAndGore.ATIVADO) {
              player.playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            }
            if (profile2.getPreferencesContainer().getBloodAndGore() == BloodAndGore.ATIVADO) {
              damager.playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            }
          }
        }

        if (!evt.isCancelled()) {
          evt.setDamage(20.0);
          profile.setHit(damager.getName());
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDamage(EntityDamageEvent evt) {
    if (evt.getEntity() instanceof Player) {
      Player player = (Player) evt.getEntity();

      Profile profile = Profile.getProfile(player.getName());
      if (profile != null) {
        Murder game = profile.getGame(Murder.class);
        if (game == null) {
          evt.setCancelled(true);
        } else {
          if (game.getState() != GameState.EMJOGO) {
            evt.setCancelled(true);
          } else if (game.isSpectator(player)) {
            evt.setCancelled(true);
          } else if (evt.getCause() != DamageCause.ENTITY_ATTACK && evt.getCause() != DamageCause.PROJECTILE && evt.getCause() != DamageCause.VOID) {
            evt.setCancelled(true);
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityShotBow(EntityShootBowEvent evt) {
    if (evt.getEntity() instanceof Player) {
      Player player = (Player) evt.getEntity();

      Profile profile = Profile.getProfile(player.getName());
      if (profile != null) {
        ClassicMurder game = profile.getGame(ClassicMurder.class);
        if (game != null) {
          game.setDelay(player);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureSpawn(CreatureSpawnEvent evt) {
    evt.setCancelled(evt.getSpawnReason() != SpawnReason.CUSTOM);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onProjectileHitEvent(ProjectileHitEvent evt) {
    if (evt.getEntity() instanceof Arrow) {
      evt.getEntity().remove();
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onFoodLevelChange(FoodLevelChangeEvent evt) {
    evt.setCancelled(true);
  }
}
