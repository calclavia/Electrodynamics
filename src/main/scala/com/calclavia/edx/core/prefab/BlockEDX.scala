package com.calclavia.edx.core.prefab

import com.calclavia.edx.core.CategoryEDX
import nova.core.block.Block

/**
 * @author Calclavia
 */
abstract class BlockEDX extends Block {
	protected val category = add(new CategoryEDX)
}
