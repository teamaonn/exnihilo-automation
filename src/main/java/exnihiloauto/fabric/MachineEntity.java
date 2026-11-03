package exnihiloauto.fabric;

import java.util.*;
import exnihiloomnia.fabric.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class MachineEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    // Input, mesh/hammer/worm, fuel, three upgrades, twelve outputs.
    public static final int SIZE=18,OUTPUT_START=6;
    private final NonNullList<ItemStack> items=NonNullList.withSize(SIZE,ItemStack.EMPTY);
    private final List<ItemStack> pending=new ArrayList<>();
    private ItemStack jobInput=ItemStack.EMPTY,jobTool=ItemStack.EMPTY;
    private int progress,energy,wormCharges,status;
    private long fuelReserve;
    public MachineEntity(BlockPos pos,BlockState state){super(Automation.ENTITY,pos,state);}
    public MachineKind kind(){return ((MachineBlock)getBlockState().getBlock()).kind;}
    public int energy(){return energy;}
    public int progress(){return progress;}
    public int pendingCount(){return pending.stream().mapToInt(ItemStack::getCount).sum();}
    public int wormCharges(){return wormCharges;}
    public int upgrade(String name){int n=0;for(int i=3;i<6;i++)if(Automation.is(items.get(i),name))n++;return n;}
    public int duration(){
        int base=switch(kind()){case SIEVE->MachineConfig.sieveTicks;case HAMMER->MachineConfig.hammerTicks;case SILKER->MachineConfig.silkerTicks;};
        return Math.max(1,base*(100-20*upgrade("speed_upgrade"))/100);
    }
    public static int mesh(ItemStack stack){for(int i=0;i<ExNihiloOmnia.MESHES.length;i++)if(stack.is(ExNihiloOmnia.ITEMS.get(ExNihiloOmnia.MESHES[i])))return i+1;return 0;}
    public static int material(ItemStack stack){for(int i=1;i<=ExNihiloOmnia.MATERIALS.length;i++)if(stack.is(ExNihiloOmnia.materialItem(i,false))||stack.is(ExNihiloOmnia.materialItem(i,true)))return i;return 0;}
    public static boolean compressed(ItemStack stack){for(var entry:ExNihiloOmnia.ITEMS.entrySet())if(entry.getKey().startsWith("compressed_")&&stack.is(entry.getValue()))return true;return false;}
    public static boolean reinforced(ItemStack stack){for(var entry:Automation.ITEMS.entrySet())if(entry.getKey().startsWith("reinforced_hammer_")&&stack.is(entry.getValue()))return true;return false;}
    public static boolean hammer(ItemStack stack){return stack.getItem() instanceof ExNihiloOmnia.HammerItem||reinforced(stack);}
    public static boolean leaves(ItemStack stack){return stack.getItem() instanceof BlockItem b&&ExNihiloOmnia.INFESTABLE.containsKey(b.getBlock());}
    public static Block hammered(ItemStack stack){return stack.getItem() instanceof BlockItem b?ExNihiloOmnia.hammerOutput(b.getBlock()):null;}
    public static int fuelTicks(ItemStack stack){
        if(stack.is(Items.COAL)||stack.is(Items.CHARCOAL))return 1600;
        if(stack.is(Items.COAL_BLOCK))return 16000;
        if(stack.is(Items.BLAZE_ROD))return 2400;
        if(stack.is(Items.LAVA_BUCKET))return 20000;
        return 0;
    }
    public boolean acceptsInput(ItemStack stack){return switch(kind()){case SIEVE->material(stack)>0;case HAMMER->hammered(stack)!=null;case SILKER->leaves(stack);};}
    public boolean acceptsTool(ItemStack stack){return switch(kind()){case SIEVE->mesh(stack)>0;case HAMMER->hammer(stack);case SILKER->stack.is(ExNihiloOmnia.ITEMS.get("silkworm"));};}
    private int requirements(){
        if(!acceptsInput(items.get(0)))return 2;
        if(kind()!=MachineKind.SILKER||wormCharges==0)if(!acceptsTool(items.get(1)))return 3;
        if(kind()==MachineKind.HAMMER&&compressed(items.get(0))&&upgrade("reinforced_upgrade")==0&&!reinforced(items.get(1)))return 4;
        return 0;
    }
    public void tick(){
        if(!(level instanceof ServerLevel server))return;
        // A finished job owns its exact rolled rewards. Flush before starting a new job.
        flushPending();
        int next=requirements();
        if(!pending.isEmpty()||outputsFull())next=6;
        if(level.hasNeighborSignal(worldPosition))next=7;
        boolean working=false;
        if(next==0){
            if(!same(jobInput,items.get(0))||!same(jobTool,items.get(1))){progress=0;jobInput=items.get(0).copyWithCount(1);jobTool=items.get(1).copyWithCount(1);}
            charge();
            int cost=MachineConfig.energyPerTick;
            if(energy<cost)next=5;
            else {
                energy-=cost;progress++;working=true;next=1;
                if(progress>=duration()){complete(server);progress=0;jobInput=ItemStack.EMPTY;jobTool=ItemStack.EMPTY;flushPending();}
            }
            setChanged();
        }else if(next==2||next==3||next==4){if(progress!=0){progress=0;setChanged();}}
        status=next;
        if(getBlockState().getValue(MachineBlock.ACTIVE)!=working)level.setBlock(worldPosition,getBlockState().setValue(MachineBlock.ACTIVE,working),3);
    }
    private static boolean same(ItemStack a,ItemStack b){return a.isEmpty()&&b.isEmpty()||!a.isEmpty()&&!b.isEmpty()&&ItemStack.isSameItemSameComponents(a,b);}
    private void charge(){
        if(energy>=MachineConfig.capacity)return;
        if(fuelReserve==0&&fuelTicks(items.get(2))>0){
            var fuel=items.get(2);fuelReserve=(long)fuelTicks(fuel)*MachineConfig.fuelEnergyPerTick;
            if(fuel.is(Items.LAVA_BUCKET))items.set(2,new ItemStack(Items.BUCKET));else fuel.shrink(1);
        }
        int transfer=(int)Math.min(fuelReserve,MachineConfig.capacity-energy);energy+=transfer;fuelReserve-=transfer;
    }
    private boolean outputsFull(){for(int i=6;i<SIZE;i++)if(items.get(i).isEmpty()||items.get(i).getCount()<items.get(i).getMaxStackSize())return false;return true;}
    private void complete(ServerLevel server){
        var input=items.get(0);var tool=items.get(1);
        int bonus=upgrade("bonus_upgrade");
        switch(kind()){
            case SIEVE -> {
                SieveDrops.roll(material(input),mesh(tool),compressed(input)?9:1,server.getRandom(),pending::add);
                // Bonus multiplies successful rewards; it never unlocks an unavailable mesh tier.
                for(var reward:pending)if(server.getRandom().nextFloat()<.25F*bonus)reward.grow(reward.getCount());
            }
            case HAMMER -> {
                Block output=hammered(input);
                int count=server.getRandom().nextFloat()<.25F*bonus?2:1;
                pending.add(new ItemStack(output,count));
                int damage=compressed(input)?9:1;
                tool.hurtAndBreak(damage,server,null,item->{});
            }
            case SILKER -> {
                if(wormCharges==0){tool.shrink(1);wormCharges=4;}
                wormCharges--;
                pending.add(new ItemStack(Items.STRING,server.getRandom().nextFloat()<.25F*bonus?2:1));
            }
        }
        input.shrink(1);setChanged();
    }
    private void flushPending(){
        boolean changed=false;
        for(var stack:pending){
            for(int i=6;i<SIZE&&!stack.isEmpty();i++){
                var target=items.get(i);
                if(target.isEmpty()){int n=Math.min(stack.getCount(),stack.getMaxStackSize());items.set(i,stack.split(n));changed=true;}
                else if(ItemStack.isSameItemSameComponents(target,stack)){
                    int n=Math.min(stack.getCount(),target.getMaxStackSize()-target.getCount());
                    if(n>0){target.grow(n);stack.shrink(n);changed=true;}
                }
            }
        }
        pending.removeIf(ItemStack::isEmpty);
        if(changed)setChanged();
    }
    public final ContainerData data=new ContainerData(){
        public int getCount(){return 8;}
        public int get(int i){return switch(i){case 0->energy&65535;case 1->energy>>>16;case 2->progress*1000/duration();case 3->status;case 4->wormCharges;case 5->MachineConfig.capacity&65535;case 6->MachineConfig.capacity>>>16;case 7->kind().ordinal();default->0;};}
        public void set(int i,int value){}
    };
    public Component getDisplayName(){return Component.translatable("block.exnihiloauto."+kind().id);}
    public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new MachineMenu(id,inventory,this,data);}
    public int getContainerSize(){return SIZE;}
    public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    public ItemStack getItem(int slot){return slot>=0&&slot<SIZE?items.get(slot):ItemStack.EMPTY;}
    public ItemStack removeItem(int slot,int count){var result=items.get(slot).split(count);setChanged();return result;}
    public ItemStack removeItemNoUpdate(int slot){var result=items.get(slot);items.set(slot,ItemStack.EMPTY);return result;}
    public void setItem(int slot,ItemStack stack){items.set(slot,stack);setChanged();}
    public void clearContent(){items.clear();setChanged();}
    public boolean stillValid(Player player){return Container.stillValidBlockEntity(this,player);}
    public int slotLimit(int slot){return slot>=3&&slot<6||slot==1&&kind()!=MachineKind.SILKER?1:64;}
    public int getMaxStackSize(ItemStack stack){return mesh(stack)>0?1:Math.min(64,stack.getMaxStackSize());}
    public boolean canPlaceItem(int slot,ItemStack stack){
        if(stack.isEmpty())return false;
        if(slot==0)return acceptsInput(stack);
        if(slot==1)return acceptsTool(stack);
        if(slot==2)return fuelTicks(stack)>0;
        if(slot>=3&&slot<6)return Automation.is(stack,"speed_upgrade")||Automation.is(stack,"bonus_upgrade")||kind()==MachineKind.HAMMER&&Automation.is(stack,"reinforced_upgrade");
        return false;
    }
    public int[] getSlotsForFace(Direction side){
        if(side==Direction.DOWN)return java.util.stream.IntStream.concat(java.util.stream.IntStream.of(2),java.util.stream.IntStream.range(6,SIZE)).toArray();
        if(side==Direction.UP)return new int[]{0,1};
        return new int[]{2};
    }
    public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return side==Direction.UP&&(slot==0||slot==1)&&canPlaceItem(slot,stack)||side!=Direction.UP&&side!=Direction.DOWN&&slot==2&&canPlaceItem(slot,stack);}
    public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return side==Direction.DOWN&&slot>=6||slot==2&&stack.is(Items.BUCKET)&&side!=Direction.UP;}
    public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level instanceof ServerLevel){for(var stack:items)if(!stack.isEmpty())Block.popResource(level,pos,stack.copy());for(var stack:pending)Block.popResource(level,pos,stack.copy());}
        items.clear();pending.clear();super.preRemoveSideEffects(pos,state);
    }
    protected void saveAdditional(ValueOutput out){
        super.saveAdditional(out);
        for(int i=0;i<SIZE;i++)if(!items.get(i).isEmpty())out.store("slot_"+i,ItemStack.CODEC,items.get(i));
        out.store("pending",ItemStack.CODEC.listOf(),pending);
        if(!jobInput.isEmpty())out.store("job_input",ItemStack.CODEC,jobInput);
        if(!jobTool.isEmpty())out.store("job_tool",ItemStack.CODEC,jobTool);
        out.putInt("progress",progress);out.putInt("energy",energy);out.putLong("fuel_reserve",fuelReserve);out.putInt("worm_charges",wormCharges);
    }
    protected void loadAdditional(ValueInput in){
        super.loadAdditional(in);
        for(int i=0;i<SIZE;i++)items.set(i,in.read("slot_"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));
        pending.clear();pending.addAll(in.read("pending",ItemStack.CODEC.listOf()).orElse(List.of()));
        jobInput=in.read("job_input",ItemStack.CODEC).orElse(ItemStack.EMPTY);jobTool=in.read("job_tool",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        progress=Math.clamp(in.getIntOr("progress",0),0,120000);
        energy=Math.clamp(in.getIntOr("energy",0),0,MachineConfig.capacity);
        fuelReserve=Math.clamp(in.getLongOr("fuel_reserve",0),0,20000000);
        wormCharges=Math.clamp(in.getIntOr("worm_charges",0),0,4);
    }
}
