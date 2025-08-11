package hero.amongus.lobby.trait;

import net.hero.services.libraries.npclib.api.npc.NPC;
import net.hero.services.libraries.npclib.npc.skin.Skin;
import net.hero.services.libraries.npclib.npc.skin.SkinnableEntity;
import net.hero.services.libraries.npclib.trait.NPCTrait;

public class NPCSkinTrait extends NPCTrait {

  private Skin skin;

  public NPCSkinTrait(NPC npc, String value, String signature) {
    super(npc);
    this.skin = Skin.fromData(value, signature);
  }

  @Override
  public void onSpawn() {
    this.skin.apply((SkinnableEntity) this.getNPC().getEntity());
  }
}
