package com.builtbroken.minecraft.prefab;

import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.Reference;
import resonantinduction.core.Settings;
import resonatninduction.tilenetwork.INetworkPart;
import calclavia.lib.access.AccessUser;
import calclavia.lib.access.ISpecialAccess;
import calclavia.lib.multiblock.link.IBlockActivate;
import calclavia.lib.prefab.block.BlockTile;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.IExtraInfo.IExtraBlockInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Basic TileEntity Container class designed to be used by generic machines. It is suggested that
 * each mod using this create there own basic block extending this to reduce need to use build data
 * per block.
 * 
 * @author Darkguardsman
 */
public abstract class BlockMachine extends BlockTile implements IExtraBlockInfo
{

	public boolean zeroAnimation, zeroSound, zeroRendering;
	public int guiID = -1;

	public Icon iconInput, iconOutput;

	public BlockMachine(Configuration config, String blockName, Material material)
	{
		super(config.getBlock(blockName, Settings.getNextBlockID()).getInt(), material);
		this.setUnlocalizedName(blockName);
		this.setResistance(100f);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconReg)
	{
		this.blockIcon = iconReg.registerIcon(Reference.PREFIX + "machine");
		this.iconInput = iconReg.registerIcon(Reference.PREFIX + "machine_input");
		this.iconOutput = iconReg.registerIcon(Reference.PREFIX + "machine_output");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return this.blockIcon;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return this.zeroRendering;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsNormalBlock()
	{
		return this.zeroRendering;
	}

	/** Called whenever the block is added into the world. Args: world, x, y, z */
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity instanceof INetworkPart)
		{
			((INetworkPart) tileEntity).refresh();
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof ISpecialAccess && entity instanceof EntityPlayer)
		{
			((ISpecialAccess) tile).setUserAccess(new AccessUser((EntityPlayer) entity), ((ISpecialAccess) tile).getOwnerGroup());
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		super.onNeighborBlockChange(world, x, y, z, blockID);
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof INetworkPart)
		{
			((INetworkPart) tileEntity).refresh();
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		super.breakBlock(world, x, y, z, par5, par6);
		world.notifyBlockChange(x, y, z, world.getBlockId(x, y, z));
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side)
	{
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof IInventory)
		{
			return Container.calcRedstoneFromInventory((IInventory) entity);
		}
		return 0;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return super.createTileEntity(world, metadata);
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{

	}

	@Override
	public boolean hasExtraConfigs()
	{
		return true;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		this.zeroAnimation = config.get("Effects--Not_Supported_By_All_Blocks", "disableAnimation", false, "Turns off animations of the block").getBoolean(false);
		this.zeroRendering = config.get("Effects--Not_Supported_By_All_Blocks", "disableRender", false, "Turns off the block render replacing it with a normal block").getBoolean(false);
		this.zeroSound = config.get("Effects--Not_Supported_By_All_Blocks", "disableSound", false, "Turns of sound of the block for any of its actions").getBoolean(false);
	}

	@Override
	public void loadOreNames()
	{
		OreDictionary.registerOre(this.getUnlocalizedName().replace("tile.", ""), this);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof IBlockActivate && ((IBlockActivate) entity).onActivated(entityPlayer))
		{
			return true;
		}
		return super.onBlockActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
	}

}
