package hero.amongus.listeners.player;

import hero.amongus.Main;
import hero.amongus.game.Murder;
import hero.amongus.game.object.VentSystem;
import hero.amongus.menus.MenuVent;
import net.hero.services.player.Profile;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener para interação do jogador com ventilações
 */
public class VentInteractListener implements Listener {

    /**
     * Manipula o evento de interação do jogador com blocos
     * Detecta quando um jogador clica em uma ventilação
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getProfile(player.getName());
        
        if (profile == null) {
            return;
        }
        
        // Verifica se o jogador está em um jogo
        Murder game = profile.getGame(Murder.class);
        if (game == null) {
            return;
        }
        
        // Verifica se o jogo tem um sistema de ventilação
        VentSystem ventSystem = game.getVentSystem();
        if (ventSystem == null) {
            return;
        }
        
        // Verifica se o jogador clicou com o botão direito em um bloco
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            // Verifica se o bloco clicado é uma ventilação (iron trapdoor)
            if (event.getClickedBlock().getType() == Material.IRON_TRAPDOOR) {
                // Tenta encontrar uma ventilação próxima
                VentSystem.Vent vent = ventSystem.getVentByLocation(event.getClickedBlock().getLocation(), 1.0);
                
                if (vent != null) {
                    event.setCancelled(true);
                    
                    // Verifica se a ventilação já está ocupada
                    if (vent.isOccupied() && !player.equals(vent.getPlayerInside())) {
                        player.sendMessage("§cEsta ventilação já está sendo usada por outro jogador.");
                        return;
                    }
                    
                    // Define o jogador como estando dentro da ventilação
                    vent.setPlayerInside(player);
                    
                    // Toca som de teleporte
                    EnumSound.ENDERMAN_TELEPORT.play(player, 0.5F, 1.0F);
                    
                    // Abre o menu de ventilação
                    new MenuVent(profile, ventSystem, vent);
                }
            }
        }
    }
}