package hero.amongus.game.object;

import net.hero.services.game.GameState;
import hero.amongus.container.SelectedContainer;
import hero.amongus.cosmetics.CosmeticType;
import hero.amongus.cosmetics.object.AbstractExecutor;
import hero.amongus.cosmetics.types.WinAnimation;
import hero.amongus.game.types.ClassicMurder;
import net.hero.services.player.Profile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.MurderTeam;
import hero.amongus.game.object.runnables.AssassinsIngame;
import hero.amongus.game.object.runnables.AssassinsWaiting;
import hero.amongus.game.object.runnables.ClassicIngame;
import hero.amongus.game.object.runnables.ClassicWaiting;
import hero.amongus.game.object.runnables.AmongUSIngame;
import hero.amongus.game.object.runnables.AmongUSWaiting;

import java.util.ArrayList;
import java.util.List;

public class MurderTask {

  private Murder game;
  private BukkitTask task;

  public MurderTask(Murder game) {
    this.game = game;
  }

  public void cancel() {
    if (this.task != null) {
      this.task.cancel();
      this.task = null;
    }
  }

  public void reset() {
    this.cancel();
    
    // Determinar qual tipo de waiting task usar baseado no tipo de jogo
    if (this.game instanceof ClassicMurder) {
      this.task = new ClassicWaiting(this.game).runTaskTimer(Main.getInstance(), 0, 20);
    } else if (this.game instanceof hero.amongus.game.types.Impostor1Murder || 
               this.game instanceof hero.amongus.game.types.Impostor2Murder || 
               this.game instanceof hero.amongus.game.types.Impostor3Murder) {
      this.task = new AmongUSWaiting(this.game).runTaskTimer(Main.getInstance(), 0, 20);
    } else {
      this.task = new AssassinsWaiting(this.game).runTaskTimer(Main.getInstance(), 0, 20);
    }
  }

  public void swap(MurderTeam winners) {
    this.cancel();
    if (this.game.getState() == GameState.EMJOGO) {
      this.game.setTimer(Language.options$ingame$time);
      this.game.getWorld().getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
      
      // Determinar qual tipo de ingame task usar baseado no tipo de jogo
      if (this.game instanceof ClassicMurder) {
        this.task = new ClassicIngame(this.game).runTaskTimer(Main.getInstance(), 0, 20);
      } else if (this.game instanceof hero.amongus.game.types.Impostor1Murder || 
                 this.game instanceof hero.amongus.game.types.Impostor2Murder || 
                 this.game instanceof hero.amongus.game.types.Impostor3Murder) {
        this.task = new AmongUSIngame(this.game).runTaskTimer(Main.getInstance(), 0, 20);
      } else {
        this.task = new AssassinsIngame(this.game).runTaskTimer(Main.getInstance(), 0, 20);
      }
    } else if (this.game.getState() == GameState.ENCERRADO) {
      this.game.setTimer(10);
      List<AbstractExecutor> executors = new ArrayList<>();
      if (winners != null) {
        winners.listPlayers().forEach(player -> executors.add(
                Profile.getProfile(player.getName()).getAbstractContainer("HeroCoreMurder", "selected", SelectedContainer.class)
                        .getSelected(CosmeticType.WIN_ANIMATION, WinAnimation.class).execute(player)));
      }
      this.task = new BukkitRunnable() {

        @Override
        public void run() {
          if (game.getTimer() == 0) {
            executors.forEach(AbstractExecutor::cancel);
            executors.clear();
            game.listPlayers().forEach(player -> game.leave(Profile.getProfile(player.getName()), null));
            game.reset();
            return;
          }

          executors.forEach(executor -> {
            if (winners == null || !winners.listPlayers().contains(executor.getPlayer())) {
              return;
            }

            executor.tick();
          });

          game.setTimer(game.getTimer() - 1);
        }
      }.runTaskTimer(Main.getInstance(), 0, 20);
    }
  }
}
