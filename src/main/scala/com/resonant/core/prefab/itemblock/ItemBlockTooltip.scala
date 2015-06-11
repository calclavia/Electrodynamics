package com.resonant.core.prefab.itemblock

import nova.core.block.BlockFactory
import nova.core.item.ItemBlock

class ItemBlockTooltip(blockFactory: BlockFactory) extends ItemBlock(blockFactory) with TooltipItem {
}