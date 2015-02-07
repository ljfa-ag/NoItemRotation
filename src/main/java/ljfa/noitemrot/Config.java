package ljfa.noitemrot;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
    public static Configuration conf;
    
    public static final String CATEGORY_GENERAL = "general";
    
    public static boolean disableRotation;
    public static boolean disableBobbing;
    public static boolean rotateBackwards;

    public static void loadConfig(File file) {
        if(conf == null)
            conf = new Configuration(file);
        
        conf.load();
        loadValues();
    }
    
    public static void loadValues() {
        disableRotation = conf.get(CATEGORY_GENERAL, "disableRotation", true, "Disables items rotating around their axis").getBoolean();
        disableBobbing = conf.get(CATEGORY_GENERAL, "disableBobbing", false, "Disables items bobbing up and down").getBoolean();
        rotateBackwards = conf.get(CATEGORY_GENERAL, "rotateBackwards", false, "Makes items rotate clockwise rather than counter-clockwise").getBoolean();
        //----------------
        if(conf.hasChanged())
            conf.save();
    }
}
