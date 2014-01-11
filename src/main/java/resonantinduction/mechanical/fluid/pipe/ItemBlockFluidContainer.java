package resonantinduction.mechanical.fluid.pipe;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetworkTile;
import resonantinduction.mechanical.fluid.tank.TileTank;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.energy.UnitDisplay.UnitPrefix;
import universalelectricity.api.vector.Vector3;

public class ItemBlockFluidContainer extends ItemBlock
{
	public ItemBlockFluidContainer(int id)
	{
		super(id);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("fluid"))
		{
			FluidStack fluid = FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("fluid"));

			if (fluid != null)
			{
				list.add("Fluid: " + fluid.getFluid().getName());
				list.add("Volume: " + UnitDisplay.getDisplay(fluid.amount, Unit.LITER, UnitPrefix.MILLI));
			}
		}
	}

	public static ItemStack getWrenchedItem(World world, Vector3 vec)
	{
		TileEntity entity = vec.getTileEntity(world);
		if (entity instanceof TileTank && ((TileTank) entity).getTankInfo() != null && ((TileTank) entity).getTankInfo()[0] != null)
		{
			ItemStack itemStack = new ItemStack(Mechanical.blockTank);
			FluidStack stack = ((TileTank) entity).getTankInfo()[0].fluid;

			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			if (stack != null)
			{
				((TileTank) entity).drain(ForgeDirection.UNKNOWN, stack.amount, true);
				itemStack.getTagCompound().setCompoundTag("fluid", stack.writeToNBT(new NBTTagCompound()));
			}
			return itemStack;
		}
		return null;
	}

	@Override
	public void onUpdate(ItemStack itemStack, World par2World, Entity entity, int par4, boolean par5)
	{
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entity;

			if (itemStack.getTagCompound() != null && !player.capabilities.isCreativeMode && itemStack.getTagCompound().hasKey("fluid"))
			{
				player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 5, 0));
			}
		}
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("fluid"))
		{
			return 1;
		}

		return this.maxStackSize;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return Block.blocksList[this.getBlockID()].getUnlocalizedName() + "." + itemStack.getItemDamage();
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		if (super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, (stack.getItemDamage() / FluidContainerMaterial.spacing)))
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);
			if (tile instanceof TileFluidNetworkTile)
			{
				((TileFluidNetworkTile) tile).setSubID(stack.getItemDamage());
				if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("fluid"))
				{
					((TileFluidNetworkTile) tile).fill(ForgeDirection.UNKNOWN, FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("fluid")), true);
				}
			}
			return true;
		}
		return false;
	}
}