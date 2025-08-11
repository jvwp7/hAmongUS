package hero.amongus.cmd.mm;

import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import hero.amongus.cmd.SubCommand;

import java.util.List;

/**
 * Comando para adicionar botões de emergência durante a criação de um mapa
 */
public class EmergencyButtonCommand extends SubCommand {

  public EmergencyButtonCommand() {
    super("emergencia", "emergencia", "Adicionar um botão de emergência na sala atual.", true);
  }

  @Override
  public void perform(Player player, String[] args) {
    if (!AmongUSCreateCommand.CREATING.containsKey(player)) {
      player.sendMessage("§cVocê não está criando um mapa. Use /am criar primeiro.");
      return;
    }

    Location playerLocation = player.getLocation();
    
    @SuppressWarnings("unchecked")
    List<String> rooms = (List<String>) AmongUSCreateCommand.CREATING.get(player)[6];
    
    // Verificar se a lista de salas existe e não está vazia
    if (rooms == null || rooms.isEmpty()) {
      player.sendMessage("§cNenhuma sala foi criada ainda. Use /am sala [nome] primeiro.");
      return;
    }
    
    // Encontrar a sala mais próxima
    String closestRoom = null;
    double closestDistance = Double.MAX_VALUE;
    int closestIndex = -1;
    
    for (int i = 0; i < rooms.size(); i++) {
      String roomData = rooms.get(i);
      String[] parts = roomData.split(";");
      if (parts.length < 2) {
        continue; // Pular salas malformadas
      }
      String roomName = parts[0];
      String locationString = parts[1];
      
      // Verificar se a string de localização não está vazia
      if (locationString == null || locationString.trim().isEmpty()) {
        player.sendMessage("§cErro: localização da sala '" + roomName + "' está vazia.");
        continue;
      }
      
      try {
        Location roomLocation = BukkitUtils.deserializeLocation(locationString);
        if (roomLocation != null && roomLocation.getWorld() != null) {
          double distance = playerLocation.distance(roomLocation);
          if (distance < closestDistance) {
            closestDistance = distance;
            closestRoom = roomName;
            closestIndex = i;
          }
        } else {
          player.sendMessage("§cErro: localização da sala '" + roomName + "' é inválida.");
          continue;
        }
      } catch (Exception e) {
        player.sendMessage("§cErro ao processar sala '" + roomName + "': " + e.getMessage());
        player.sendMessage("§cString de localização: " + locationString);
        continue;
      }
    }
    
    if (closestRoom == null || closestDistance > 10) {
      player.sendMessage("§cNenhuma sala encontrada próxima a você. Adicione uma sala primeiro com /am sala.");
      return;
    }
    
    // Atualizar a sala para ter um botão de emergência
    String roomData = rooms.get(closestIndex);
    String[] parts = roomData.split(";");
    
    // Verificar se a sala tem pelo menos 2 partes (nome e localização)
    if (parts.length < 2) {
      player.sendMessage("§cErro: dados da sala corrompidos. Recrie a sala.");
      return;
    }
    
    // Se já tem 3 partes, verificar se já tem botão de emergência
    if (parts.length >= 3 && "true".equals(parts[2])) {
      player.sendMessage("§cEsta sala já tem um botão de emergência!");
      return;
    }
    
    String updatedRoomData = parts[0] + ";" + parts[1] + ";true";
    rooms.set(closestIndex, updatedRoomData);
    
    player.sendMessage("§aBotão de emergência adicionado na sala '" + closestRoom + "'!");
  }
}