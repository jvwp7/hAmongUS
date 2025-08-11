package hero.amongus.game.object;

import hero.amongus.game.Murder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa uma reunião de emergência no Among Us
 */
public class Meeting {

  private Murder game;
  private Player caller; // Jogador que chamou a reunião
  private Player reportedBody; // Corpo reportado (null se for botão de emergência)
  private int timeLeft; // Tempo restante em segundos
  private Map<UUID, UUID> votes; // Votos: <Votante, Votado>
  private Map<UUID, Integer> voteCount; // Contagem de votos: <Votado, Quantidade>
  private boolean votingPhase; // true se está na fase de votação, false se está na fase de discussão
  
  /**
   * Cria uma nova reunião de emergência
   * @param game Jogo
   * @param caller Jogador que chamou a reunião
   * @param reportedBody Corpo reportado (null se for botão de emergência)
   * @param discussionTime Tempo de discussão em segundos
   */
  public Meeting(Murder game, Player caller, Player reportedBody, int discussionTime) {
    this.game = game;
    this.caller = caller;
    this.reportedBody = reportedBody;
    this.timeLeft = discussionTime;
    this.votes = new HashMap<>();
    this.voteCount = new HashMap<>();
    this.votingPhase = false;
    
    // Inicializa a contagem de votos para todos os jogadores vivos
    for (Player player : game.getPlayers()) {
      if (player != null && player.isOnline() && !game.isSpectator(player)) {
        voteCount.put(player.getUniqueId(), 0);
      }
    }
  }
  
  /**
   * Obtém o jogador que chamou a reunião
   * @return Jogador
   */
  public Player getCaller() {
    return caller;
  }
  
  /**
   * Obtém o corpo reportado
   * @return Jogador morto, ou null se foi botão de emergência
   */
  public Player getReportedBody() {
    return reportedBody;
  }
  
  /**
   * Verifica se a reunião foi chamada por um corpo reportado
   * @return true se foi corpo reportado, false se foi botão de emergência
   */
  public boolean isBodyReport() {
    return reportedBody != null;
  }
  
  /**
   * Obtém o tempo restante da reunião
   * @return Tempo em segundos
   */
  public int getTimeLeft() {
    return timeLeft;
  }
  
  /**
   * Reduz o tempo restante da reunião
   * @return true se a reunião acabou, false caso contrário
   */
  public boolean tick() {
    timeLeft--;
    return timeLeft <= 0;
  }
  
  /**
   * Registra um voto
   * @param voter Jogador que votou
   * @param voted Jogador votado (null para pular)
   * @return true se o voto foi registrado, false se o jogador já votou
   */
  public boolean vote(Player voter, Player voted) {
    if (votes.containsKey(voter.getUniqueId())) {
      return false;
    }
    
    // Remove voto anterior se existir
    UUID previousVote = votes.get(voter.getUniqueId());
    if (previousVote != null) {
      voteCount.put(previousVote, voteCount.get(previousVote) - 1);
    }
    
    // Registra novo voto
    if (voted != null) {
      votes.put(voter.getUniqueId(), voted.getUniqueId());
      voteCount.put(voted.getUniqueId(), voteCount.getOrDefault(voted.getUniqueId(), 0) + 1);
    } else {
      votes.put(voter.getUniqueId(), null); // Voto para pular
    }
    
    return true;
  }
  
  /**
   * Verifica se um jogador já votou
   * @param player Jogador
   * @return true se já votou, false caso contrário
   */
  public boolean hasVoted(Player player) {
    return votes.containsKey(player.getUniqueId());
  }
  
  /**
   * Obtém o jogador em quem um jogador votou
   * @param voter Jogador que votou
   * @return UUID do jogador votado, ou null se votou para pular ou não votou
   */
  public UUID getVote(Player voter) {
    return votes.get(voter.getUniqueId());
  }
  
  /**
   * Obtém a contagem de votos para um jogador
   * @param player Jogador
   * @return Número de votos
   */
  public int getVoteCount(Player player) {
    return voteCount.getOrDefault(player.getUniqueId(), 0);
  }
  
  /**
   * Obtém o jogador mais votado
   * @return UUID do jogador mais votado, ou null se empate ou nenhum voto
   */
  public UUID getMostVoted() {
    UUID mostVoted = null;
    int maxVotes = 0;
    boolean tie = false;
    
    for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
      if (entry.getValue() > maxVotes) {
        mostVoted = entry.getKey();
        maxVotes = entry.getValue();
        tie = false;
      } else if (entry.getValue() == maxVotes && entry.getValue() > 0) {
        tie = true;
      }
    }
    
    return tie ? null : mostVoted;
  }
  
  /**
   * Verifica se todos os jogadores vivos já votaram
   * @return true se todos votaram, false caso contrário
   */
  public boolean allVoted() {
    int livingPlayers = 0;
    for (Player player : game.getPlayers()) {
      if (player != null && player.isOnline() && !game.isSpectator(player)) {
        livingPlayers++;
      }
    }
    
    return votes.size() >= livingPlayers;
  }
  
  /**
   * Verifica se a reunião está na fase de votação
   * @return true se está na fase de votação, false se está na fase de discussão
   */
  public boolean isVotingPhase() {
    return votingPhase;
  }
  
  /**
   * Define a fase da reunião
   * @param votingPhase true para fase de votação, false para fase de discussão
   * @param votingTime Tempo de votação em segundos (ignorado se votingPhase for false)
   */
  public void setVotingPhase(boolean votingPhase, int votingTime) {
    this.votingPhase = votingPhase;
    if (votingPhase) {
      this.timeLeft = votingTime;
    }
  }
}