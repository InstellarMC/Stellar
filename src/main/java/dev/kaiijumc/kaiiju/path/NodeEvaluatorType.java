package dev.kaiijumc.kaiiju.path;

import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;

public enum NodeEvaluatorType {

    WALK, SWIM, FLY, AMPHIBIOUS;

    public static NodeEvaluatorType from(NodeEvaluator evaluator) {

        return switch (evaluator) {

            case AmphibiousNodeEvaluator ignored -> AMPHIBIOUS;
            case SwimNodeEvaluator ignored -> SWIM;
            case FlyNodeEvaluator ignored -> FLY;
            default -> WALK;

        };

    }

}