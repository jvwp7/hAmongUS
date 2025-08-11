package hero.amongus.container;

import net.hero.services.database.data.DataContainer;
import net.hero.services.database.data.interfaces.AbstractContainer;
import hero.amongus.cosmetics.Cosmetic;
import org.json.simple.JSONObject;
import hero.amongus.cosmetics.CosmeticType;

@SuppressWarnings("unchecked")
public class SelectedContainer extends AbstractContainer {

  public SelectedContainer(DataContainer dataContainer) {
    super(dataContainer);
    JSONObject cosmetics = this.dataContainer.getAsJsonObject();
    if (!cosmetics.containsKey("DEATH_MESSAGE")) {
      for (CosmeticType type : CosmeticType.values()) {
        cosmetics.put(type.name(), new JSONObject());
      }
    }

    this.dataContainer.set(cosmetics.toString());
    cosmetics.clear();
  }

  public void setSelected(Cosmetic cosmetic) {
    this.setSelected(cosmetic.getType(), cosmetic.getId());
  }

  public void setSelected(CosmeticType type, long id) {
    this.setSelected(type, id, 1);
  }

  public void setSelected(CosmeticType type, long id, long index) {
    JSONObject cosmetics = this.dataContainer.getAsJsonObject();
    ((JSONObject) cosmetics.get(type.name())).put(String.valueOf(index), id);
    this.dataContainer.set(cosmetics.toString());
    cosmetics.clear();
  }

  public boolean isSelected(Cosmetic cosmetic) {
    JSONObject cosmetics = this.dataContainer.getAsJsonObject();
    JSONObject selected = (JSONObject) cosmetics.get(cosmetic.getType().name());
    if (!selected.containsKey(String.valueOf(cosmetic.getIndex()))) {
      selected.put(String.valueOf(cosmetic.getIndex()), 0L);
      this.dataContainer.set(cosmetics.toString());
    }
    boolean isSelected = selected.get(String.valueOf(cosmetic.getIndex())).equals(cosmetic.getId());
    selected.clear();
    cosmetics.clear();
    return isSelected;
  }

  public <T extends Cosmetic> T getSelected(CosmeticType type, Class<T> cosmeticClass) {
    return this.getSelected(type, cosmeticClass, 1);
  }

  public <T extends Cosmetic> T getSelected(CosmeticType type, Class<T> cosmeticClass, long index) {
    JSONObject cosmetics = this.dataContainer.getAsJsonObject();
    JSONObject selected = (JSONObject) cosmetics.get(type.name());
    if (!selected.containsKey(String.valueOf(index))) {
      selected.put(String.valueOf(index), 0L);
      this.dataContainer.set(cosmetics.toString());
    }
    T cosmetic = Cosmetic.findById(cosmeticClass, (long) selected.get(String.valueOf(index)));
    selected.clear();
    cosmetics.clear();
    return cosmetic;
  }
}
