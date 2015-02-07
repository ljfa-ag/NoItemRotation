package ljfa.noitemrot;

import java.util.Arrays;
import java.util.Map;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@Name("NoItemRotation")
@MCVersion("1.7.10")
@SortingIndex(1001)
public class NIRLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"ljfa.noitemrot.RenderItemTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return "ljfa.noitemrot.NIRModContainer";
    }

    @Override
    public String getSetupClass() {
        return "ljfa.noitemrot.NIRSetup";
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
