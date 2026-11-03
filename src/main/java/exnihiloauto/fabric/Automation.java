package exnihiloauto.fabric;

import java.util.*;
import java.util.function.Function;
import exnihiloomnia.fabric.ExNihiloOmnia;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class Automation implements ModInitializer {
    public static final Map<String,Item> ITEMS=new LinkedHashMap<>();
    public static final Map<String,MachineBlock> MACHINES=new LinkedHashMap<>();
    public static BlockEntityType<MachineEntity> ENTITY;
    public static MenuType<MachineMenu> MENU;
    public static Identifier id(String path){return Identifier.fromNamespaceAndPath("exnihiloauto",path);}
    public void onInitialize(){
        MachineConfig.load();
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((level,player,pos,state,entity)->{
            var tool=player.getMainHandItem();
            if(player.isCreative()||!MachineEntity.reinforced(tool))return true;
            Block output=ExNihiloOmnia.hammerOutput(state.getBlock());
            if(output==null)return true;
            boolean compressed=MachineEntity.compressed(new ItemStack(state.getBlock()));
            if(level.removeBlock(pos,false)){
                Block.popResource(level,pos,new ItemStack(output));
                tool.hurtAndBreak(compressed?9:1,player,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            return false;
        });
        for(String name:List.of("speed_upgrade","bonus_upgrade","reinforced_upgrade")) {
            String description=switch(name){case "speed_upgrade"->"20% less processing time per upgrade (maximum 3).";case "bonus_upgrade"->"25% chance of double output per upgrade (maximum 3).";default->"Auto Hammer: enables compressed inputs.";};
            item(name,Item::new,new Item.Properties().stacksTo(1).component(net.minecraft.core.component.DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(List.of(Component.literal(description)))));
        }
        for(String tier:List.of("wood","stone","iron","gold","diamond","netherite")) {
            ToolMaterial material=switch(tier){case "wood"->ToolMaterial.WOOD;case "stone"->ToolMaterial.STONE;case "iron"->ToolMaterial.IRON;case "gold"->ToolMaterial.GOLD;case "diamond"->ToolMaterial.DIAMOND;default->ToolMaterial.NETHERITE;};
            var properties=new Item.Properties().pickaxe(material,1F,-2.8F).durability(material.durability()*4)
                .component(net.minecraft.core.component.DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("4x durability. Enables compressed Auto Hammer processing."))));
            if(tier.equals("netherite"))properties.fireResistant();
            item("reinforced_hammer_"+tier,Item::new,properties);
        }
        for(MachineKind kind:MachineKind.values()){
            String name=kind.id;
            var key=ResourceKey.create(Registries.BLOCK,id(name));
            var block=Registry.register(BuiltInRegistries.BLOCK,key,new MachineBlock(kind,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3F).setId(key)));
            MACHINES.put(name,block);
            item(name,p->new BlockItem(block,p),new Item.Properties().useBlockDescriptionPrefix());
        }
        ENTITY=Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,id("machine"),new BlockEntityType<>(MachineEntity::new,new HashSet<>(MACHINES.values())));
        MENU=Registry.register(BuiltInRegistries.MENU,id("machine"),new MenuType<>(MachineMenu::new,FeatureFlags.DEFAULT_FLAGS));
        ItemStorage.SIDED.registerForBlockEntity((e,side)->ContainerStorage.of(e,side),ENTITY);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,id("main"),FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.exnihiloauto")).icon(()->new ItemStack(ITEMS.get("auto_sieve")))
            .displayItems((p,out)->ITEMS.values().forEach(out::accept)).build());
    }
    private static void item(String name,Function<Item.Properties,Item> factory,Item.Properties properties){
        var key=ResourceKey.create(Registries.ITEM,id(name));
        ITEMS.put(name,Registry.register(BuiltInRegistries.ITEM,key,factory.apply(properties.setId(key))));
    }
    public static boolean is(ItemStack stack,String name){return stack.is(ITEMS.get(name));}
}
