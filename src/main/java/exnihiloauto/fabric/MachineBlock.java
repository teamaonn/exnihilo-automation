package exnihiloauto.fabric;

import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class MachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public final MachineKind kind;
    public MachineBlock(MachineKind kind,Properties p){super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(ACTIVE,false).setValue(FACING,Direction.NORTH));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ACTIVE,FACING);}
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new MachineEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){
        return !l.isClientSide()&&type==Automation.ENTITY?(w,p,b,e)->((MachineEntity)e).tick():null;
    }
    protected InteractionResult useItemOn(ItemStack i,BlockState s,Level l,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){return open(l,p,player);}
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return open(l,p,player);}
    private InteractionResult open(Level l,BlockPos p,Player player){
        if(!l.isClientSide()&&l.getBlockEntity(p) instanceof MachineEntity e)player.openMenu(e);
        return InteractionResult.SUCCESS;
    }
    protected boolean hasAnalogOutputSignal(BlockState state){return true;}
    protected int getAnalogOutputSignal(BlockState state,Level level,BlockPos pos,Direction direction){
        return level.getBlockEntity(pos) instanceof MachineEntity e ? net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(e):0;
    }
}
