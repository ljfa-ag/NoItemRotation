package ljfa.noitemrot;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class NIRModContainer extends DummyModContainer {
    public NIRModContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "no_item_rotation";
        meta.name = "NoItemRotation";
        meta.version = "1.1-beta";
        meta.authorList = Arrays.asList("ljfa");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

}
