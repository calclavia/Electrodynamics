package mffs.base;

import calclavia.lib.prefab.block.BlockTile;
import mffs.MFFSCreativeTab;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.block.material.Material;

public class BlockBase extends BlockTile
{
	public BlockBase(int id, String name, Material material)
	{
		super(Settings.CONFIGURATION.getBlock(name, id).getInt(id), material);
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
		this.setTextureName(ModularForceFieldSystem.PREFIX + name);
	}
}
