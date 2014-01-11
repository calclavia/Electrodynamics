package resonantinduction.archaic.crafting;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Advanced tiering of a crafting table adding advanced features such as visual crafting, and auto
 * crafting. Original idea by Infinite
 * 
 * @author DarkGuardsman
 */
public class BlockEngineeringTable extends BlockRI
{
	@SideOnly(Side.CLIENT)
	private Icon workbenchIconTop;
	@SideOnly(Side.CLIENT)
	private Icon workbenchIconFront;

	public BlockEngineeringTable()
	{
		super("craftingDesk");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2)
	{
		return par1 == 1 ? this.workbenchIconTop : (par1 == 0 ? Block.planks.getBlockTextureFromSide(par1) : (par1 != 2 && par1 != 4 ? this.blockIcon : this.workbenchIconFront));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(this.getTextureName() + "_side");
		this.workbenchIconTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
		this.workbenchIconFront = par1IconRegister.registerIcon(this.getTextureName() + "_front");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEngineeringTable();
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if (metadata >= 0 && metadata < CraftingTables.values().length)
		{
			CraftingTables table = CraftingTables.values()[metadata];
			if (table.enabled && table.tileClass != null)
			{
				try
				{
					return table.tileClass.newInstance();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return super.createTileEntity(world, metadata);
	}

	public static enum CraftingTables
	{
		/** Upgraded wooden crafting table, stores items, and contains a small inventory */
		ADVANCED(),
		/** Stone version of the advanced, renders end result of crafting on surface */
		STONE(),
		/** Iron version of stone adding ability to repair armor */
		IRON(),
		/** Upgraded version of the iron allowing repair of tools, as well upgrades of tools */
		STEEL(),
		/** First work bench that can be combined with robotics to auto craft items */
		AUTOMATED(),
		/** Crafting table that allows large long term projects to be made one item at a time */
		PROJECT(),
		/** Crafting table that allows industrial machines to be crafted */
		INDUSTRIAL(),
		/**
		 * Crafting table that allows advanced electronics to be crafted, also allows electronic
		 * devices to be designed
		 */
		ELECTRONICS(),
		/** Upgraded auto table that can use blueprints, works faster, and do some cool tricks */
		INDUSTRIAL_AUTOMATED(),
		/** Auto crafting version of the electronics table, allows autocrafting of custom devices */
		ELECTRONICS_AUTOMATED(),
		/** Auto crafting table that allows long term projects to be made one item at a time */
		AUTOMATED_PROJECT();

		public final boolean enabled;
		public Class<? extends TileEntity> tileClass = null;

		private CraftingTables()
		{
			this(false);
		}

		private CraftingTables(boolean enabled)
		{
			this.enabled = enabled;
		}
	}

}
