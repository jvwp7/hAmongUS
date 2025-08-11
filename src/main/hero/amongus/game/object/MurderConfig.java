package hero.amongus.game.object;

import hero.amongus.game.enums.MurderMode;
import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import hero.amongus.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static hero.amongus.utils.VoidChunkGenerator.VOID_CHUNK_GENERATOR;

public class MurderConfig {

  private KConfig config;

  private String yaml;
  private World world;
  private String name;
  private MurderMode mode;
  private String spawnLocation;
  private List<String> spawns;
  private List<String> golds;
  private int minPlayers;
  private int maxPlayers;
  private List<Room> rooms;
  private List<Task> tasks;
  private List<Sabotage> sabotages;
  private int killCooldown;
  private int meetingTime;
  private int votingTime;
  private int taskTime;
  private double playerSpeed;
  private double killDistance;

  public MurderConfig(String yaml) {
    this.yaml = yaml;
    this.config = Main.getInstance().getConfig("arenas", this.yaml);
    
    // Verificar se a configuração tem os campos necessários
    if (!this.config.contains("name") || !this.config.contains("mode")) {
      Main.getInstance().getLogger().warning("Configuração de arena \"" + yaml + "\" incompleta, usando valores padrão...");
      this.name = yaml;
      this.mode = MurderMode.IMPOSTOR_1; // Valor padrão
      this.spawnLocation = "world,0,64,0,0,0";
      this.minPlayers = 3;
      this.maxPlayers = 8;
      this.spawns = new ArrayList<>();
      this.golds = new ArrayList<>();
      this.rooms = new ArrayList<>();
      this.tasks = new ArrayList<>();
      this.sabotages = new ArrayList<>();
      this.killCooldown = 30;
      this.meetingTime = 15;
      this.votingTime = 120;
      this.taskTime = 5;
      this.playerSpeed = 1.0;
      this.killDistance = 1.5;
      return; // Não tentar recarregar se não há configuração válida
    }
    
    this.name = this.config.getString("name");
    this.mode = MurderMode.fromName(this.config.getString("mode"));
    this.spawnLocation = this.config.getString("spawn");
    this.minPlayers = this.config.getInt("minPlayers");
    this.maxPlayers = this.config.getInt("maxPlayers", this.mode.getSize());
    this.spawns = this.config.getStringList("spawns");
    this.golds = this.config.getStringList("golds");
    this.rooms = new ArrayList<>();
    this.tasks = new ArrayList<>();
    this.sabotages = new ArrayList<>();
    this.killCooldown = this.config.getInt("kill_cooldown", 30);
    this.meetingTime = this.config.getInt("meeting_time", 15);
    this.votingTime = this.config.getInt("voting_time", 120);
    this.taskTime = this.config.getInt("task_time", 5);
    this.playerSpeed = this.config.getDouble("player_speed", 1.0);
    this.killDistance = this.config.getDouble("kill_distance", 1.5);
    
    // Só tentar recarregar se há configuração válida
    try {
      this.reload();
    } catch (Exception ex) {
      Main.getInstance().getLogger().warning("Erro ao recarregar arena \"" + yaml + "\": " + ex.getMessage());
    }
  }

  public void destroy() {
    if ((this.world = Bukkit.getWorld(this.yaml)) != null) {
      Bukkit.unloadWorld(this.world, false);
    }

    Main.getInstance().getFileUtils().deleteFile(new File(this.yaml));
    this.yaml = null;
    this.name = null;
    this.mode = null;
    this.spawns.clear();
    this.spawns = null;
    this.world = null;
    this.config = null;
  }

  public void reload() {
    File file = new File("plugins/hAmongUS/mundos/" + this.yaml);
    if ((this.world = Bukkit.getWorld(file.getName())) != null) {
      Bukkit.unloadWorld(this.world, false);
    }

    Main.getInstance().getFileUtils().deleteFile(new File(file.getName()));
    Main.getInstance().getFileUtils().copyFiles(file, new File(file.getName()));

    WorldCreator wc = WorldCreator.name(file.getName());
    wc.generator(VOID_CHUNK_GENERATOR);
    wc.generateStructures(false);
    this.world = wc.createWorld();
    this.world.setTime(0L);
    this.world.setStorm(false);
    this.world.setThundering(false);
    this.world.setAutoSave(false);
    this.world.setAnimalSpawnLimit(0);
    this.world.setWaterAnimalSpawnLimit(0);
    this.world.setKeepSpawnInMemory(false);
    this.world.setGameRuleValue("doMobSpawning", "false");
    this.world.setGameRuleValue("doDaylightCycle", "false");
    this.world.setGameRuleValue("mobGriefing", "false");
    this.world.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
  }

  public Location getRandomGold() {
    return BukkitUtils.deserializeLocation(listGolds().get(ThreadLocalRandom.current().nextInt(listGolds().size())));
  }

  public World getWorld() {
    return this.world;
  }

  public KConfig getConfig() {
    return this.config;
  }

  public String getMapName() {
    return this.name;
  }

  public MurderMode getMode() {
    return this.mode;
  }

  public Location getSpawnLocation() {
    return BukkitUtils.deserializeLocation(this.spawnLocation);
  }

  public List<String> listSpawns() {
    return this.spawns;
  }

  public List<String> listGolds() {
    return this.golds;
  }

  public int getMinPlayers() {
    return minPlayers;
  }

  /**
   * Define o nome do mapa
   * @param name Nome do mapa
   */
  public void setMapName(String name) {
    this.name = name;
  }

  /**
   * Define o número mínimo de jogadores
   * @param minPlayers Número mínimo de jogadores
   */
  public void setMinPlayers(int minPlayers) {
    this.minPlayers = minPlayers;
  }

  /**
   * Define o número máximo de jogadores
   * @param maxPlayers Número máximo de jogadores
   */
  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  /**
   * Obtém o número máximo de jogadores
   * @return Número máximo de jogadores
   */
  public int getMaxPlayers() {
    return this.maxPlayers;
  }

  /**
   * Define as salas do mapa
   * @param rooms Lista de salas
   */
  public void setRooms(List<Room> rooms) {
    this.rooms = rooms;
  }

  /**
   * Obtém as salas do mapa
   * @return Lista de salas
   */
  public List<Room> getRooms() {
    return this.rooms;
  }

  /**
   * Define as tarefas do mapa
   * @param tasks Lista de tarefas
   */
  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }

  /**
   * Obtém as tarefas do mapa
   * @return Lista de tarefas
   */
  public List<Task> getTasks() {
    return this.tasks;
  }

  /**
   * Define as sabotagens do mapa
   * @param sabotages Lista de sabotagens
   */
  public void setSabotages(List<Sabotage> sabotages) {
    this.sabotages = sabotages;
  }

  /**
   * Obtém as sabotagens do mapa
   * @return Lista de sabotagens
   */
  public List<Sabotage> getSabotages() {
    return this.sabotages;
  }

  /**
   * Define o cooldown de kill
   * @param cooldown Cooldown em segundos
   */
  public void setKillCooldown(int cooldown) {
    this.killCooldown = cooldown;
  }

  /**
   * Obtém o cooldown de kill
   * @return Cooldown em segundos
   */
  public int getKillCooldown() {
    return this.killCooldown;
  }

  /**
   * Define o tempo de reunião
   * @param time Tempo em segundos
   */
  public void setMeetingTime(int time) {
    this.meetingTime = time;
  }

  /**
   * Obtém o tempo de reunião
   * @return Tempo em segundos
   */
  public int getMeetingTime() {
    return this.meetingTime;
  }

  /**
   * Define o tempo de votação
   * @param time Tempo em segundos
   */
  public void setVotingTime(int time) {
    this.votingTime = time;
  }

  /**
   * Obtém o tempo de votação
   * @return Tempo em segundos
   */
  public int getVotingTime() {
    return this.votingTime;
  }

  /**
   * Define o tempo das tarefas
   * @param time Tempo em segundos
   */
  public void setTaskTime(int time) {
    this.taskTime = time;
  }

  /**
   * Obtém o tempo das tarefas
   * @return Tempo em segundos
   */
  public int getTaskTime() {
    return this.taskTime;
  }

  /**
   * Define a velocidade dos jogadores
   * @param speed Velocidade (1.0 = normal)
   */
  public void setPlayerSpeed(double speed) {
    this.playerSpeed = speed;
  }

  /**
   * Obtém a velocidade dos jogadores
   * @return Velocidade dos jogadores
   */
  public double getPlayerSpeed() {
    return this.playerSpeed;
  }

  /**
   * Define a distância de kill
   * @param distance Distância em blocos
   */
  public void setKillDistance(double distance) {
    this.killDistance = distance;
  }

  /**
   * Obtém a distância de kill
   * @return Distância em blocos
   */
  public double getKillDistance() {
    return this.killDistance;
  }

  /**
   * Define o lobby de espera
   * @param location Localização do lobby
   */
  public void setWaitingLobby(Location location) {
    this.spawnLocation = BukkitUtils.serializeLocation(location);
  }

  /**
   * Adiciona um spawn ao jogo
   * @param location Localização do spawn
   */
  public void addSpawn(Location location) {
    this.spawns.add(BukkitUtils.serializeLocation(location));
  }
}
