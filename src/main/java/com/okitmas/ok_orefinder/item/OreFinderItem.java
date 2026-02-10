package com.okitmas.ok_orefinder.item;

import com.okitmas.ok_orefinder.Config;
import com.okitmas.ok_orefinder.FinderHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.okitmas.ok_orefinder.Ok_OreFinder.OK_MOD_LOGGER;


public class OreFinderItem extends Item implements Vanishable {
    @Nullable private BlockPos posCache = null;
    private final ItemLevel itemLevel;

    public OreFinderItem(ItemLevel itemLevel, Properties pProperties) {
        super(pProperties);
        this.itemLevel = itemLevel;
    }

    /**
     * make this item glint enchanted effect
     * @param pStack not use
     * @return true
     */
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    /**
     * Add some tool descriptions in user interface
     * @param pStack properties of item
     * @param pLevel world
     * @param pTooltipComponents original description
     * @param pIsAdvanced a flag
     */
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack,pLevel,pTooltipComponents,pIsAdvanced);
        if (Screen.hasShiftDown()) {
            // 显示详细信息
            pTooltipComponents.add(Component.translatable("tooltip.ok_orefinder.description"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.ok_orefinder.holdshift"));
        }
    }

    public @Nullable BlockPos getPosCache() {
        return posCache;
    }

    public void setPosCache(@Nullable BlockPos newPos) {
        this.posCache = newPos;
    }

    public Set<Block> getTargetOresWithLevel() {
        return switch (this.itemLevel) {
            case DIAMOND_ITEM, NETHERITE_ITEM -> Config.getDiamond_target_ores();
            case IRON_ITEM -> Config.getIron_target_ores();
        };
    }

    public int getBFS_DepthWithLevel() {
        return switch (this.itemLevel) {
            case DIAMOND_ITEM, NETHERITE_ITEM -> Config.getDiamond_bfs_depth();
            case IRON_ITEM -> Config.getIron_bfs_depth();
        };
    }

    public int getDownwardsDepthWithLevel() {
        return switch (this.itemLevel) {
            case DIAMOND_ITEM, NETHERITE_ITEM -> Config.getDiamond_downwards_depth();
            case IRON_ITEM -> Config.getIron_downwards_depth();
        };
    }

    //TODO:Use Tags provided by Forge to judge
    @Override
    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return switch (this.itemLevel) {
            case IRON_ITEM -> pRepair.is(Items.IRON_INGOT);
            case DIAMOND_ITEM -> pRepair.is(Items.DIAMOND);
            case NETHERITE_ITEM -> pRepair.is(Items.NETHERITE_SCRAP);
        };
    }

    public ItemLevel getItemLevel() {return this.itemLevel;}

    /**
     * decide behavior of ore finder when right-click a block
     * @param pContext to get clicked position, player and world(level)
     * @return the result of Interaction(success)
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        BlockPos clickedPos = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();

        if (player instanceof ServerPlayer serverPlayer && !level.isClientSide()) {
            serverPlayer.getCooldowns().addCooldown(this, Config.getSearch_cooldown());

            serverPlayer.sendSystemMessage(Component.translatable("tip.ok_orefinder.searching"));

            pContext.getItemInHand().hurtAndBreak(1, player,
                    (pPlayer) -> pPlayer.broadcastBreakEvent(pContext.getHand())
            );

            if (FinderHelper.isExecutorRunning()) {
                FinderHelper.wrappedSearchingAction(this, clickedPos, level, serverPlayer);
            } else {
                OK_MOD_LOGGER.info("searching in main server thread");
                FinderHelper.searchingAction(this, clickedPos, level, serverPlayer);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * check the validity of current BlockPos cache
     * @param clickedPos the position aimed at
     * @param level the world to check block
     * @return validity of BlockPos cache (valid -> true)
     */
    public boolean isPosCacheValid(BlockPos clickedPos, Level level) {
        // 缓存值是否为空
        if (posCache== null) {
            return false;
        }
        // 距离是否包含在搜寻范围内
        int xDiff = posCache.getX() - clickedPos.getX();
        int zDiff = posCache.getZ() - clickedPos.getZ();
        int yDiff = posCache.getY() - clickedPos.getY();
        if (Screen.hasShiftDown()) {
            int yCapability = clickedPos.getY() - level.getMinBuildHeight();
            if (Math.abs(xDiff) + Math.abs(zDiff) > Config.getDownwards_layer_size() ||
                    yDiff > Math.min(Config.getDiamond_downwards_depth(), yCapability)) {
                posCache = null;
                return false;
            }
        } else {
            if (Math.abs(xDiff) + Math.abs(zDiff) + Math.abs(yDiff) > Config.getDiamond_bfs_depth()) {
                posCache = null;
                return false;
            }
        }
        // 目标是否仍然符合要求
        BlockState blockState = level.getBlockState(posCache);
        if (!this.getTargetOresWithLevel().contains(blockState.getBlock())) {
            posCache = null;
            return false;
        }

        return true;
    }
}
