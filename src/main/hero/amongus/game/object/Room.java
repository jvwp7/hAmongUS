package hero.amongus.game.object;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma sala no mapa do Among Us
 */
public class Room {

  private String name;
  private Location center;
  private List<Task> tasks;
  private List<Location> vents; // Condutas de ventilação
  private boolean hasEmergencyButton; // Botão de emergência
  
  /**
   * Cria uma nova sala
   * @param name Nome da sala
   * @param center Localização central da sala
   */
  public Room(String name, Location center) {
    this.name = name;
    this.center = center;
    this.tasks = new ArrayList<>();
    this.vents = new ArrayList<>();
    this.hasEmergencyButton = false;
  }
  
  /**
   * Obtém o nome da sala
   * @return Nome da sala
   */
  public String getName() {
    return name;
  }
  
  /**
   * Obtém a localização central da sala
   * @return Localização central
   */
  public Location getCenter() {
    return center;
  }
  
  /**
   * Adiciona uma tarefa à sala
   * @param task Tarefa
   */
  public void addTask(Task task) {
    this.tasks.add(task);
  }
  
  /**
   * Obtém todas as tarefas da sala
   * @return Lista de tarefas
   */
  public List<Task> getTasks() {
    return tasks;
  }
  
  /**
   * Adiciona uma conduta de ventilação à sala
   * @param vent Localização da conduta
   */
  public void addVent(Location vent) {
    this.vents.add(vent);
  }
  
  /**
   * Obtém todas as condutas de ventilação da sala
   * @return Lista de localizações das condutas
   */
  public List<Location> getVents() {
    return vents;
  }
  
  /**
   * Verifica se a sala tem condutas de ventilação
   * @return true se a sala tem condutas, false caso contrário
   */
  public boolean hasVents() {
    return !vents.isEmpty();
  }
  
  /**
   * Verifica se uma localização está dentro desta sala
   * @param location Localização a verificar
   * @param radius Raio da sala a partir do centro
   * @return true se a localização está na sala, false caso contrário
   */
  public boolean contains(Location location, double radius) {
    if (!location.getWorld().equals(center.getWorld())) {
      return false;
    }
    
    return location.distance(center) <= radius;
  }

  /**
   * Define se a sala tem botão de emergência
   * @param hasEmergencyButton true se tem botão de emergência, false caso contrário
   */
  public void setHasEmergencyButton(boolean hasEmergencyButton) {
    this.hasEmergencyButton = hasEmergencyButton;
  }

  /**
   * Verifica se a sala tem botão de emergência
   * @return true se tem botão de emergência, false caso contrário
   */
  public boolean hasEmergencyButton() {
    return hasEmergencyButton;
  }
}