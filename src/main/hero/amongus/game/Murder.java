package hero.amongus.game;

import static hero.amongus.hook.MMCoreHook.reloadScoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.hero.services.game.Game;
import net.hero.services.game.GameState;
import hero.amongus.Language;
import hero.amongus.Main;
import hero.amongus.game.enums.MurderMode;
import hero.amongus.game.interfaces.LoadCallback;
import hero.amongus.game.object.AmongUSMapLoader;
import hero.amongus.game.object.MurderConfig;
import hero.amongus.game.object.MurderTask;
import hero.amongus.game.object.VentSystem;
import hero.amongus.game.object.Room;
import hero.amongus.game.object.Task;
import hero.amongus.game.object.Sabotage;
import net.hero.services.player.Profile;
import net.hero.services.player.hotbar.Hotbar;
import net.hero.services.plugin.logger.KLogger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import net.hero.services.plugin.config.KConfig;

public abstract class Murder implements Game<MurderTeam> {

  protected String name;
  protected MurderConfig config;

  protected int timer;
  protected MurderTask task;
  protected GameState state;
  protected List<UUID> players;
  protected List<UUID> spectators;
  protected List<MurderTeam> teams;
  protected VentSystem ventSystem; // Sistema de ventilação

  public Murder(String name) {
    this.name = name;
    this.config = new MurderConfig(name);
    this.timer = Language.options$start$waiting + 1;
    this.task = new MurderTask(this);
    this.task.reset();
    this.state = GameState.AGUARDANDO;
    this.players = new ArrayList<>();
    this.spectators = new ArrayList<>();
    this.teams = new ArrayList<>();
    this.ventSystem = new VentSystem(); // Inicializa o sistema de ventilação
  }

  public boolean spectate(Player player, Player target) {
    if (this.getState() == GameState.AGUARDANDO) {
      player.sendMessage("§cA partida ainda não começou.");
      return false;
    }

    Profile profile = Profile.getProfile(player.getName());
    if (profile.playingGame()) {
      if (profile.getGame().equals(this)) {
        return false;
      }

      profile.getGame().leave(profile, this);
    }

    profile.setGame(this);
    spectators.add(player.getUniqueId());

    player.teleport(target.getLocation());
    reloadScoreboard(profile);
    for (Player players : Bukkit.getOnlinePlayers()) {
      if (!players.getWorld().equals(player.getWorld())) {
        player.hidePlayer(players);
        players.hidePlayer(player);
        continue;
      }

      if (isSpectator(players)) {
        players.showPlayer(player);
      } else {
        players.hidePlayer(player);
      }
      player.showPlayer(players);
    }

    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    profile.refresh();
    player.setGameMode(GameMode.ADVENTURE);
    player.spigot().setCollidesWithEntities(false);
    player.setAllowFlight(true);
    player.setFlying(true);
    return true;
  }

  @Override
  public void broadcastMessage(String message) {
    this.broadcastMessage(message, true);
  }

  @Override
  public void broadcastMessage(String message, boolean spectators) {
    this.listPlayers().forEach(player -> player.sendMessage(message));
  }

  @Override
  public void reset() {
    this.players.clear();
    this.spectators.clear();
    this.listTeams().forEach(MurderTeam::reset);
    this.task.cancel();
    addToQueue(this);
  }

  public void setTimer(int timer) {
    this.timer = timer;
  }

  public int getTimer() {
    return this.timer;
  }

  @Override
  public String getGameName() {
    return this.name;
  }

  public MurderConfig getConfig() {
    return this.config;
  }

  public World getWorld() {
    return this.config.getWorld();
  }

  public MurderTask getTask() {
    return this.task;
  }

  public String getMapName() {
    return this.config.getMapName();
  }

  public MurderMode getMode() {
    return this.config.getMode();
  }
  
  /**
   * Obtém o sistema de ventilação do jogo
   * @return Sistema de ventilação
   */
  public VentSystem getVentSystem() {
    return this.ventSystem;
  }
  
  /**
   * Define o sistema de ventilação do jogo
   * @param ventSystem Sistema de ventilação
   */
  public void setVentSystem(VentSystem ventSystem) {
    this.ventSystem = ventSystem;
  }

  @Override
  public boolean isSpectator(Player player) {
    return this.spectators.contains(player.getUniqueId());
  }

  public void setState(GameState state) {
    this.state = state;
  }

  @Override
  public GameState getState() {
    return this.state;
  }

  @Override
  public int getOnline() {
    return this.players.size();
  }

  @Override
  public int getMaxPlayers() {
    return this.config.getMaxPlayers();
  }

  /**
   * Obtém o número mínimo de jogadores
   * @return Número mínimo de jogadores
   */
  public int getMinPlayers() {
    return this.config.getMinPlayers();
  }

  public MurderTeam getAvailableTeam() {
    for (MurderTeam team : this.listTeams()) {
      if (team.canJoin()) {
        return team;
      }
    }

    return null;
  }

  @Override
  public MurderTeam getTeam(Player player) {
    return this.listTeams().stream().filter(team -> team.hasMember(player)).findAny().orElse(null);
  }

  @Override
  public List<MurderTeam> listTeams() {
    return this.teams;
  }

  @Override
  public List<Player> listPlayers() {
    return this.listPlayers(false);
  }

  @Override
  public List<Player> listPlayers(boolean spectators) {
    List<Player> players = new ArrayList<>(
        spectators ? this.spectators.size() + this.players.size() : this.players.size());
    this.players.forEach(id -> players.add(Bukkit.getPlayer(id)));
    if (spectators) {
      this.spectators.stream().filter(id -> !this.players.contains(id))
          .forEach(id -> players.add(Bukkit.getPlayer(id)));
    }

    return players.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  public static final KLogger LOGGER = ((KLogger) Main.getInstance().getLogger()).getModule("GAME");
  protected static final List<Murder> QUEUE = new ArrayList<>();
  private static final Map<String, Murder> GAMES = new HashMap<>();

  public static void addToQueue(Murder game) {
    if (QUEUE.contains(game)) {
      return;
    }

    QUEUE.add(game);
  }

  public static void setupGames() {
    try {
      new ArenaRollbackerTask().runTaskTimer(Main.getInstance(), 0, 100);

      File ymlFolder = new File("plugins/arenas");
      File mapFolder = new File("plugins/hAmongUS/mundos");

      if (!ymlFolder.exists() || !mapFolder.exists()) {
        if (!ymlFolder.exists()) {
          ymlFolder.mkdirs();
          LOGGER.info("Pasta de arenas criada: " + ymlFolder.getAbsolutePath());
        }
        if (!mapFolder.exists()) {
          mapFolder.mkdirs();
          LOGGER.info("Pasta de mapas criada: " + mapFolder.getAbsolutePath());
        }
      }

      // Verificar se há arquivos de arena para carregar
      File[] arenaFiles = ymlFolder.listFiles();
      if (arenaFiles == null || arenaFiles.length == 0) {
        LOGGER.info("Nenhum arquivo de arena encontrado. Criando configurações padrão...");
        createDefaultArenas();
      } else {
        for (File file : arenaFiles) {
          if (file.isFile() && file.getName().endsWith(".yml")) {
            try {
              load(file, null);
            } catch (Exception ex) {
              LOGGER.log(Level.WARNING, "Erro ao carregar arena \"" + file.getName() + "\": ", ex);
            }
          }
        }
      }

      LOGGER.info("Foram carregadas " + GAMES.size() + " salas.");
      
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Erro crítico ao configurar jogos: ", ex);
    }
  }

  /**
   * Cria arenas padrão para o plugin funcionar sem configuração
   */
  private static void createDefaultArenas() {
    try {
      // Criar arena padrão para Impostor 1
      File defaultArenaFile = new File("plugins/arenas", "default_impostor1.yml");
      if (!defaultArenaFile.exists()) {
        KConfig defaultConfig = Main.getInstance().getConfig("arenas", "default_impostor1");
        defaultConfig.set("name", "Arena Padrão - Impostor 1");
        defaultConfig.set("mode", "IMPOSTOR_1");
        defaultConfig.set("minPlayers", 3);
        defaultConfig.set("maxPlayers", 8);
        defaultConfig.set("spawn", "world,0,64,0,0,0");
        defaultConfig.set("spawns", new ArrayList<String>() {{
          add("world,0,64,0,0,0");
          add("world,2,64,0,0,0");
          add("world,4,64,0,0,0");
          add("world,6,64,0,0,0");
          add("world,8,64,0,0,0");
          add("world,10,64,0,0,0");
          add("world,12,64,0,0,0");
          add("world,14,64,0,0,0");
        }});
        defaultConfig.set("rooms", new ArrayList<String>());
        defaultConfig.set("tasks", new ArrayList<String>());
        defaultConfig.set("sabotages", new ArrayList<String>());
        defaultConfig.set("kill_cooldown", 30);
        defaultConfig.set("meeting_time", 15);
        defaultConfig.set("voting_time", 120);
        defaultConfig.set("task_time", 5);
        defaultConfig.set("player_speed", 1.0);
        defaultConfig.set("kill_distance", 1.5);
        defaultConfig.save();
        
        LOGGER.info("Arena padrão criada: default_impostor1.yml");
      }
      
      // Carregar a arena padrão
      if (defaultArenaFile.exists()) {
        load(defaultArenaFile, null);
      }
      
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Erro ao criar arenas padrão: ", ex);
    }
  }

  public static void load(File yamlFile, LoadCallback callback) {
    String arenaName = yamlFile.getName().split("\\.")[0];

    try {
      // Verificar se o arquivo de configuração existe
      KConfig arenaConfig = Main.getInstance().getConfig("arenas", arenaName);
      if (!arenaConfig.contains("mode")) {
        LOGGER.warning("Arquivo de arena \"" + yamlFile.getName() + "\" não tem modo definido, pulando...");
        return;
      }

      MurderMode mode = MurderMode.fromName(arenaConfig.getString("mode"));
      if (mode == null) {
        LOGGER.warning("Modo do mapa \"" + yamlFile.getName() + "\" não é válido, pulando...");
        return;
      }

      // Verificar se é um mapa do AmongUS
      if (mode == MurderMode.IMPOSTOR_1 || mode == MurderMode.IMPOSTOR_2 || mode == MurderMode.IMPOSTOR_3) {
        // Usar o carregador específico para mapas do AmongUS
        AmongUSMapLoader.loadAmongUSMap(yamlFile, callback);
      } else {
        // Usar o carregador padrão para outros modos
        GAMES.put(arenaName, mode.buildGame(arenaName, callback));
      }
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "load(\"" + yamlFile.getName() + "\"): ", ex);
    }
  }

  public static Murder getByWorldName(String worldName) {
    return GAMES.get(worldName);
  }

  public static int getWaiting(MurderMode mode) {
    int waiting = 0;
    List<Murder> games = listByMode(mode);
    for (Murder game : games) {
      if (game.getState() != GameState.EMJOGO) {
        waiting += game.getOnline();
      }
    }

    return waiting;
  }

  public static int getPlaying(MurderMode mode) {
    int playing = 0;
    List<Murder> games = listByMode(mode);
    for (Murder game : games) {
      if (game.getState() == GameState.EMJOGO) {
        playing += game.getOnline();
      }
    }

    return playing;
  }

  public static Murder findRandom(MurderMode mode) {
    List<Murder> games = GAMES.values().stream().filter(
        game -> game.getMode().equals(mode) && game.getState().canJoin() && game.getOnline() < game
            .getMaxPlayers())
        .sorted((g1, g2) -> Integer.compare(g2.getOnline(), g1.getOnline()))
        .collect(Collectors.toList());
    Murder game = games.stream().findFirst().orElse(null);
    if (game != null && game.getOnline() == 0) {
      game = games.get(ThreadLocalRandom.current().nextInt(games.size()));
    }

    return game;
  }

  public static Map<String, List<Murder>> getAsMap(MurderMode mode) {
    Map<String, List<Murder>> result = new HashMap<>();
    GAMES.values().stream().filter(
        game -> game.getMode().equals(mode) && game.getState().canJoin() && game.getOnline() < game
            .getMaxPlayers()).forEach(game -> {
      List<Murder> list = result.computeIfAbsent(game.getMapName(), k -> new ArrayList<>());

      if (game.getState().canJoin() && game.getOnline() < game.getMaxPlayers()) {
        list.add(game);
      }
    });

    return result;
  }

  public static List<Murder> listByMode(MurderMode mode) {
    return GAMES.values().stream().filter(mm -> mm.getMode().equals(mode))
        .collect(Collectors.toList());
  }

  /**
   * Registra um jogo no sistema
   * @param name Nome do jogo
   * @param game Instância do jogo
   */
  public static void registerGame(String name, Murder game) {
    GAMES.put(name, game);
  }

  /**
   * Define as salas do mapa
   * @param rooms Lista de salas
   */
  public void setRooms(List<Room> rooms) {
    if (this.config != null) {
      this.config.setRooms(rooms);
    }
  }

  /**
   * Define as tarefas do mapa
   * @param tasks Lista de tarefas
   */
  public void setTasks(List<Task> tasks) {
    if (this.config != null) {
      this.config.setTasks(tasks);
    }
  }

  /**
   * Define as sabotagens do mapa
   * @param sabotages Lista de sabotagens
   */
  public void setSabotages(List<Sabotage> sabotages) {
    if (this.config != null) {
      this.config.setSabotages(sabotages);
    }
  }

  /**
   * Define o cooldown de kill
   * @param cooldown Cooldown em segundos
   */
  public void setKillCooldown(int cooldown) {
    if (this.config != null) {
      this.config.setKillCooldown(cooldown);
    }
  }

  /**
   * Define o tempo de reunião
   * @param time Tempo em segundos
   */
  public void setMeetingTime(int time) {
    if (this.config != null) {
      this.config.setMeetingTime(time);
    }
  }

  /**
   * Define o tempo de votação
   * @param time Tempo em segundos
   */
  public void setVotingTime(int time) {
    if (this.config != null) {
      this.config.setVotingTime(time);
    }
  }

  /**
   * Define o tempo das tarefas
   * @param time Tempo em segundos
   */
  public void setTaskTime(int time) {
    if (this.config != null) {
      this.config.setTaskTime(time);
    }
  }

  /**
   * Define a velocidade dos jogadores
   * @param speed Velocidade (1.0 = normal)
   */
  public void setPlayerSpeed(double speed) {
    if (this.config != null) {
      this.config.setPlayerSpeed(speed);
    }
  }

  /**
   * Define a distância de kill
   * @param distance Distância em blocos
   */
  public void setKillDistance(double distance) {
    if (this.config != null) {
      this.config.setKillDistance(distance);
    }
  }

  /**
   * Define o nome do mapa
   * @param mapName Nome do mapa
   */
  public void setMapName(String mapName) {
    if (this.config != null) {
      this.config.setMapName(mapName);
    }
  }

  /**
   * Define o número mínimo de jogadores
   * @param minPlayers Número mínimo de jogadores
   */
  public void setMinPlayers(int minPlayers) {
    if (this.config != null) {
      this.config.setMinPlayers(minPlayers);
    }
  }

  /**
   * Define o número máximo de jogadores
   * @param maxPlayers Número máximo de jogadores
   */
  public void setMaxPlayers(int maxPlayers) {
    if (this.config != null) {
      this.config.setMaxPlayers(maxPlayers);
    }
  }

  /**
   * Define o lobby de espera
   * @param location Localização do lobby
   */
  public void setWaitingLobby(Location location) {
    if (this.config != null) {
      this.config.setWaitingLobby(location);
    }
  }

  /**
   * Adiciona um spawn ao mapa
   * @param location Localização do spawn
   */
  public void addSpawn(Location location) {
    if (this.config != null) {
      this.config.addSpawn(location);
    }
  }

  /**
   * Obtém a lista de jogadores (alias para listPlayers)
   * @return Lista de jogadores
   */
  public List<Player> getPlayers() {
    return listPlayers(false);
  }

  /**
   * Verifica se todas as tarefas foram completadas
   * @return true se todas as tarefas foram completadas
   */
  public abstract boolean areAllTasksCompleted();

  /**
   * Implementação do método leave da interface Game
   * @param profile Perfil do jogador que saiu
   * @param game Jogo do qual o jogador saiu
   */
  @Override
  public void leave(Profile profile, Game<?> game) {
    // Implementação do método leave
    if (profile != null) {
      this.broadcastMessage("§e" + profile.getName() + " saiu do jogo!");
    }
  }

  /**
   * Destrói o jogo e limpa todos os recursos
   */
  public void destroy() {
    this.name = null;
    if (this.config != null) {
      this.config.destroy();
      this.config = null;
    }
    this.timer = 0;
    this.state = null;
    if (this.task != null) {
      this.task.cancel();
      this.task = null;
    }
    if (this.players != null) {
      this.players.clear();
      this.players = null;
    }
    if (this.spectators != null) {
      this.spectators.clear();
      this.spectators = null;
    }
  }

  /**
   * Verifica o estado do jogo
   */
  public void check() {
    if (this.state != GameState.EMJOGO) {
      return;
    }

    boolean impostorWin = !teams.get(1).isAlive();
    boolean crewmateWin = !teams.get(0).isAlive() || areAllTasksCompleted();
    
    if (impostorWin || crewmateWin) {
      // Corrigido para usar o método correto
      this.destroy();
    }
  }
}
