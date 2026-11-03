package exnihiloauto.fabric;

import java.util.*;
import exnihiloomnia.fabric.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public final class AutomationTests {
    private static void check(boolean yes,String message){if(!yes)throw new IllegalStateException(message);System.out.println("AUTOMATION PASS: "+message);}
    private static MachineEntity machine(GameTestHelper h,String name){
        BlockPos p=h.absolutePos(new BlockPos(1,2,1));h.getLevel().setBlock(p,Automation.MACHINES.get(name).defaultBlockState(),3);
        return (MachineEntity)h.getLevel().getBlockEntity(p);
    }
    private static ItemStack omnia(String name){return new ItemStack(ExNihiloOmnia.ITEMS.get(name));}
    private static Map<Item,Integer> outputs(MachineEntity e){Map<Item,Integer> counts=new HashMap<>();for(int i=6;i<18;i++){var stack=e.getItem(i);if(!stack.isEmpty())counts.merge(stack.getItem(),stack.getCount(),Integer::sum);}return counts;}
    private static void run(MachineEntity e,int n){for(int i=0;i<n;i++)e.tick();}
    private static void fuel(MachineEntity e){e.setItem(2,new ItemStack(Items.COAL));}
    @GameTest public void compressedSieveUsesLiveIndependentTable(GameTestHelper h){
        var e=machine(h,"auto_sieve");e.setItem(0,omnia("compressed_dirt"));e.setItem(1,omnia("mesh_diamond"));fuel(e);
        List<ItemStack> expected=new ArrayList<>();SieveDrops.roll(1,4,9,RandomSource.create(9123),expected::add);
        Map<Item,Integer> counts=new HashMap<>();for(var s:expected)counts.merge(s.getItem(),s.getCount(),Integer::sum);
        h.getLevel().getRandom().setSeed(9123);run(e,e.duration());
        check(e.getItem(0).isEmpty(),"one compressed input consumed");
        // Dirt has more distinct results than twelve slots; drain buffer while preserving counts.
        Map<Item,Integer> actual=new HashMap<>(outputs(e));for(int i=6;i<18;i++)e.removeItemNoUpdate(i);
        while(e.pendingCount()>0){e.tick();outputs(e).forEach((k,v)->actual.merge(k,v,Integer::sum));for(int i=6;i<18;i++)e.removeItemNoUpdate(i);}
        check(actual.equals(counts),"compressed sieve matches exactly nine independent live Omnia rolls");
        check(e.getItem(1).getCount()==1,"mesh retained");h.succeed();
    }
    @GameTest public void hammerNeedsReinforcementAndWearsTool(GameTestHelper h){
        var e=machine(h,"auto_hammer");e.setItem(0,omnia("compressed_cobblestone"));e.setItem(1,omnia("hammer_iron"));fuel(e);
        run(e,120);check(e.getItem(0).getCount()==1&&e.energy()==0,"compressed hammer pauses before consuming fuel without reinforcement");
        e.setItem(3,new ItemStack(Automation.ITEMS.get("reinforced_upgrade")));run(e,e.duration());
        check(e.getItem(6).is(ExNihiloOmnia.ITEMS.get("compressed_gravel")),"compressed hammer follows Omnia chain");
        check(e.getItem(1).getDamageValue()==9,"compressed hammer uses nine durability");h.succeed();
    }
    @GameTest public void reinforcedHammerWorksWithoutUpgrade(GameTestHelper h){
        var e=machine(h,"auto_hammer");e.setItem(0,omnia("compressed_sand"));e.setItem(1,new ItemStack(Automation.ITEMS.get("reinforced_hammer_diamond")));fuel(e);run(e,e.duration());
        check(e.getItem(6).is(ExNihiloOmnia.ITEMS.get("compressed_dust")),"reinforced hammer processes compressed sand");h.succeed();
    }
    @GameTest public void silkerConsumesOneWormForFourLeaves(GameTestHelper h){
        var e=machine(h,"auto_silker");e.setItem(0,new ItemStack(Items.CHERRY_LEAVES,5));e.setItem(1,omnia("silkworm"));fuel(e);run(e,e.duration()*5);
        check(e.getItem(6).is(Items.STRING)&&e.getItem(6).getCount()==4,"one worm processes exactly four leaves");
        check(e.getItem(0).getCount()==1&&e.getItem(1).isEmpty()&&e.wormCharges()==0,"silker waits for next worm");h.succeed();
    }
    @GameTest public void progressEnergyAndChargesSurviveSave(GameTestHelper h){
        var e=machine(h,"auto_silker");e.setItem(0,new ItemStack(Items.OAK_LEAVES,8));e.setItem(1,omnia("silkworm"));fuel(e);run(e,e.duration()+7);
        var tag=e.saveWithFullMetadata(h.getLevel().registryAccess());
        var copy=(MachineEntity)BlockEntity.loadStatic(e.getBlockPos(),e.getBlockState(),tag,h.getLevel().registryAccess());copy.setLevel(h.getLevel());
        check(copy.progress()==e.progress()&&copy.energy()==e.energy()&&copy.wormCharges()==3,"progress, energy and worm uses survive serialization");
        check(outputs(copy).equals(outputs(e)),"outputs survive serialization");run(copy,copy.duration()-copy.progress());
        check(copy.getItem(6).getCount()==2&&copy.wormCharges()==2,"restored silker completes without extra worm");h.succeed();
    }
    @GameTest public void fullOutputAndRedstonePreserveInput(GameTestHelper h){
        var e=machine(h,"auto_hammer");e.setItem(0,new ItemStack(Items.COBBLESTONE,2));e.setItem(1,omnia("hammer_iron"));fuel(e);
        for(int i=6;i<18;i++)e.setItem(i,new ItemStack(Items.DIRT,64));run(e,150);
        check(e.getItem(0).getCount()==2&&e.energy()==0,"full output consumes neither input nor fuel");
        for(int i=6;i<18;i++)e.removeItemNoUpdate(i);
        h.getLevel().setBlock(e.getBlockPos().east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);run(e,120);
        check(e.getItem(0).getCount()==2,"redstone signal pauses machine");
        h.getLevel().removeBlock(e.getBlockPos().east(),false);run(e,e.duration());
        check(e.getItem(0).getCount()==1&&e.getItem(6).is(Items.GRAVEL),"machine resumes after redstone removed");h.succeed();
    }
    @GameTest public void sidedInventoryAndShiftClick(GameTestHelper h){
        var e=machine(h,"auto_sieve");
        check(e.canPlaceItemThroughFace(0,new ItemStack(Items.SAND),Direction.UP),"top accepts sieve input");
        check(!e.canTakeItemThroughFace(1,omnia("mesh_diamond"),Direction.DOWN),"hopper cannot steal mesh");
        check(!e.canPlaceItem(6,new ItemStack(Items.DIRT)),"output slots reject manual insertion");
        check(e.canPlaceItemThroughFace(2,new ItemStack(Items.COAL),Direction.WEST),"side accepts fuel");
        check(!e.canPlaceItemThroughFace(0,new ItemStack(Items.DIAMOND),Direction.UP),"invalid input rejected");
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.getInventory().setItem(9,new ItemStack(Items.SAND,8));
        var menu=new MachineMenu(1,player.getInventory(),e,e.data);menu.quickMoveStack(player,18);
        check(e.getItem(0).getCount()==8&&player.getInventory().getItem(9).isEmpty(),"shift-click routes material to input");h.succeed();
    }
    @GameTest public void pendingRewardsSurviveSaveAndBreaking(GameTestHelper h){
        var e=machine(h,"auto_sieve");e.setItem(0,omnia("compressed_dirt"));e.setItem(1,omnia("mesh_diamond"));fuel(e);
        // One non-full unrelated output permits processing; actual rewards must stay queued.
        for(int i=6;i<18;i++)e.setItem(i,new ItemStack(Items.DIAMOND,i==6?63:64));
        run(e,e.duration());check(e.getItem(0).isEmpty()&&e.pendingCount()>0,"completed input consumed once with blocked rewards queued");
        int count=e.pendingCount();var tag=e.saveWithFullMetadata(h.getLevel().registryAccess());
        var copy=(MachineEntity)BlockEntity.loadStatic(e.getBlockPos(),e.getBlockState(),tag,h.getLevel().registryAccess());copy.setLevel(h.getLevel());
        check(copy.pendingCount()==count,"queued rewards persist across reload");run(copy,200);
        check(copy.pendingCount()==count,"waiting never rerolls queued rewards");
        for(int i=6;i<18;i++)copy.removeItemNoUpdate(i);copy.tick();
        int emitted=outputs(copy).values().stream().mapToInt(Integer::intValue).sum()+copy.pendingCount();
        check(emitted==count,"unblocking preserves exact queued counts");
        copy.preRemoveSideEffects(copy.getBlockPos(),copy.getBlockState());
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(copy.getBlockPos()).inflate(2));
        check(!drops.isEmpty(),"breaking recovers inventory and pending rewards");h.succeed();
    }
    @GameTest public void upgradesAndToolReplacement(GameTestHelper h){
        var e=machine(h,"auto_hammer");e.setItem(0,new ItemStack(Items.COBBLESTONE,4));e.setItem(1,omnia("hammer_wood"));fuel(e);run(e,30);
        e.setItem(1,omnia("hammer_iron"));e.tick();check(e.progress()==1,"replacing tool resets work");
        e.setItem(3,new ItemStack(Automation.ITEMS.get("speed_upgrade")));e.setItem(4,new ItemStack(Automation.ITEMS.get("speed_upgrade")));e.setItem(5,new ItemStack(Automation.ITEMS.get("speed_upgrade")));
        check(e.duration()==40,"three speed upgrades reduce default duration to 40 percent");run(e,39);
        check(e.getItem(6).is(Items.GRAVEL),"upgraded machine completes");h.succeed();
    }
}
