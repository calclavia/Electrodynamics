package resonantinduction.archaic.filter;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.imprint.ItemImprint;
import resonantinduction.core.prefab.imprint.TileFilterable;
import resonantinduction.core.render.RenderItemOverlayTile;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import universalelectricity.api.vector.Vector3;
import calclavia.api.resonantinduction.IFilterable;
import calclavia.api.resonantinduction.recipe.MachineRecipes;
import calclavia.api.resonantinduction.recipe.RecipeResource;
import calclavia.api.resonantinduction.recipe.MachineRecipes.RecipeType;
import calclavia.lib.content.module.TileRender;
import calclavia.lib.network.Synced.SyncedInput;
import calclavia.lib.network.Synced.SyncedOutput;
import calclavia.lib.prefab.vector.Cuboid;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileFilter extends TileFilterable implements IFilterable
{
	public TileFilter()
	{
		super(Material.iron);
		maxSlots = 1;
		isOpaqueCube = false;
		normalRender = false;
	}

	public Iterable<Cuboid> getCollisionBoxes(Cuboid intersect, Entity entity)
	{
		if (entity == null)
			return null;

		if (entity instanceof EntityItem)
		{
			if (isFiltering(((EntityItem) entity).getEntityItem()))
			{
				return null;
			}
		}

		return super.getCollisionBoxes(intersect, entity);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (ticks % 60 == 0)
		{
			/**
			 * Toggle item filter render
			 */
			List<ItemStack> filteredStacks = ItemImprint.getFilterList(getFilter());

			if (filteredStacks.size() > 0)
				renderIndex = (renderIndex + 1) % filteredStacks.size();

			/**
			 * Fluid filters
			 */
			Vector3 position = new Vector3(this);
			Vector3 checkAbove = position.clone().translate(ForgeDirection.UP);
			Vector3 checkBelow = position.clone().translate(ForgeDirection.DOWN);

			Block bAbove = Block.blocksList[checkAbove.getBlockID(worldObj)];
			Block bBelow = Block.blocksList[checkAbove.getBlockID(worldObj)];

			if (bAbove instanceof BlockFluidMixture && (worldObj.isAirBlock(checkBelow.intX(), checkBelow.intY(), checkBelow.intZ()) || checkBelow.getTileEntity(worldObj) instanceof IFluidHandler))
			{
				worldObj.spawnParticle("dripWater", xCoord + 0.5, yCoord, zCoord + 0.5, 0, 0, 0);

				/**
				 * Leak the fluid down.
				 */
				BlockFluidMixture fluidBlock = (BlockFluidMixture) bAbove;
				int amount = fluidBlock.getQuantaValue(worldObj, checkAbove.intX(), checkAbove.intY(), checkAbove.intZ());
				int leakAmount = 2;

				/**
				 * Drop item from fluid.
				 */
				for (RecipeResource resoure : MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, "dust" + LanguageUtility.capitalizeFirst(ResourceGenerator.mixtureToMaterial(fluidBlock.getFluid().getName()))))
				{
					InventoryUtility.dropItemStack(worldObj, checkAbove.clone().add(0.5), resoure.getItemStack().copy(), 0, 0);
				}

				// TODO: Check if this is correct?
				int remaining = amount - leakAmount;

				/**
				 * Remove liquid from top.
				 */
				fluidBlock.setQuanta(worldObj, checkAbove.intX(), checkAbove.intY(), checkAbove.intZ(), remaining);

				/**
				 * Add liquid to bottom.
				 */
				checkBelow.setBlock(worldObj, Block.waterMoving.blockID);
			}
		}
	}

	@SyncedOutput
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
	}

	@SyncedInput
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
	}

	private int renderIndex = 0;

	@SideOnly(Side.CLIENT)
	protected TileRender newRenderer()
	{
		return new TileRender()
		{
			@Override
			public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
			{
				if (isItem)
					return false;

				if (getFilter() != null)
				{
					List<ItemStack> filteredStacks = ItemImprint.getFilterList(getFilter());

					if (filteredStacks.size() > 0)
					{
						ItemStack renderStack = filteredStacks.get(renderIndex);
						RenderItemOverlayTile.renderItemOnSides(TileFilter.this, renderStack, position.x, position.y, position.z);
					}
				}
				
				return false;
			}
		};
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_ANNOTATION.getPacket(this);
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return slot == 0 && stack.getItem() instanceof ItemImprint;
	}

	@Override
	public void setFilter(ItemStack filter)
	{
		setInventorySlotContents(0, filter);
		markUpdate();
	}

	@Override
	public ItemStack getFilter()
	{
		return getStackInSlot(0);
	}
}
