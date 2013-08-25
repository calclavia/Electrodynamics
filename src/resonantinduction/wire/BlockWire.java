package resonantinduction.wire;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import universalelectricity.core.block.IConductor;
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

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);
		TileEntityWire tileEntity = (TileEntityWire) t;

		if (entityPlayer.getCurrentEquippedItem() != null)
		{
			if (entityPlayer.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				tileEntity.setDye(entityPlayer.getCurrentEquippedItem().getItemDamage());
				return true;
			}
			else if (entityPlayer.getCurrentEquippedItem().itemID == Block.cloth.blockID)
			{
				tileEntity.setInsulated();
				tileEntity.setDye(BlockColored.getDyeFromBlock(entityPlayer.getCurrentEquippedItem().getItemDamage()));
				entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				return true;
			}
		}

		if (!world.isRemote)
		{
			if (entityPlayer.isSneaking())
			{
				/**
				 * Change the TileEntity type.
				 */
				NBTTagCompound nbt = new NBTTagCompound();
				tileEntity.writeToNBT(nbt);

				if (tileEntity instanceof TileEntityTickWire)
				{
					nbt.setString("id", this.getUnlocalizedName());
					entityPlayer.addChatMessage("Wire will now stop ticking and withdrawing energy.");
				}
				else
				{
					nbt.setString("id", this.getUnlocalizedName() + "2");
					entityPlayer.addChatMessage("Wire will now tick and withdraw energy.");
				}

				world.setBlockTileEntity(x, y, z, TileEntity.createAndLoadEntity(nbt));
				((IConductor) world.getBlockTileEntity(x, y, z)).refresh();
				world.markBlockForUpdate(x, y, z);
			}
		}

		return false;
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
		return new TileEntityWire();
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < EnumWireMaterial.values().length; i++)
		{
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);

		/**
		 * Drop wool insulation if the wire is insulated.
		 */
		if (t instanceof TileEntityWire)
		{
			TileEntityWire tileEntity = (TileEntityWire) t;

			if (tileEntity.isInsulated)
			{
				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(tileEntity.dyeID)));
			}
		}

		super.breakBlock(world, x, y, z, par5, par6);
	}
}