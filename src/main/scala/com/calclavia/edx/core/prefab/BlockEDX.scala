package com.calclavia.edx.core.prefab

import com.calclavia.edx.core.CategoryEDX
import nova.core.block.Block
import nova.core.component.misc.Collider

/**
 * @author Calclavia
 */
abstract class BlockEDX extends Block {
	protected val category = add(new CategoryEDX)
	protected val collider = add(new Collider)
}
