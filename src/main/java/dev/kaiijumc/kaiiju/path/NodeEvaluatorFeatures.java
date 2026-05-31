package dev.kaiijumc.kaiiju.path;

import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;

public record NodeEvaluatorFeatures(
        NodeEvaluatorType type,
        boolean canPassDoors,
        boolean canFloat,
        boolean canWalkOverFences,
        boolean canOpenDoors,
        boolean allowBreaching
) {

    public static NodeEvaluatorFeatures from(NodeEvaluator evaluator) {

        return new NodeEvaluatorFeatures(
                NodeEvaluatorType.from(evaluator),
                evaluator.canPassDoors(),
                evaluator.canFloat(),
                evaluator.canWalkOverFences(),
                evaluator.canOpenDoors(),
                evaluator instanceof SwimNodeEvaluator swim && swim.allowBreaching
        );

    }

}