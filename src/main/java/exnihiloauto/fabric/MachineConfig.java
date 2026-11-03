package exnihiloauto.fabric;

import java.nio.file.*;
import java.io.*;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class MachineConfig {
    public static int sieveTicks=100,hammerTicks=100,silkerTicks=25,energyPerTick=1,capacity=10000,fuelEnergyPerTick=10;
    public static void load(){
        Path path=FabricLoader.getInstance().getConfigDir().resolve("exnihiloauto.properties");
        Properties p=new Properties();
        try {
            if(Files.exists(path))try(var in=Files.newInputStream(path)){p.load(in);}
            sieveTicks=value(p,"sieve_ticks",100,1,120000);
            hammerTicks=value(p,"hammer_ticks",100,1,120000);
            silkerTicks=value(p,"silker_ticks",25,1,120000);
            energyPerTick=value(p,"energy_per_tick",1,1,1000);
            capacity=value(p,"energy_capacity",10000,1000,10000000);
            fuelEnergyPerTick=value(p,"fuel_energy_per_burn_tick",10,1,1000);
            Files.createDirectories(path.getParent());
            try(var out=Files.newOutputStream(path)){p.store(out,"Ex Nihilo Automation - restart the server after editing. Speed upgrades reduce work time by 20% each (maximum three). Coal provides 1600 burn ticks.");}
        } catch(IOException e){LoggerFactory.getLogger("exnihiloauto").error("Could not read/write machine configuration; using defaults",e);}
    }
    private static int value(Properties p,String key,int fallback,int min,int max){
        int v=fallback;try{v=Integer.parseInt(p.getProperty(key,""));}catch(NumberFormatException ignored){}
        v=Math.clamp(v,min,max);p.setProperty(key,Integer.toString(v));return v;
    }
}
