package hero.amongus.game.object.runnables;

import hero.amongus.game.enums.MurderRole;
import hero.amongus.game.MurderTeam;
import net.hero.services.nms.NMS;
import net.hero.services.player.Profile;
import net.hero.services.utils.StringUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import hero.amongus.Language;
import hero.amongus.game.Murder;

import java.util.List;
import java.util.stream.Collectors;

public class AmongUSIngame extends BukkitRunnable {

  private Murder game;

  public AmongUSIngame(Murder game) {
    this.game = game;
  }

  @Override
  public void run() {
    if (this.game.getTimer() == 0) {
      // Verificar vitória por tempo
      checkTimeVictory();
      return;
    }

    // Atualizar informações dos jogadores
    this.game.listPlayers().forEach(player -> {
      Profile profile = Profile.getProfile(player.getName());
      if (profile != null) {
        profile.update();
        
        // Mostrar informações específicas do Among Us
        MurderTeam team = this.game.getTeam(player);
        if (team != null) {
          String roleInfo = team.getRole() == MurderRole.IMPOSTOR ? "§cImpostor" : "§aTripulante";
          String taskInfo = team.getRole() == MurderRole.CREWMATE ? " - Complete suas tarefas!" : " - Elimine os tripulantes!";
          
          NMS.sendActionBar(player, roleInfo + taskInfo + " §7| §e" + StringUtils.formatNumber(this.game.getTimer()) + "s");
        }
      }
    });

    // Verificar vitória por tarefas completadas
    if (this.game.areAllTasksCompleted()) {
      crewmateVictory();
      return;
    }

    // Verificar vitória dos impostores
    List<Player> crewmates = this.game.listPlayers().stream()
        .filter(player -> {
          MurderTeam team = this.game.getTeam(player);
          return team != null && team.getRole() == MurderRole.CREWMATE;
        })
        .collect(Collectors.toList());
    
    List<Player> impostors = this.game.listPlayers().stream()
        .filter(player -> {
          MurderTeam team = this.game.getTeam(player);
          return team != null && team.getRole() == MurderRole.IMPOSTOR;
        })
        .collect(Collectors.toList());

    if (crewmates.isEmpty()) {
      impostorVictory(impostors);
      return;
    }

    if (impostors.isEmpty()) {
      crewmateVictory();
      return;
    }

    // Atualizar timer
    this.game.setTimer(this.game.getTimer() - 1);
  }

  private void checkTimeVictory() {
    // Se o tempo acabou, os impostores vencem
    List<Player> impostors = this.game.listPlayers().stream()
        .filter(player -> {
          MurderTeam team = this.game.getTeam(player);
          return team != null && team.getRole() == MurderRole.IMPOSTOR;
        })
        .collect(Collectors.toList());
    
    if (!impostors.isEmpty()) {
      impostorVictory(impostors);
    } else {
      crewmateVictory();
    }
  }

  private void impostorVictory(List<Player> impostors) {
    this.game.broadcastMessage("§c§lVITÓRIA DOS IMPOSTORES!");
    this.game.broadcastMessage("§cOs impostores eliminaram todos os tripulantes!");
    
    // Encontrar o time dos impostores
    MurderTeam impostorTeam = null;
    for (Player impostor : impostors) {
      MurderTeam team = this.game.getTeam(impostor);
      if (team != null && team.getRole() == MurderRole.IMPOSTOR) {
        impostorTeam = team;
        break;
      }
    }
    
    if (impostorTeam != null) {
      this.game.setState(net.hero.services.game.GameState.ENCERRADO);
      this.game.getTask().swap(impostorTeam);
    }
  }

  private void crewmateVictory() {
    this.game.broadcastMessage("§a§lVITÓRIA DOS TRIPULANTES!");
    this.game.broadcastMessage("§aOs tripulantes completaram todas as tarefas!");
    
    // Encontrar o time dos tripulantes
    MurderTeam crewmateTeam = null;
    for (Player player : this.game.listPlayers()) {
      MurderTeam team = this.game.getTeam(player);
      if (team != null && team.getRole() == MurderRole.CREWMATE) {
        crewmateTeam = team;
        break;
      }
    }
    
    if (crewmateTeam != null) {
      this.game.setState(net.hero.services.game.GameState.ENCERRADO);
      this.game.getTask().swap(crewmateTeam);
    }
  }

  @Override
  public synchronized void cancel() throws IllegalStateException {
    super.cancel();
    this.game = null;
  }
}
