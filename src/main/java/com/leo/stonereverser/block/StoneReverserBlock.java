package com.leo.stonereverser.block;

import com.leo.stonereverser.menu.StoneReverserMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StoneReverserBlock extends DirectionalBlock {
    public static final Component TITLE = Component.translatable("container.stone_reverser");
    public static final MapCodec<StoneReverserBlock> CODEC = simpleCodec(StoneReverserBlock::new);

    public StoneReverserBlock(Properties properties) {
        super(properties);
        // 默认朝下（与原版 stonecutter 一致）
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 与原版 stonecutter 相同：点击面的朝向就是正面方向。
        // 这样 parent=minecraft:block/stonecutter 的 x/y 旋转完全匹配原版，贴图不会错位。
        Direction facing = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider provider = getMenuProvider(state, level, pos);
        if (provider != null) {
            player.openMenu(provider);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inv, p) ->
                new StoneReverserMenu(id, inv, ContainerLevelAccess.create(level, pos)), TITLE);
    }

    /**
     * 半高碰撞箱（与原版石切机一致）：底部 10 像素是实心底座，顶部 6 像素是锯片区域可穿越。
     * 这样玩家能走上底座，但能跨过/站在锯片位置，不再像完整方块那样"站在锯片上"。
     */
    private static final VoxelShape COLLISION_SHAPE = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    /**
     * 选择形状（ray trace 用完整方块）：保持与 parent model 一致，
     * 否则点击面/放置朝向会错位。
     */
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
}
