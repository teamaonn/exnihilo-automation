package exnihiloauto.fabric;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
public final class AutomationClient implements ClientModInitializer {
    public void onInitializeClient(){MenuScreens.register(Automation.MENU,MachineScreen::new);}
}
