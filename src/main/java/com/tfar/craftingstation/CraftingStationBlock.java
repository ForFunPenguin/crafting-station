package com.tfar.craftingstation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.IntStream;

public class CraftingStationBlock extends Block implements IWaterLoggable {

  public static final VoxelShape shape;

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;


  private static final Random RANDOM = new Random();

  public CraftingStationBlock(Properties p_i48440_1_) {
    super(p_i48440_1_);
    this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(WATERLOGGED, false);
  }

  static {
    VoxelShape[] shapes = new VoxelShape[5];

    shapes[0] = Block.makeCuboidShape(0, 12, 0, 16, 16, 16);
    shapes[1] = Block.makeCuboidShape(0, 0, 0, 4, 12, 4);
    shapes[2] = Block.makeCuboidShape(12, 0, 0, 16, 12, 4);
    shapes[3] = Block.makeCuboidShape(0, 0, 12, 4, 12, 16);
    shapes[4] = Block.makeCuboidShape(12, 0, 12, 16, 12, 16);

    shape = VoxelShapes.or(shapes[0], shapes[1], shapes[2], shapes[3], shapes[4]);
  }

  @Override
  public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
    if (!world.isRemote) {
      TileEntity tileEntity = world.getTileEntity(pos);
      if (tileEntity instanceof INamedContainerProvider) {
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
      } else {
        throw new IllegalStateException("Our named container provider is missing!");
      }
    }
    return true;
  }

  @Override
  public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);
    return te instanceof CraftingStationBlockEntity ? (INamedContainerProvider) te : null;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new CraftingStationBlockEntity();
  }

  @Override
  public void onReplaced(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof CraftingStationBlockEntity) {
        dropItems((CraftingStationBlockEntity) tileentity, worldIn, pos);
        worldIn.updateComparatorOutputLevel(pos, this);
      }
      super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
  }

  public static void dropItems(CraftingStationBlockEntity table, World world, BlockPos pos) {
    IntStream.range(0, table.input.getSlots()).mapToObj(i -> table.input.getStackInSlot(i)).filter(stack -> !stack.isEmpty()).forEach(stack -> spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
  }

  public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack) {
    double d0 = EntityType.ITEM.getWidth();
    double d1 = 1 - d0;
    double d2 = d0 / 2;
    double d3 = Math.floor(x) + RANDOM.nextDouble() * d1 + d2;
    double d4 = Math.floor(y) + RANDOM.nextDouble() * d1;
    double d5 = Math.floor(z) + RANDOM.nextDouble() * d1 + d2;

    while (!stack.isEmpty()) {
      ItemEntity itementity = new ItemEntity(worldIn, d3, d4, d5, stack.split(RANDOM.nextInt(21) + 10));
      float f = 0.05F;
      itementity.setMotion(RANDOM.nextGaussian() * f, RANDOM.nextGaussian() * f + 0.2, RANDOM.nextGaussian() * f);
      worldIn.addEntity(itementity);
    }
  }

  @Override
  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
    return shape;
  }

  @Nonnull
  public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
    return p_185499_1_.with(FACING, p_185499_2_.rotate(p_185499_1_.get(FACING)));
  }

  @Nonnull
  public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
    return p_185471_1_.rotate(p_185471_2_.toRotation(p_185471_1_.get(FACING)));
  }

  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> p_206840_1_) {
    p_206840_1_.add(WATERLOGGED);
    p_206840_1_.add(FACING);
  }

  @Nonnull
  public IFluidState getFluidState(BlockState p_204507_1_) {
    return p_204507_1_.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(p_204507_1_);
  }

  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
    IWorld lvt_2_1_ = p_196258_1_.getWorld();
    BlockPos lvt_3_1_ = p_196258_1_.getPos();
    boolean lvt_4_1_ = lvt_2_1_.getFluidState(lvt_3_1_).getFluid() == Fluids.WATER;
    return this.getDefaultState().with(FACING, p_196258_1_.getPlacementHorizontalFacing()).with(WATERLOGGED, lvt_4_1_);
  }
}
