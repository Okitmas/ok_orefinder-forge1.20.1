package com.okitmas.ok_orefinder;

import com.okitmas.ok_orefinder.item.OreFinderItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.okitmas.ok_orefinder.Ok_OreFinder.OK_MOD_LOGGER;

public class FinderHelper {
    public static final int CORE_POOL_SIZE = 1;
    public static final int MAX_POOL_SIZE = 1;
    public static final long KEEP_ALIVE_TIME = 10L;
    public static final int QUEUE_CAPACITY = 8;
    private static ThreadPoolExecutor EXECUTORS;

    public static void startExecutor(){
        shutdownExecutor();

        OK_MOD_LOGGER.info("starting the executor with core size {}, max size {}, queue capacity {}.",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        EXECUTORS = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadFactory() {
                    private static final AtomicInteger poolNum = new AtomicInteger(1);
                    private static final AtomicInteger threadNum = new AtomicInteger(1);
                    private static final String prefix = String.format("%s-pool#%d", Ok_OreFinder.MOD_ID, poolNum.get());
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        String threadName = String.format("%s-thread#%d-",prefix,threadNum.getAndIncrement());
                        return new Thread(r,threadName);
                    }
                },
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    public static void shutdownExecutor() {
        if (EXECUTORS != null) {
            OK_MOD_LOGGER.info("shutting down the executor...");
            EXECUTORS.shutdownNow();
        }
    }

    public static boolean isExecutorRunning() {
        return FinderHelper.EXECUTORS != null && !FinderHelper.EXECUTORS.isShutdown();
    }

    public static void wrappedSearchingAction(OreFinderItem oreFinderItem, BlockPos clickedPos, Level level, ServerPlayer serverPlayer) {
        EXECUTORS.execute(() -> {
            OK_MOD_LOGGER.debug("start to searching ores");
            final long startTime = System.nanoTime();
            FinderHelper.searchingAction(oreFinderItem, clickedPos, level, serverPlayer);
            final String spendTime = NumberFormat.getNumberInstance().format(
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            );
            OK_MOD_LOGGER.debug("execution finished, elapse {} ms", spendTime);
        });
    }

    public static void searchingAction(
            @NotNull OreFinderItem oreFinderItem,
            @NotNull BlockPos clickedPos,
            @NotNull Level level,
            @NotNull ServerPlayer serverPlayer
    ) {
        BlockPos targetBlockPos = null;
        // 检查缓存值
        if (oreFinderItem.isPosCacheValid(clickedPos,level)){
            targetBlockPos = oreFinderItem.getPosCache();
        } else {
            Optional<BlockPos> posOpt = (Screen.hasShiftDown()) ?
                    searchOreDownwards(clickedPos, level,
                            oreFinderItem.getDownwardsDepthWithLevel(),
                            oreFinderItem.getTargetOresWithLevel()) :
                    searchOreSpread(clickedPos, level,
                            oreFinderItem.getBFS_DepthWithLevel(),
                            oreFinderItem.getTargetOresWithLevel());

            if (posOpt.isEmpty()) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("tip.ok_orefinder.notfound")
                ));
            } else {
                targetBlockPos = posOpt.get();
                oreFinderItem.setPosCache(targetBlockPos);
            }
        }
        if (targetBlockPos != null){
            if (level instanceof ServerLevel serverLevel) {
                genParticlesAndSoundServer(serverLevel, serverPlayer,
                        ParticleTypes.COMPOSTER, SoundEvents.EXPERIENCE_ORB_PICKUP);
                sendMsgDecidedByRotation(targetBlockPos, serverLevel, serverPlayer);
            }
        }
    }

    /* 广度优先方法 */
    public static Optional<BlockPos> searchOreSpread(
            BlockPos beginPos,
            Level world,
            int bfs_depth,
            Set<Block> target_ores
    ) {
        // 位置队列
        ArrayDeque<BlockPos> currentPosQueue = new ArrayDeque<>();
        // 从被点击的方块开始
        currentPosQueue.addLast(beginPos);
        // 查重表
        HashSet<BlockPos> operatedPos = new HashSet<>(currentPosQueue);
        // 逐层查找
        for (int remainDepth = bfs_depth;
             !currentPosQueue.isEmpty() && remainDepth >= 0;
             remainDepth --) {
            // 每一层的所有块都需要查找
            for (int remainsBlockPos = currentPosQueue.size();
                 remainsBlockPos > 0;
                 remainsBlockPos --) {
                // 取出当前层的元素
                BlockPos targetBlockPos = currentPosQueue.removeFirst();
                BlockState currentBlock = world.getBlockState(targetBlockPos);
                // 如果是可跳过逻辑的方块(默认为空气、基岩、水和岩浆)
                if (Config.getSkip_blocks().contains(currentBlock.getBlock())) {
                    continue;
                }
                // 如果是目标矿物
                if (target_ores.contains(currentBlock.getBlock())) {
                    return Optional.of(targetBlockPos);
                }
                // 如果不是最后一层
                if (remainDepth > 0) {
                    // 将下一层的方块加入队列
                    spread(operatedPos, currentPosQueue, new BlockPos[]{
                            targetBlockPos.east(),
                            targetBlockPos.south(),
                            targetBlockPos.west(),
                            targetBlockPos.north(),
                            targetBlockPos.below(),
                            targetBlockPos.above()
                    });
                }
            }
        }
        return Optional.empty();
    }

    /**
     * add nodes in next layer into queue
     * @param closed includes nodes already add into queue
     * @param open on behalf of nodes in next layer
     * @param blockPositions next nodes
     */
    public static void spread(Set<BlockPos> closed, Deque<BlockPos> open, BlockPos[] blockPositions) {
        Arrays.stream(blockPositions)
                .filter((blockPos -> !closed.contains(blockPos)))
                .forEach((blockPos -> {
                    closed.add(blockPos);
                    open.addLast(blockPos);
                }));
    }

    /* 按层向下查找，每层仍然是广度优先 */
    public static Optional<BlockPos> searchOreDownwards(
            BlockPos beginPos,
            Level world,
            int downwards_depth,
            Set<Block> target_ores
    ) {
        int yCapability = beginPos.getY() - world.getMinBuildHeight();

        for (int downwardsDepth = 0;
             downwardsDepth <= Math.min(downwards_depth, yCapability);
             downwardsDepth ++) {
            // 从每层的中心开始
            BlockPos currentCenter = beginPos.below(downwardsDepth);
            ArrayDeque<BlockPos> currentPosQueue = new ArrayDeque<>();
            currentPosQueue.addLast(currentCenter);
            HashSet<BlockPos> operatedPos = new HashSet<>(currentPosQueue);
            // 扩散
            for (int layerSpreadDistance = Config.getDownwards_layer_size();
                 layerSpreadDistance >= 0;
                 layerSpreadDistance--) {
                for (int remainsBlockPos = currentPosQueue.size();
                     remainsBlockPos > 0;
                     remainsBlockPos--) {
                    BlockPos targetBlockPos = currentPosQueue.removeFirst();

                    BlockState currentBlock = world.getBlockState(targetBlockPos);
                    if (target_ores.contains(currentBlock.getBlock())) {
                        return Optional.of(targetBlockPos);
                    }

                    if (layerSpreadDistance > 0) {
                        spread(operatedPos, currentPosQueue, new BlockPos[]{
                                targetBlockPos.east(),
                                targetBlockPos.south(),
                                targetBlockPos.west(),
                                targetBlockPos.north()
                        });
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * generate target particles and sound for trigger player from server side
     * @param serverLevel world's server
     * @param serverPlayer trigger player in server side
     * @param targetParticle target particle
     * @param soundEvent target sound
     * @param <T> a class extends form ParticleOptions
     */
    public static  <T extends ParticleOptions> void genParticlesAndSoundServer(ServerLevel serverLevel, ServerPlayer serverPlayer, T targetParticle, SoundEvent soundEvent) {
        RandomSource rs = serverLevel.getRandom();
        // 粒子的表现不太好
        serverLevel.sendParticles(serverPlayer,targetParticle, false,
                serverPlayer.getX() ,
                serverPlayer.getY(),
                serverPlayer.getZ(),35,
                rs.nextGaussian(),0.0,rs.nextGaussian(),
                rs.nextGaussian()
        );
        serverLevel.playSound(null,serverPlayer.getX(),serverPlayer.getY(),serverPlayer.getZ(),
                soundEvent, SoundSource.AMBIENT,1.0f,0.375f + rs.nextInt(20) * 0.0625f);
    }

    /**
     * send a tip decided by player's rotation to action bar from server
     * @param blockPos target block position
     * @param serverLevel target level in server side
     * @param serverPlayer player in server side
     */
    public static void sendMsgDecidedByRotation(BlockPos blockPos, ServerLevel serverLevel, ServerPlayer serverPlayer) {
        // 获取方块名称
        String blockName = serverLevel.getBlockState(blockPos).getBlock().getName().getString();
        // 获取玩家偏转角
        float playerYaw = serverPlayer.getYRot();
        // 获取目标方块与玩家的距离差
        BlockPos playerPos = serverPlayer.getOnPos();
        int diffX = blockPos.getX() - playerPos.getX();
        int diffZ = blockPos.getZ() - playerPos.getZ();
        int diffY = blockPos.getY() - (playerPos.getY() + 1);// 按照玩家的脚所在方块计算

        // 如果不高于玩家，显示在下方
        MutableComponent yHint = (diffY <= 0) ?
                Component.translatable("tip.ok_orefinder.belowHint") :
                Component.translatable("tip.ok_orefinder.aboveHint");

        MutableComponent xHint;
        MutableComponent zHint;
        if (playerYaw >= -135.0F && playerYaw < -45.0F) {// 面朝东，x+
            // x轴为前和后
            xHint = (diffX >= 0) ?
                    Component.translatable("tip.ok_orefinder.frontHint") :
                    Component.translatable("tip.ok_orefinder.behindHint");
            // z轴为右和左
            zHint = (diffZ >= 0) ?
                    Component.translatable("tip.ok_orefinder.rightHint") :
                    Component.translatable("tip.ok_orefinder.leftHint");
        } else if (playerYaw >= -45.0F && playerYaw < 45.0F) {// 面朝南, z+
            // z轴为前和后
            zHint = (diffZ >= 0) ?
                    Component.translatable("tip.ok_orefinder.frontHint") :
                    Component.translatable("tip.ok_orefinder.behindHint");
            // x轴为左和右
            xHint = (diffX >= 0) ?
                    Component.translatable("tip.ok_orefinder.leftHint") :
                    Component.translatable("tip.ok_orefinder.rightHint");
        } else if (playerYaw >= 45.0F && playerYaw < 135.0F) {// 面朝西, x-
            xHint = (diffX >= 0) ?
                    Component.translatable("tip.ok_orefinder.behindHint") :
                    Component.translatable("tip.ok_orefinder.frontHint");
            zHint = (diffZ >= 0) ?
                    Component.translatable("tip.ok_orefinder.leftHint") :
                    Component.translatable("tip.ok_orefinder.rightHint");
        } else {// 面朝北, z-
            zHint = (diffZ >= 0) ?
                    Component.translatable("tip.ok_orefinder.behindHint") :
                    Component.translatable("tip.ok_orefinder.frontHint");
            xHint = (diffX >= 0) ?
                    Component.translatable("tip.ok_orefinder.rightHint") :
                    Component.translatable("tip.ok_orefinder.leftHint");
        }
        // 从服务端向客户端发送提示
        serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                Component.translatable("tip.ok_orefinder.guideRot",blockName,
                        Math.abs(diffX), xHint.getString(),
                        Math.abs(diffY), yHint.getString(),
                        Math.abs(diffZ), zHint.getString()
                )));
    }
}
