package hero.amongus.game;

import net.hero.services.game.GameTeam;
import hero.amongus.game.enums.MurderRole;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MurderTeam extends GameTeam {

  private int kills;
  private MurderRole role;
  private Map<UUID, Boolean> completedTasks;
  private Map<UUID, Long> killCooldowns;

  public MurderTeam(Murder game, String location, int size, MurderRole role) {
    super(game, location, size);
    this.role = role;
    this.completedTasks = new HashMap<>();
    this.killCooldowns = new HashMap<>();
  }

  @Override
  public void reset() {
    super.reset();
    this.kills = 0;
    this.completedTasks.clear();
    this.killCooldowns.clear();
  }

  public void addKills() {
    this.kills++;
  }

  public MurderRole getRole() {
    return this.role;
  }

  public int getKills() {
    return this.kills;
  }

  /**
   * Define o status de conclusão de tarefas para um jogador
   * @param player Jogador
   * @param completed true se completou todas as tarefas, false caso contrário
   */
  public void setTasksCompleted(Player player, boolean completed) {
    this.completedTasks.put(player.getUniqueId(), completed);
  }
  
  /**
   * Verifica se um jogador completou todas as suas tarefas
   * @param player Jogador
   * @return true se completou todas as tarefas, false caso contrário
   */
  public boolean hasCompletedTasks(Player player) {
    return this.completedTasks.getOrDefault(player.getUniqueId(), false);
  }
  
  /**
   * Define o cooldown de assassinato para um impostor
   * @param player Impostor
   * @param cooldownMillis Tempo em milissegundos
   */
  public void setKillCooldown(Player player, long cooldownMillis) {
    if (this.role.isImpostor()) {
      this.killCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
    }
  }
  
  /**
   * Verifica se um impostor pode matar (cooldown expirou)
   * @param player Impostor
   * @return true se pode matar, false caso contrário
   */
  public boolean canKill(Player player) {
    if (!this.role.isImpostor()) {
      return false;
    }
    
    long cooldownEnd = this.killCooldowns.getOrDefault(player.getUniqueId(), 0L);
    return System.currentTimeMillis() >= cooldownEnd;
  }
  
  /**
   * Obtém o tempo restante do cooldown de assassinato em segundos
   * @param player Impostor
   * @return Tempo em segundos, 0 se não tiver cooldown
   */
  public int getRemainingKillCooldown(Player player) {
    if (!this.role.isImpostor()) {
      return 0;
    }
    
    long cooldownEnd = this.killCooldowns.getOrDefault(player.getUniqueId(), 0L);
    long remaining = cooldownEnd - System.currentTimeMillis();
    return remaining <= 0 ? 0 : (int) (remaining / 1000);
  }
  
  @Override
  public String toString() {
    return "MurderTeam{" + "role=" + role + ", kills=" + kills + ", players=[" + this.listPlayers().stream().map(Player::getName).collect(Collectors.joining(", ")) + "]}";
  }
}
