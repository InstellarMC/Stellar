package dev.kaiijumc.kaiiju.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class AsyncPath extends Path {

    private volatile boolean processed = false;
    private final List<Runnable> postProcessors = new ArrayList<>(0);
    private final Set<BlockPos> positions;
    private final Supplier<Path> pathSupplier;
    private final Runnable postSupplier;
    private final List<Node> nodes;

    private @Nullable BlockPos target;
    private float distToTarget = 0;
    private boolean canReach = true;

    @SuppressWarnings("DataFlowIssue")
    public AsyncPath(
            final @NotNull List<Node> emptyNodeList,
            final @NotNull Set<BlockPos> positions,
            final @NotNull Supplier<Path> pathSupplier,
            final @NotNull Runnable postSupplier
    ) {
        super(emptyNodeList, null, false);

        this.nodes = emptyNodeList;
        this.positions = positions;
        this.pathSupplier = pathSupplier;
        this.postSupplier = postSupplier;

        AsyncPathProcessor.queue(this);
    }

    public synchronized void process() {
        if (this.processed) return;

        final Path best = this.pathSupplier.get();
        this.postSupplier.run();

        this.nodes.addAll(best.nodes);
        this.target = best.getTarget();
        this.distToTarget = best.getDistToTarget();
        this.canReach = best.canReach();
        this.processed = true;

        for (Runnable runnable : this.postProcessors) runnable.run();
    }

    public synchronized void postProcessing(final @NotNull Runnable runnable) {
        if (this.processed) runnable.run();
        else this.postProcessors.add(runnable);
    }

    private void checkProcessed() {
        if (!this.processed) this.process();
    }

    public boolean hasSamePosition(final Set<BlockPos> positions) {
        if (this.positions.size() != positions.size()) return false;
        return this.positions.containsAll(positions);
    }

    @Override
    public boolean isProcessed() {
        return this.processed;
    }

    @Override
    public boolean isDone() {
        return this.isProcessed() && super.isDone();
    }

    @Override
    public BlockPos getTarget() {
        this.checkProcessed();
        return this.target;
    }

    @Override
    public float getDistToTarget() {
        this.checkProcessed();
        return this.distToTarget;
    }

    @Override
    public boolean canReach() {
        this.checkProcessed();
        return this.canReach;
    }

    @Override
    public void advance() {
        this.checkProcessed();
        super.advance();
    }

    @Override
    public boolean notStarted() {
        this.checkProcessed();
        return super.notStarted();
    }

    @Override
    @Nullable
    public Node getEndNode() {
        this.checkProcessed();
        return super.getEndNode();
    }

    @Override
    public Node getNode(int index) {
        this.checkProcessed();
        return super.getNode(index);
    }

    @Override
    public void truncateNodes(int index) {
        this.checkProcessed();
        super.truncateNodes(index);
    }

    @Override
    public void replaceNode(int index, Node node) {
        this.checkProcessed();
        super.replaceNode(index, node);
    }

    @Override
    public int getNodeCount() {
        this.checkProcessed();
        return super.getNodeCount();
    }

    @Override
    public int getNextNodeIndex() {
        this.checkProcessed();
        return super.getNextNodeIndex();
    }

    @Override
    public void setNextNodeIndex(int nodeIndex) {
        this.checkProcessed();
        super.setNextNodeIndex(nodeIndex);
    }

    @Override
    public Vec3 getEntityPosAtNode(Entity entity, int index) {
        this.checkProcessed();
        return super.getEntityPosAtNode(entity, index);
    }

    @Override
    public BlockPos getNodePos(int index) {
        this.checkProcessed();
        return super.getNodePos(index);
    }

    @Override
    public Vec3 getNextEntityPos(Entity entity) {
        this.checkProcessed();
        return super.getNextEntityPos(entity);
    }

    @Override
    public BlockPos getNextNodePos() {
        this.checkProcessed();
        return super.getNextNodePos();
    }

    @Override
    public Node getNextNode() {
        this.checkProcessed();
        return super.getNextNode();
    }

    @Override
    @Nullable
    public Node getPreviousNode() {
        this.checkProcessed();
        return super.getPreviousNode();
    }

    @Override
    public boolean hasNext() {
        this.checkProcessed();
        return super.hasNext();
    }

}