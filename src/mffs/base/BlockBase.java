package mffs.base;

import mffs.MFFSCreativeTab;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.block.material.Material;
import universalelectricity.prefab.block.BlockTile;

public class BlockBase extends BlockTile
{
	public BlockBase(int id, String name, Material material)
	{
		super(Settings.CONFIGURATION.getBlock(name, id).getInt(id), material);
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
		this.func_111022_d(ModularForceFieldSystem.PREFIX + name);
	}
}
