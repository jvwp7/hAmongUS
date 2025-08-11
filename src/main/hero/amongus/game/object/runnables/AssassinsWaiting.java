package hero.amongus.game.object.runnables;

import hero.amongus.game.types.AssassinsMurder;
import net.hero.services.player.Profile;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.scheduler.BukkitRunnable;
import hero.amongus.Language;
import hero.amongus.game.Murder;

public class AssassinsWaiting extends BukkitRunnable {

  private AssassinsMurder game;

  public AssassinsWaiting(Murder game) {
    this.game = (AssassinsMurder) game;
  }

  @Override
  public void run() {
    if (this.game.getTimer() == 0) {
      this.game.start();
      return;
    }

    if (this.game.getOnline() < this.game.getConfig().getMinPlayers()) {
      if (this.game.getTimer() != (Language.options$start$waiting + 1)) {
        this.game.setTimer(Language.options$start$waiting + 1);
      }

      this.game.listPlayers().forEach(player -> Profile.getProfile(player.getName()).update());
      return;
    }

    if (this.game.getTimer() == (Language.options$start$waiting + 1)) {
      this.game.setTimer(Language.options$start$waiting);
    }

    this.game.listPlayers().forEach(player -> {
      Profile.getProfile(player.getName()).update();
      if (this.game.getTimer() == 10 || game.getTimer() <= 5) {
        EnumSound.CLICK.play(player, 0.5F, 2.0F);
      }
    });

    if (this.game.getTimer() == 30 || this.game.getTimer() == 15 || this.game.getTimer() == 10 || this.game.getTimer() <= 5) {
      this.game
        .broadcastMessage(Language.ingame$broadcast$starting.replace("{time}", StringUtils.formatNumber(this.game.getTimer())).replace("{s}", this.game.getTimer() > 1 ? "s" : ""));
    }

    this.game.setTimer(this.game.getTimer() - 1);
  }

  @Override
  public synchronized void cancel() throws IllegalStateException {
    super.cancel();
    this.game = null;
  }
}
