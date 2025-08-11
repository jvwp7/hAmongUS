package hero.amongus.game.object;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Representa uma tarefa no jogo Among Us
 */
public class Task {

  private String name;
  private String description;
  private Location location;
  private TaskType type;
  private boolean completed;
  private Material icon;
  
  /**
   * Cria uma nova tarefa
   * @param name Nome da tarefa
   * @param description Descrição da tarefa
   * @param location Localização da tarefa
   * @param type Tipo da tarefa
   * @param icon Ícone da tarefa
   */
  public Task(String name, String description, Location location, TaskType type, Material icon) {
    this.name = name;
    this.description = description;
    this.location = location;
    this.type = type;
    this.completed = false;
    this.icon = icon;
  }
  
  /**
   * Cria uma nova tarefa com ícone padrão
   * @param name Nome da tarefa
   * @param description Descrição da tarefa
   * @param location Localização da tarefa
   * @param type Tipo da tarefa
   */
  public Task(String name, String description, Location location, TaskType type) {
    this(name, description, location, type, Material.PAPER);
  }
  
  /**
   * Obtém o nome da tarefa
   * @return Nome da tarefa
   */
  public String getName() {
    return name;
  }
  
  /**
   * Obtém a descrição da tarefa
   * @return Descrição da tarefa
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Obtém a localização da tarefa
   * @return Localização da tarefa
   */
  public Location getLocation() {
    return location;
  }
  
  /**
   * Obtém o tipo da tarefa
   * @return Tipo da tarefa
   */
  public TaskType getType() {
    return type;
  }
  
  /**
   * Verifica se a tarefa foi completada
   * @return true se a tarefa foi completada, false caso contrário
   */
  public boolean isCompleted() {
    return completed;
  }
  
  /**
   * Define se a tarefa foi completada
   * @param completed true se a tarefa foi completada, false caso contrário
   */
  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
  
  /**
   * Obtém o ícone da tarefa
   * @return Ícone da tarefa
   */
  public Material getIcon() {
    return icon;
  }
  
  /**
   * Inicia a tarefa para um jogador
   * @param player Jogador
   */
  public void start(Player player) {
    // Implementação específica para cada tipo de tarefa
  }
  
  /**
   * Completa a tarefa para um jogador
   * @param player Jogador
   */
  public void complete(Player player) {
    this.completed = true;
  }
  
  /**
   * Tipos de tarefas no Among Us
   */
  public enum TaskType {
    COMMON, // Tarefas comuns (todos os tripulantes têm)
    SHORT, // Tarefas curtas
    LONG, // Tarefas longas
    VISUAL // Tarefas visuais (outros jogadores podem ver)
  }
}