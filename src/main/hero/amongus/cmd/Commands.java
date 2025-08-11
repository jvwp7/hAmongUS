package hero.amongus.cmd;

import hero.amongus.Main;
import hero.amongus.cmd.MurderCommand;
import hero.amongus.cmd.SpectateCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class Commands implements CommandExecutor, TabCompleter {
  
  protected final String name;
  protected final List<String> aliases;
  
  public Commands(String name, String... aliases) {
    this.name = name;
    this.aliases = Arrays.asList(aliases);
  }
  
  public abstract void perform(CommandSender sender, String label, String[] args);
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    this.perform(sender, label, args);
    return true;
  }
  
  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return null; // Subclasses podem sobrescrever se necessário
  }
  
  public static void setupCommands() {
    // Registrar comandos usando o sistema padrão do Bukkit
    MurderCommand murderCommand = new MurderCommand();
    SpectateCommand spectateCommand = new SpectateCommand();
    
    // Registrar o comando principal /am
    Main.getInstance().getCommand("am").setExecutor(murderCommand);
    Main.getInstance().getCommand("am").setTabCompleter(murderCommand);
    
    // Registrar o comando /assistir
    Main.getInstance().getCommand("assistir").setExecutor(spectateCommand);
    Main.getInstance().getCommand("assistir").setTabCompleter(spectateCommand);
    
    Main.getInstance().getLogger().info("Comandos registrados com sucesso!");
  }
}
