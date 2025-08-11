package hero.amongus.game.object;

import net.hero.services.plugin.config.KConfig;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.World;
import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.enums.MurderMode;
import hero.amongus.game.interfaces.LoadCallback;
import hero.amongus.game.types.Impostor1Murder;
import hero.amongus.game.types.Impostor2Murder;
import hero.amongus.game.types.Impostor3Murder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe responsável por carregar mapas do AmongUS a partir dos arquivos de configuração
 */
public class AmongUSMapLoader {

    private static final Logger LOGGER = Main.getInstance().getLogger();

    /**
     * Carrega um mapa do AmongUS a partir do arquivo de configuração
     * @param yamlFile Arquivo de configuração
     * @param callback Callback de carregamento
     */
    public static void loadAmongUSMap(File yamlFile, LoadCallback callback) {
        String arenaName = yamlFile.getName().split("\\.")[0];

        try {
            KConfig config = Main.getInstance().getConfig("arenas", arenaName);
            
            // Verificar se a configuração tem os campos necessários
            if (!config.contains("mode")) {
                Main.getInstance().getLogger().warning("Arquivo de arena \"" + yamlFile.getName() + "\" não tem modo definido, pulando...");
                return;
            }
            
            MurderMode mode = MurderMode.fromName(config.getString("mode"));
            if (mode == null) {
                Main.getInstance().getLogger().warning("Modo do mapa \"" + yamlFile.getName() + "\" não é válido, pulando...");
                return;
            }

            // Criar a instância do jogo com base no modo
            Murder game;
            switch (mode) {
                case IMPOSTOR_1:
                    game = new Impostor1Murder(arenaName, null);
                    break;
                case IMPOSTOR_2:
                    game = new Impostor2Murder(arenaName, null);
                    break;
                case IMPOSTOR_3:
                    game = new Impostor3Murder(arenaName, null);
                    break;
                default:
                    Main.getInstance().getLogger().warning("Modo de jogo não suportado: " + mode.name() + ", pulando...");
                    return;
            }

            // Configurar o jogo com base no arquivo de configuração
            configureGame(game, config);

            // Registrar o jogo
            Murder.registerGame(arenaName, game);

            if (callback != null) {
                callback.finish();
            }

        } catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.WARNING, "loadAmongUSMap(\"" + yamlFile.getName() + "\"): ", ex);
        }
    }

    /**
     * Configura um jogo com base no arquivo de configuração
     * @param game Instância do jogo
     * @param config Configuração do mapa
     */
    private static void configureGame(Murder game, KConfig config) {
        // Configurações básicas
        game.setMapName(config.getString("name"));
        game.setMinPlayers(config.getInt("minPlayers"));
        game.setMaxPlayers(config.getInt("maxPlayers", game.getMode().getSize()));

        // Configurar spawns
        String waitingSpawn = config.getString("spawn");
        if (waitingSpawn != null) {
            game.setWaitingLobby(BukkitUtils.deserializeLocation(waitingSpawn));
        }

        List<String> spawns = config.getStringList("spawns");
        for (String spawn : spawns) {
            game.addSpawn(BukkitUtils.deserializeLocation(spawn));
        }

        // Configurar salas
        List<String> roomsData = config.getStringList("rooms");
        List<hero.amongus.game.object.Room> rooms = new ArrayList<>();
        for (String roomData : roomsData) {
            String[] parts = roomData.split(";");
            if (parts.length >= 3) {
                String name = parts[0];
                Location center = BukkitUtils.deserializeLocation(parts[1]);
                boolean hasEmergencyButton = Boolean.parseBoolean(parts[2]);
                
                hero.amongus.game.object.Room room = new hero.amongus.game.object.Room(name, center);
                room.setHasEmergencyButton(hasEmergencyButton);
                rooms.add(room);
            }
        }
        game.setRooms(rooms);

        // Configurar tarefas
        List<String> tasksData = config.getStringList("tasks");
        List<hero.amongus.game.object.Task> tasks = new ArrayList<>();
        for (String taskData : tasksData) {
            String[] parts = taskData.split(";");
            if (parts.length >= 3) {
                hero.amongus.game.object.Task.TaskType type = Task.TaskType.valueOf(parts[0]);
                String name = parts[1];
                Location location = BukkitUtils.deserializeLocation(parts[2]);
                
                hero.amongus.game.object.Task task = new Task(name, "", location, type);
                tasks.add(task);
            }
        }
        game.setTasks(tasks);

        // Configurar ventilações
        List<String> ventsData = config.getStringList("vents");
        VentSystem ventSystem = new VentSystem();
        for (String ventData : ventsData) {
            String[] parts = ventData.split(";");
            if (parts.length >= 3) {
                String id = parts[0];
                Location location = BukkitUtils.deserializeLocation(parts[1]);
                String roomName = parts[2];
                
                ventSystem.addVent(id, location, roomName);
            }
        }
        
        // Conectar ventilações
        for (String ventData : ventsData) {
            String[] parts = ventData.split(";");
            if (parts.length > 3) {
                String id = parts[0];
                
                for (int i = 3; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        ventSystem.connectVents(id, parts[i]);
                    }
                }
            }
        }
        game.setVentSystem(ventSystem);

        // Configurar sabotagens
        List<String> sabotagesData = config.getStringList("sabotages");
        List<Sabotage> sabotages = new ArrayList<>();
        for (String sabotageData : sabotagesData) {
            String[] parts = sabotageData.split(";");
            if (parts.length >= 3) {
                Sabotage.SabotageType type = Sabotage.SabotageType.valueOf(parts[0]);
                Location location = BukkitUtils.deserializeLocation(parts[1]);
                String roomName = parts[2];
                
                Sabotage sabotage = new Sabotage(game, type, location);
                sabotages.add(sabotage);
            }
        }
        game.setSabotages(sabotages);

        // Configurações específicas do AmongUS
        game.setKillCooldown(config.getInt("kill_cooldown", 30));
        game.setMeetingTime(config.getInt("meeting_time", 15));
        game.setVotingTime(config.getInt("voting_time", 120));
        game.setTaskTime(config.getInt("task_time", 5));
        game.setPlayerSpeed(config.getDouble("player_speed", 1.0));
        game.setKillDistance(config.getDouble("kill_distance", 1.5));
    }
}