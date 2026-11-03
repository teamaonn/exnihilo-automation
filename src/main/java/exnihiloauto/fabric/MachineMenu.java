package exnihiloauto.fabric;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class MachineMenu extends AbstractContainerMenu {
    private final Container container;
    public final ContainerData data;
    public MachineMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(MachineEntity.SIZE),new SimpleContainerData(8));}
    public MachineMenu(int id,Inventory inv,Container container,ContainerData data){
        super(Automation.MENU,id);this.container=container;this.data=data;
        container.startOpen(inv.player);
        addMachineSlot(0,16,35);addMachineSlot(1,52,35);addMachineSlot(2,16,79);
        addMachineSlot(3,52,79);addMachineSlot(4,70,79);addMachineSlot(5,88,79);
        for(int i=0;i<12;i++)addMachineSlot(i+6,144+i%4*18,27+i/4*18);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,35+col*18,145+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,35+col*18,203));
        addDataSlots(data);
    }
    private void addMachineSlot(int index,int x,int y){
        addSlot(new Slot(container,index,x,y){
            public boolean mayPlace(ItemStack stack){return index<6&&container.canPlaceItem(index,stack);}
            public int getMaxStackSize(){return container instanceof MachineEntity e?e.slotLimit(index):index>=3&&index<6?1:64;}
        });
    }
    public int energy(){return (data.get(0)&65535)|(data.get(1)&65535)<<16;}
    public int capacity(){return (data.get(5)&65535)|(data.get(6)&65535)<<16;}
    public int progress(){return data.get(2);}
    public int status(){return data.get(3);}
    public int wormCharges(){return data.get(4);}
    public MachineKind kind(){return MachineKind.values()[Math.clamp(data.get(7),0,2)];}
    public boolean stillValid(Player player){return container.stillValid(player);}
    public void removed(Player player){super.removed(player);container.stopOpen(player);}
    public ItemStack quickMoveStack(Player player,int index){
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),original=stack.copy();
        if(index<MachineEntity.SIZE){if(!moveItemStackTo(stack,MachineEntity.SIZE,slots.size(),true))return ItemStack.EMPTY;}
        else {
            boolean moved=false;
            for(int i=0;i<6;i++)if(slots.get(i).mayPlace(stack)){if(moveItemStackTo(stack,i,i+1,false))moved=true;if(stack.isEmpty())break;}
            if(!moved)return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(player,stack);return original;
    }
}
