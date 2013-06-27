package mffs.api;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;

/**
 * Adds blocks to this black list if you do not wish them to be moved by the force manipulator.
 * @author User
 *
 */
public class ForceManipulatorBlacklist
{
	public static final Set<Block> blackList = new HashSet<Block>();
}
