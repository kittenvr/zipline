package com.claude.pathfinding;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.pathing.calc.IPathingControlManager;
import com.claude.Zipline;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BaritoneIntegration {
    private final IBaritone baritone;

    public BaritoneIntegration() {
        this.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    }

    public List<BlockPos> calculatePath(BlockPos target) {
        List<BlockPos> path = new ArrayList<>();

        try {
            Goal goal = new GoalBlock(target);
            IPathingControlManager pathingControlManager = baritone.getPathingControlManager();
            ICustomGoalProcess customGoalProcess = baritone.getCustomGoalProcess();

            customGoalProcess.setGoalAndPath(goal);

            long startTime = System.currentTimeMillis();
            while (!customGoalProcess.isActive() && System.currentTimeMillis() - startTime < 5000) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Zipline.LOGGER.error("Interrupted while waiting for path calculation", e);
                    return path;
                }
            }

            if (baritone.getPathingBehavior().isPathing()
                    && baritone.getPathingBehavior().getCurrent() != null
                    && baritone.getPathingBehavior().getCurrent().getPath() != null) {

                baritone.getPathingBehavior().getCurrent().getPath().positions().forEach(bpos -> {
                    path.add(new BlockPos(bpos.x, bpos.y, bpos.z));
                });
            }

            baritone.getPathingBehavior().cancelEverything();

        } catch (Exception e) {
            Zipline.LOGGER.error("Error calculating path with Baritone", e);
        }

        return path;
    }

    public void stopBaritoneProcesses() {
        baritone.getPathingBehavior().cancelEverything();
    }
}
