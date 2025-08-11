package hero.amongus.game.object;

import hero.amongus.game.Murder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma sabotagem no jogo Among Us
 */
public class Sabotage {

  private Murder game;
  private SabotageType type;
  private Location location;
  private int timeLeft; // Tempo restante em segundos
  private boolean active;
  private List<Player> fixingPlayers; // Jogadores tentando consertar
  
  /**
   * Cria uma nova sabotagem
   * @param game Jogo
   * @param type Tipo de sabotagem
   * @param location Localização da sabotagem
   * @param timeLimit Tempo limite em segundos (0 para sabotagens sem tempo)
   */
  public Sabotage(Murder game, SabotageType type, Location location, int timeLimit) {
    this.game = game;
    this.type = type;
    this.location = location;
    this.timeLeft = timeLimit;
    this.active = false;
    this.fixingPlayers = new ArrayList<>();
  }
  
  /**
   * Cria uma nova sabotagem com tempo limite padrão
   * @param game Jogo
   * @param type Tipo de sabotagem
   * @param location Localização da sabotagem
   */
  public Sabotage(Murder game, SabotageType type, Location location) {
    this(game, type, location, 60); // Tempo padrão de 60 segundos
  }
  
  /**
   * Ativa a sabotagem
   * @return true se foi ativada, false se já estava ativa
   */
  public boolean activate() {
    if (active) {
      return false;
    }
    
    this.active = true;
    return true;
  }
  
  /**
   * Desativa a sabotagem
   */
  public void deactivate() {
    this.active = false;
    this.fixingPlayers.clear();
  }
  
  /**
   * Verifica se a sabotagem está ativa
   * @return true se está ativa, false caso contrário
   */
  public boolean isActive() {
    return active;
  }
  
  /**
   * Obtém o tipo de sabotagem
   * @return Tipo de sabotagem
   */
  public SabotageType getType() {
    return type;
  }
  
  /**
   * Obtém a localização da sabotagem
   * @return Localização
   */
  public Location getLocation() {
    return location;
  }
  
  /**
   * Obtém o tempo restante da sabotagem
   * @return Tempo em segundos
   */
  public int getTimeLeft() {
    return timeLeft;
  }
  
  /**
   * Reduz o tempo restante da sabotagem
   * @return true se o tempo acabou, false caso contrário
   */
  public boolean tick() {
    if (!active || timeLeft <= 0) {
      return false;
    }
    
    timeLeft--;
    return timeLeft <= 0;
  }
  
  /**
   * Adiciona um jogador à lista de jogadores tentando consertar
   * @param player Jogador
   * @return true se foi adicionado, false se já estava na lista
   */
  public boolean addFixingPlayer(Player player) {
    if (fixingPlayers.contains(player)) {
      return false;
    }
    
    fixingPlayers.add(player);
    return true;
  }
  
  /**
   * Remove um jogador da lista de jogadores tentando consertar
   * @param player Jogador
   * @return true se foi removido, false se não estava na lista
   */
  public boolean removeFixingPlayer(Player player) {
    return fixingPlayers.remove(player);
  }
  
  /**
   * Verifica se um jogador está tentando consertar a sabotagem
   * @param player Jogador
   * @return true se está tentando consertar, false caso contrário
   */
  public boolean isFixing(Player player) {
    return fixingPlayers.contains(player);
  }
  
  /**
   * Obtém a lista de jogadores tentando consertar
   * @return Lista de jogadores
   */
  public List<Player> getFixingPlayers() {
    return new ArrayList<>(fixingPlayers);
  }
  
  /**
   * Verifica se a sabotagem foi consertada
   * @return true se foi consertada, false caso contrário
   */
  public boolean isFixed() {
    switch (type) {
      case LIGHTS:
        // Luzes são consertadas por um único jogador
        return fixingPlayers.size() >= 1;
      case COMMUNICATIONS:
        // Comunicações são consertadas por um único jogador
        return fixingPlayers.size() >= 1;
      case REACTOR:
      case O2:
        // Reator e O2 precisam de dois jogadores em locais diferentes
        return fixingPlayers.size() >= 2;
      case DOORS:
        // Portas são consertadas automaticamente após um tempo
        return timeLeft <= 0;
      default:
        return false;
    }
  }
  
  /**
   * Tipos de sabotagem no Among Us
   */
  public enum SabotageType {
    LIGHTS, // Luzes (reduz visão dos tripulantes)
    COMMUNICATIONS, // Comunicações (desativa mapa e lista de tarefas)
    REACTOR, // Reator (tempo limitado para consertar)
    O2, // Oxigênio (tempo limitado para consertar)
    DOORS // Portas (bloqueia acesso a uma sala)
  }
}