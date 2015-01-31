package ljfa.noitemrot;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;

public class NIRModContainer extends DummyModContainer {
    public NIRModContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "no_item_rotation";
        meta.name = "NoItemRotation";
        meta.version = "1.0-beta";
        meta.authorList = Arrays.asList("ljfa");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

}
