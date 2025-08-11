package hero.amongus.game.object;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema de ventilação do Among Us
 */
public class VentSystem {

  private Map<String, Vent> vents;
  private Map<String, List<String>> connections; // Conexões entre ventilações
  
  /**
   * Cria um novo sistema de ventilação
   */
  public VentSystem() {
    this.vents = new HashMap<>();
    this.connections = new HashMap<>();
  }
  
  /**
   * Adiciona uma ventilação ao sistema
   * @param id Identificador único da ventilação
   * @param location Localização da ventilação
   * @param roomName Nome da sala onde a ventilação está
   * @return A ventilação criada
   */
  public Vent addVent(String id, Location location, String roomName) {
    Vent vent = new Vent(id, location, roomName);
    this.vents.put(id, vent);
    this.connections.put(id, new ArrayList<>());
    return vent;
  }
  
  /**
   * Conecta duas ventilações
   * @param ventId1 ID da primeira ventilação
   * @param ventId2 ID da segunda ventilação
   * @return true se a conexão foi estabelecida, false caso contrário
   */
  public boolean connectVents(String ventId1, String ventId2) {
    if (!vents.containsKey(ventId1) || !vents.containsKey(ventId2)) {
      return false;
    }
    
    // Adiciona conexão bidirecional
    if (!connections.get(ventId1).contains(ventId2)) {
      connections.get(ventId1).add(ventId2);
    }
    
    if (!connections.get(ventId2).contains(ventId1)) {
      connections.get(ventId2).add(ventId1);
    }
    
    return true;
  }
  
  /**
   * Obtém uma ventilação pelo ID
   * @param id ID da ventilação
   * @return A ventilação, ou null se não existir
   */
  public Vent getVent(String id) {
    return vents.get(id);
  }
  
  /**
   * Obtém uma ventilação pela localização
   * @param location Localização
   * @param radius Raio de busca
   * @return A ventilação mais próxima dentro do raio, ou null se não existir
   */
  public Vent getVentByLocation(Location location, double radius) {
    Vent closest = null;
    double closestDistance = Double.MAX_VALUE;
    
    for (Vent vent : vents.values()) {
      if (vent.getLocation().getWorld().equals(location.getWorld())) {
        double distance = vent.getLocation().distance(location);
        if (distance <= radius && distance < closestDistance) {
          closest = vent;
          closestDistance = distance;
        }
      }
    }
    
    return closest;
  }
  
  /**
   * Obtém todas as ventilações conectadas a uma ventilação
   * @param ventId ID da ventilação
   * @return Lista de ventilações conectadas
   */
  public List<Vent> getConnectedVents(String ventId) {
    List<Vent> result = new ArrayList<>();
    if (!connections.containsKey(ventId)) {
      return result;
    }
    
    for (String connectedId : connections.get(ventId)) {
      Vent vent = vents.get(connectedId);
      if (vent != null) {
        result.add(vent);
      }
    }
    
    return result;
  }
  
  /**
   * Representa uma ventilação no jogo
   */
  public class Vent {
    private String id;
    private Location location;
    private String roomName;
    private Player playerInside; // Jogador dentro da ventilação
    
    /**
     * Cria uma nova ventilação
     * @param id ID da ventilação
     * @param location Localização
     * @param roomName Nome da sala
     */
    public Vent(String id, Location location, String roomName) {
      this.id = id;
      this.location = location;
      this.roomName = roomName;
      this.playerInside = null;
    }
    
    /**
     * Obtém o ID da ventilação
     * @return ID
     */
    public String getId() {
      return id;
    }
    
    /**
     * Obtém a localização da ventilação
     * @return Localização
     */
    public Location getLocation() {
      return location;
    }
    
    /**
     * Obtém o nome da sala onde a ventilação está
     * @return Nome da sala
     */
    public String getRoomName() {
      return roomName;
    }
    
    /**
     * Verifica se há um jogador dentro da ventilação
     * @return true se há um jogador, false caso contrário
     */
    public boolean isOccupied() {
      return playerInside != null;
    }
    
    /**
     * Obtém o jogador dentro da ventilação
     * @return Jogador, ou null se não houver
     */
    public Player getPlayerInside() {
      return playerInside;
    }
    
    /**
     * Define o jogador dentro da ventilação
     * @param player Jogador, ou null para remover
     */
    public void setPlayerInside(Player player) {
      this.playerInside = player;
    }
  }
}