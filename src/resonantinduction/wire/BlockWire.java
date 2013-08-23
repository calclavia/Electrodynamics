package resonantinduction.wire;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import universalelectricity.prefab.block.BlockConductor;

/**
 * A copper wire block that can change its collision bounds based on the connection.
 * 
 * @author Calclavia, Aidancbrady
 */
public class BlockWire extends BlockConductor
{
	public BlockWire(int id)
	{
		super(ResonantInduction.CONFIGURATION.getBlock("wire", id).getInt(id), Material.cloth);
		this.setUnlocalizedName(ResonantInduction.PREFIX + "wire");
		this.setStepSound(soundClothFootstep);
		this.setResistance(0.2F);
		this.setHardness(0.1f);
		this.setBlockBounds(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
		this.setCreativeTab(CreativeTabs.tabRedstone);
		Block.setBurnProperties(this.blockID, 30, 60);
		this.func_111022_d(ResonantInduction.PREFIX + "wire");
		this.setCreativeTab(TabRI.INSTANCE);
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube? This determines whether or not to render the
	 * shared face of two adjacent blocks and also whether the player can attach torches, redstone
	 * wire, etc to this block.
	 */
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False (examples: signs,
	 * buttons, stairs, etc)
	 */
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityTickWire();
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < EnumWireMaterial.values().length; i++)
		{
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

}