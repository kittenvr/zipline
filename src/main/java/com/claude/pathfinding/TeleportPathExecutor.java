package com.claude.pathfinding;

import com.claude.Zipline;
import com.claude.world.ChunkLoadingTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class TeleportPathExecutor {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final AtomicBoolean isExecutingPath = new AtomicBoolean(false);
    private int failedAttempts = 0;
    private BlockPos lastPosition = null;
    private long lastTeleportTime = 0;

    private final ChunkLoadingTracker chunkTracker;

    public TeleportPathExecutor() {

        this.chunkTracker = new ChunkLoadingTracker();
    }
    public ChunkLoadingTracker getChunkTracker() {
        return chunkTracker;
    }


    public boolean isExecuting() {
        return isExecutingPath.get();
    }

    public void stopExecution() {
        isExecutingPath.set(false);
    }

    public CompletableFuture<Void> executePath(List<BlockPos> path) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!Zipline.CONFIG.useTeleportation) {
            future.completeExceptionally(new IllegalStateException("Teleportation mode is disabled"));
            return future;
        }

        if (isExecutingPath.getAndSet(true)) {
            future.completeExceptionally(new IllegalStateException("Already executing a path"));
            return future;
        }

        CompletableFuture.runAsync(() -> {
            try {
                executePathInternal(path);
                future.complete(null);
            } catch (Exception e) {
                Zipline.LOGGER.error("Error during path execution", e);
                future.completeExceptionally(e);
            } finally {
                isExecutingPath.set(false);
            }
        });

        return future;
    }

    private void executePathInternal(List<BlockPos> path) throws InterruptedException {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        failedAttempts = 0;
        lastPosition = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());

        int totalNodes = path.size();

        for (int i = 0; i < path.size(); i++) {
            BlockPos targetPos = path.get(i);


            if (!Zipline.CONFIG.useTeleportation) return;

            if (i == path.size() - 1) {
                teleportToPosition(targetPos);
                break;
            }

            Vec3d currentPos = player.getPos();
            Vec3d targetVec = new Vec3d(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            double distance = currentPos.distanceTo(targetVec);

            if (distance <= Zipline.CONFIG.maxDistancePerPacket) {
                if (teleportToPosition(targetPos)) {
                    lastPosition = targetPos;
                } else if (++failedAttempts >= Zipline.CONFIG.maxFailedTeleportAttempts) {
                    if (Zipline.CONFIG.fallbackToNormal) {
                        fallBackToNormalMovement(targetPos);
                        Thread.sleep(Zipline.CONFIG.resumeTeleportAfterBlocksMs);
                        failedAttempts = 0;
                    }
                }
            } else {
                int steps = (int) Math.ceil(distance / Zipline.CONFIG.maxDistancePerPacket);
                for (int step = 1; step <= steps; step++) {
                    double progress = (double) step / steps;
                    Vec3d intermediatePos = currentPos.lerp(targetVec, progress);
                    BlockPos intermediateBlockPos = new BlockPos((int) intermediatePos.x, (int) intermediatePos.y, (int) intermediatePos.z);

                    if (teleportToPosition(intermediateBlockPos)) {
                        lastPosition = intermediateBlockPos;
                    } else if (++failedAttempts >= Zipline.CONFIG.maxFailedTeleportAttempts) {
                        if (Zipline.CONFIG.fallbackToNormal) {
                            fallBackToNormalMovement(targetPos);
                            Thread.sleep(Zipline.CONFIG.resumeTeleportAfterBlocksMs);
                            failedAttempts = 0;
                        }
                        break;
                    }
                }
            }

            Thread.sleep(Zipline.CONFIG.packetDelayMs);
        }
    }

    private boolean teleportToPosition(BlockPos pos) throws InterruptedException {
        ClientPlayerEntity player = client.player;
        if (player == null) return false;

        ChunkPos chunkPos = new ChunkPos(pos);
        if (!chunkTracker.isChunkLoaded(chunkPos)) {
            Zipline.LOGGER.info("Waiting for chunk at {} to load", chunkPos);

            long start = System.currentTimeMillis();
            while (!chunkTracker.isChunkLoaded(chunkPos)) {
                Thread.sleep(Zipline.CONFIG.chunkLoadPollIntervalMs);
                if (System.currentTimeMillis() - start > Zipline.CONFIG.maxWaitForChunkLoadMs) {
                    Zipline.LOGGER.warn("Chunk load timeout for {}", chunkPos);
                    return false;
                }
            }
        }

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                true,
                true
        ));

        return true;
    }

    private void fallBackToNormalMovement(BlockPos pos) {
        Zipline.LOGGER.warn("Falling back to normal Baritone movement to {}", pos);
        // You could call baritone's API here if you want to walk instead.
    }
}
