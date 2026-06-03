package dev.kaiijumc.kaiiju.path;

import ca.spottedleaf.concurrentutil.util.Validate;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@DefaultQualifier(NotNull.class)
public class NodeEvaluatorCache {

    private static final Map<NodeEvaluatorFeatures, ConcurrentLinkedQueue<NodeEvaluator>> threadLocalEvaluators = new ConcurrentHashMap<>();
    private static final Map<NodeEvaluator, NodeEvaluatorGenerator> evaluatorGenerators = new ConcurrentHashMap<>();

    private static Queue<NodeEvaluator> getQueue(NodeEvaluatorFeatures features) {
        return threadLocalEvaluators.computeIfAbsent(features, ignored -> new ConcurrentLinkedQueue<>());
    }

    public static NodeEvaluator take(NodeEvaluatorGenerator generator, NodeEvaluator local) {
        final NodeEvaluatorFeatures features = NodeEvaluatorFeatures.from(local);
        @Nullable NodeEvaluator evaluator = getQueue(features).poll();

        if (evaluator == null) evaluator = generator.generate(features);

        evaluatorGenerators.put(evaluator, generator);
        return evaluator;
    }

    public static void returnEvaluator(NodeEvaluator evaluator) {
        final NodeEvaluatorGenerator generator = evaluatorGenerators.remove(evaluator);
        Validate.notNull(generator, "NodeEvaluator already returned");

        final NodeEvaluatorFeatures features = NodeEvaluatorFeatures.from(evaluator);
        getQueue(features).offer(evaluator);
    }

    public static void remove(NodeEvaluator evaluator) {
        evaluatorGenerators.remove(evaluator);
    }

}