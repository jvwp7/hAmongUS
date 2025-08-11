package hero.amongus.menus;

import hero.amongus.Main;
import hero.amongus.game.object.VentSystem;
import net.hero.services.libraries.menu.PlayerMenu;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * Menu de ventilação para o sistema Among Us
 * Permite que o jogador navegue entre ventilações conectadas
 */
public class MenuVent extends PlayerMenu {

    private final VentSystem ventSystem;
    private final VentSystem.Vent currentVent;
    private final BukkitTask timeoutTask;
    private final int timeoutSeconds;

    /**
     * Cria um novo menu de ventilação
     * @param profile Perfil do jogador
     * @param ventSystem Sistema de ventilação
     * @param currentVent Ventilação atual
     */
    public MenuVent(Profile profile, VentSystem ventSystem, VentSystem.Vent currentVent) {
        super(profile.getPlayer(), "Sistema de Ventilação", 3);
        this.ventSystem = ventSystem;
        this.currentVent = currentVent;
        this.timeoutSeconds = Main.getInstance().getConfig().getInt("amongus.vent_timeout", 10);

        // Configura o menu
        setupMenu();
        
        // Registra o timeout para sair da ventilação automaticamente
        this.timeoutTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(this.getInventory())) {
                exitVent();
            }
        }, timeoutSeconds * 20L); // 20 ticks = 1 segundo
        
        this.open();
    }

    /**
     * Configura os itens do menu
     */
    private void setupMenu() {
        // Obtém as ventilações conectadas
        List<VentSystem.Vent> connectedVents = ventSystem.getConnectedVents(currentVent.getId());
        
        // Preenche o fundo do menu com vidro preto
        for (int i = 0; i < this.getInventory().getSize(); i++) {
            this.setItem(i, BukkitUtils.deserializeItemStack("STAINED_GLASS_PANE:15 : 1 : nome>&8"));
        }
        
        // Informações da ventilação atual
        this.setItem(13, BukkitUtils.deserializeItemStack(
                "IRON_TRAPDOOR : 1 : nome>&aVentilação Atual : desc>&7Sala: &f" + 
                currentVent.getRoomName() + "\n&7ID: &f" + currentVent.getId() + 
                "\n\n&7Você tem &f" + timeoutSeconds + " &7segundos\n&7para escolher uma opção."));
        
        // Botão para sair da ventilação
        this.setItem(22, BukkitUtils.deserializeItemStack(
                "BARRIER : 1 : nome>&cSair da Ventilação : desc>&7Clique para sair da ventilação."));
        
        // Adiciona as setas para as ventilações conectadas
        if (connectedVents.size() >= 1) {
            // Seta para a esquerda (primeira ventilação conectada)
            this.setItem(11, BukkitUtils.deserializeItemStack(
                    "ARROW : 1 : nome>&aIr para Esquerda : desc>&7Sala: &f" + 
                    connectedVents.get(0).getRoomName() + "\n&7ID: &f" + 
                    connectedVents.get(0).getId()));
        }
        
        if (connectedVents.size() >= 2) {
            // Seta para a direita (segunda ventilação conectada)
            this.setItem(15, BukkitUtils.deserializeItemStack(
                    "ARROW : 1 : nome>&aIr para Direita : desc>&7Sala: &f" + 
                    connectedVents.get(1).getRoomName() + "\n&7ID: &f" + 
                    connectedVents.get(1).getId()));
        }
    }

    /**
     * Manipula os cliques no inventário
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getInventory().equals(this.getInventory())) {
            evt.setCancelled(true);

            if (evt.getWhoClicked().equals(this.player)) {
                Profile profile = Profile.getProfile(this.player.getName());
                if (profile == null) {
                    this.player.closeInventory();
                    return;
                }

                if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getInventory())) {
                    ItemStack item = evt.getCurrentItem();

                    if (item != null && item.getType() != Material.AIR) {
                        List<VentSystem.Vent> connectedVents = ventSystem.getConnectedVents(currentVent.getId());
                        
                        if (evt.getSlot() == 11 && connectedVents.size() >= 1) {
                            // Teleportar para a ventilação da esquerda
                            EnumSound.ENDERMAN_TELEPORT.play(this.player, 0.5F, 1.0F);
                            teleportToVent(connectedVents.get(0));
                        } else if (evt.getSlot() == 15 && connectedVents.size() >= 2) {
                            // Teleportar para a ventilação da direita
                            EnumSound.ENDERMAN_TELEPORT.play(this.player, 0.5F, 1.0F);
                            teleportToVent(connectedVents.get(1));
                        } else if (evt.getSlot() == 22) {
                            // Sair da ventilação
                            exitVent();
                        }
                    }
                }
            }
        }
    }

    /**
     * Teleporta o jogador para uma ventilação
     * @param vent Ventilação de destino
     */
    private void teleportToVent(VentSystem.Vent vent) {
        // Cancela o timeout atual
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        
        // Fecha o inventário atual
        this.player.closeInventory();
        
        // Atualiza o jogador na ventilação atual e na nova
        currentVent.setPlayerInside(null);
        vent.setPlayerInside(this.player);
        
        // Teleporta o jogador
        this.player.teleport(vent.getLocation());
        
        // Abre o novo menu de ventilação
        new MenuVent(Profile.getProfile(this.player.getName()), ventSystem, vent);
    }

    /**
     * Faz o jogador sair da ventilação
     */
    private void exitVent() {
        EnumSound.ENDERMAN_TELEPORT.play(this.player, 0.5F, 1.0F);
        this.player.closeInventory();
        currentVent.setPlayerInside(null);
        this.player.sendMessage("§aVocê saiu da ventilação.");
    }

    /**
     * Cancela o menu e suas tarefas
     */
    public void cancel() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        if (evt.getPlayer().equals(this.player)) {
            currentVent.setPlayerInside(null);
            this.cancel();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getInventory())) {
            this.cancel();
        }
    }
}