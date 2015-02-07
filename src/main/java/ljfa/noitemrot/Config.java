package ljfa.noitemrot;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
    public static Configuration conf;
    
    public static final String CATEGORY_GENERAL = "general";
    
    public static boolean 

    public static void loadConfig(File file) {
        if(conf == null)
            conf = new Configuration(file);
        
        conf.load();
        loadValues();
    }
    
    public static void loadValues() {
        
        //----------------
        if(conf.hasChanged())
            conf.save();
    }
}
