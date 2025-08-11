package hero.amongus.tagger;

import net.hero.services.player.role.Role;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe substituta para FakeManager que implementa apenas as funcionalidades necessárias
 * para o funcionamento do jogo sem dependências externas.
 */
public class FakeManager {

    public static Map<String, String> fakeNames = new HashMap<>();
    public static Map<String, Role> fakeRoles = new HashMap<>();
    public static Map<String, String> fakeSkins = new HashMap<>();
    
    /**
     * Verifica se um jogador está usando um nome falso
     * @param playerName Nome do jogador
     * @return true se o jogador está usando um nome falso
     */
    public static boolean isFake(String playerName) {
        return fakeNames.containsKey(playerName);
    }
    
    /**
     * Verifica se o sistema está rodando em modo BungeeCord
     * @return false, pois não estamos mais usando BungeeCord
     */
    public static boolean isBungeeSide() {
        return false;
    }
    
    /**
     * Limpa todos os dados de nomes falsos
     */
    public static void reset() {
        fakeNames.clear();
        fakeRoles.clear();
        fakeSkins.clear();
    }
}