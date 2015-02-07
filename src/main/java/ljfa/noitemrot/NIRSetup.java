package ljfa.noitemrot;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLCallHook;

public class NIRSetup implements IFMLCallHook {
    @Override
    public Void call() throws Exception {
        FMLLog.log("NoItemRotation", Level.INFO, "Loading configuration");
        Config.loadConfig(new File("config/no_item_rotation.cfg"));
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }
}
