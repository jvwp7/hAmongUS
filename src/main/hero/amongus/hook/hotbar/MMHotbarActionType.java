package hero.amongus.hook.hotbar;

import hero.amongus.game.Murder;
import hero.amongus.menus.MenuLobbies;
import hero.amongus.menus.MenuPlay;
import hero.amongus.menus.MenuShop;
import net.hero.services.player.Profile;
import net.hero.services.player.hotbar.HotbarActionType;

public class MMHotbarActionType extends HotbarActionType {

  @Override
  public void execute(Profile profile, String action) {
    if (action.equalsIgnoreCase("loja")) {
      new MenuShop(profile);
    } else if (action.equalsIgnoreCase("lobbies")) {
      new MenuLobbies(profile);
    } else if (action.equalsIgnoreCase("jogar")) {
      new MenuPlay(profile, profile.getGame(Murder.class).getMode());
    } else if (action.equalsIgnoreCase("sair")) {
      profile.getGame(Murder.class).leave(profile, null);
    }
  }
}
