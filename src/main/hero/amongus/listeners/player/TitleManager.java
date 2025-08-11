package hero.amongus.listeners.player;

import hero.amongus.Language;
import net.hero.services.nms.NMS;
import net.hero.services.player.Profile;
import org.bukkit.entity.Player;

/**
 * Gerencia os títulos exibidos para os jogadores
 */
public class TitleManager {

    /**
     * Exibe o título de boas-vindas ao lobby para um jogador
     * 
     * @param profile Perfil do jogador
     */
    public static void joinLobby(Profile profile) {
        Player player = profile.getPlayer();
        if (player != null) {
            NMS.sendTitle(player, "§6§lAMONG US", "§fBem-vindo ao lobby!", 10, 60, 10);
        }
    }
}