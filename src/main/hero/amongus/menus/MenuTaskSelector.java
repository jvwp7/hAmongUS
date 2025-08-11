package hero.amongus.menus;

import net.hero.services.libraries.menu.UpdatablePlayerPagedMenu;
import hero.amongus.Main;
import hero.amongus.game.object.Task;
import net.hero.services.player.Profile;
import net.hero.services.utils.BukkitUtils;
import net.hero.services.utils.enums.EnumSound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuTaskSelector extends UpdatablePlayerPagedMenu {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getCurrentInventory())) {
      evt.setCancelled(true);

      if (evt.getWhoClicked().equals(this.player)) {
        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getCurrentInventory())) {
          ItemStack item = evt.getCurrentItem();

          if (item != null && item.getType() != Material.AIR) {
            if (evt.getSlot() == this.previousPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openPrevious();
            } else if (evt.getSlot() == this.nextPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openNext();
            } else if (evt.getSlot() == 49) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.player.closeInventory();
            } else {
              // Adicionar tarefa selecionada
              String taskKey = this.tasks.get(item);
              if (taskKey != null) {
                EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
                this.addTask(taskKey);
              }
            }
          }
        }
      }
    }
  }

  private Profile profile;
  private Location location;
  private Map<ItemStack, String> tasks = new HashMap<>();

  public MenuTaskSelector(Profile profile, Location location) {
    super(profile.getPlayer(), "Selecionar Tarefa", 6);
    this.profile = profile;
    this.location = location;
    this.previousPage = 19;
    this.nextPage = 26;
    this.onlySlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    this.removeSlotsWith(BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cFechar"), 49);

    this.update();
    this.register(Main.getInstance(), 20);
    this.open();
  }

  @Override
  public void update() {
    List<ItemStack> items = new ArrayList<>();
    this.tasks.clear();

    // Tarefas comuns
    addTaskItem(items, "common_card_access", "Cartão de Acesso", "Passe o cartão no leitor", Material.PAPER, Task.TaskType.COMMON);
    addTaskItem(items, "common_wiring", "Fiação", "Conserte a fiação", Material.REDSTONE, Task.TaskType.COMMON);
    addTaskItem(items, "common_download_data", "Baixar Dados", "Baixe os dados do terminal", Material.BOOK, Task.TaskType.COMMON);

    // Tarefas curtas
    addTaskItem(items, "short_calibrate_distributor", "Calibrar Distribuidor", "Calibre o distribuidor de energia", Material.REDSTONE_TORCH_ON, Task.TaskType.SHORT);
    addTaskItem(items, "short_divert_power", "Desviar Energia", "Desvie a energia para outro setor", Material.LEVER, Task.TaskType.SHORT);
    addTaskItem(items, "short_stabilize_steering", "Estabilizar Direção", "Estabilize a direção da nave", Material.COMPASS, Task.TaskType.SHORT);
    addTaskItem(items, "short_clean_o2_filter", "Limpar Filtro O2", "Limpe o filtro de oxigênio", Material.GLASS, Task.TaskType.SHORT);
    addTaskItem(items, "short_empty_garbage", "Esvaziar Lixo", "Esvazie o lixo", Material.HOPPER, Task.TaskType.SHORT);

    // Tarefas longas
    addTaskItem(items, "long_start_reactor", "Iniciar Reator", "Inicie o reator da nave", Material.REDSTONE_BLOCK, Task.TaskType.LONG);
    addTaskItem(items, "long_align_engine", "Alinhar Motor", "Alinhe o motor da nave", Material.PISTON_BASE, Task.TaskType.LONG);
    addTaskItem(items, "long_inspect_sample", "Inspecionar Amostra", "Inspecione a amostra no laboratório", Material.GLASS_BOTTLE, Task.TaskType.LONG);

    // Tarefas visuais
    addTaskItem(items, "visual_medical_scan", "Scan Médico", "Faça um scan médico", Material.BEACON, Task.TaskType.VISUAL);
    addTaskItem(items, "visual_empty_chute", "Esvaziar Lixeira", "Esvazie a lixeira", Material.HOPPER, Task.TaskType.VISUAL);
    addTaskItem(items, "visual_clear_asteroids", "Limpar Asteroides", "Destrua os asteroides", Material.BOW, Task.TaskType.VISUAL);

    this.setItems(items);
  }

  private void addTaskItem(List<ItemStack> items, String key, String name, String description, Material material, Task.TaskType type) {
    String typeColor = getTypeColor(type);
    String typeName = getTypeName(type);
    
    ItemStack item = BukkitUtils.deserializeItemStack(
      material.name() + " : 1 : nome>&b" + name + " : desc>&7" + description + "\n&7Tipo: " + typeColor + typeName + "\n \n&eClique para adicionar!");
    
    items.add(item);
    this.tasks.put(item, key + ";" + name + ";" + type.name());
  }

  private String getTypeColor(Task.TaskType type) {
    switch (type) {
      case COMMON: return "&a";
      case SHORT: return "&e";
      case LONG: return "&c";
      case VISUAL: return "&b";
      default: return "&7";
    }
  }

  private String getTypeName(Task.TaskType type) {
    switch (type) {
      case COMMON: return "Comum";
      case SHORT: return "Curta";
      case LONG: return "Longa";
      case VISUAL: return "Visual";
      default: return "Desconhecido";
    }
  }

  private void addTask(String taskData) {
    String[] parts = taskData.split(";");
    if (parts.length >= 3) {
      String taskName = parts[1];
      String taskType = parts[2];
      
      String serializedTask = taskType + ";" + taskName + ";" + BukkitUtils.serializeLocation(this.location);
      
      @SuppressWarnings("unchecked")
      List<String> tasks = (List<String>) hero.amongus.cmd.mm.AmongUSCreateCommand.CREATING.get(this.player)[5];
      tasks.add(serializedTask);
      
      this.player.sendMessage("§aTarefa '" + taskName + "' adicionada com sucesso!");
      this.player.closeInventory();
    }
  }

  public void cancel() {
    super.cancel();
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(this.player)) {
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
