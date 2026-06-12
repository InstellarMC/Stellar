package ca.spottedleaf.moonrise.patches.block_counting;

import ca.spottedleaf.moonrise.common.list.IBlockDataList;

public interface BlockCountingChunkSection {

    public boolean anyTickingBlocks(); // Stellar: PaperMC/Paper#11500 - Lazy initialization of IBlockDataList

    public int moonrise$getSpecialCollidingBlocks();

    public IBlockDataList moonrise$getTickingBlockList();

}
