package resonantinduction.mechanical.logistic.belt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.api.IFilterable;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.imprint.ItemImprint;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.content.module.TileRender;
import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.network.Synced.SyncedInput;
import calclavia.lib.network.Synced.SyncedOutput;
import calclavia.lib.prefab.vector.Cuboid;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileSorter extends TileInventory
{
	private boolean isInverted = false;

	public TileSorter()
	{
		super(UniversalElectricity.machine);
		textureName = "material_metal_side";
		maxSlots = 12;
		normalRender = false;
		isOpaqueCube = false;
		bounds = Cuboid.full().expand(-0.1);
	}

	@Override
	public boolean use(EntityPlayer player, int side, Vector3 vector3)
	{
		return interactCurrentItem(side, player);
	}

	protected boolean configure(EntityPlayer player, int side, Vector3 vector3)
	{
		isInverted = !isInverted;

		if (world().isRemote)
		{
			player.addChatMessage("Sorter filter inversion: " + !isInverted);
		}

		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		if (slot < 6)
			return stack.getItem() instanceof ItemImprint;
		return true;
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return true;
	}

	@Override
	public void collide(Entity entity)
	{
		if (!world().isRemote)
		{
			if (entity instanceof EntityItem)
			{
				EntityItem entityItem = (EntityItem) entity;
				sortItem(entityItem.getEntityItem());
				entityItem.setDead();
			}
		}
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if (i < 6)
			return this.getInventory().getStackInSlot(i);
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack)
	{
		if (i >= 6)
		{
			if (itemStack != null)
				sortItem(itemStack);
			return;
		}

		super.setInventorySlotContents(i, itemStack);
	}

	public void sortItem(ItemStack sortStack)
	{
		List<ForgeDirection> possibleDirections = new ArrayList<ForgeDirection>();

		/**
		 * Move item to position where a filter allows it.
		 */
		for (int i = 0; i < 6; i++)
		{
			ItemStack stack = getStackInSlot(i);

			if (!isInverted == ItemImprint.isFiltering(stack, sortStack))
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);

				int blockID = position().translate(dir).getBlockID(world());
				Block block = Block.blocksList[blockID];

				if (block == null || !block.isNormalCube(blockID))
				{
					possibleDirections.add(dir);
				}
			}
		}

		if (possibleDirections.size() == 0)
		{
			List<ForgeDirection> inventoryDirections = new ArrayList<ForgeDirection>();

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);

				int blockID = position().translate(dir).getBlockID(world());
				Block block = Block.blocksList[blockID];

				if (block == null || !block.isNormalCube(blockID))
				{
					possibleDirections.add(dir);
				}

				if (position().translate(dir).getTileEntity(world()) instanceof IInventory)
				{
					inventoryDirections.add(dir);
				}
			}

			if (inventoryDirections.size() > 0)
				possibleDirections = inventoryDirections;
		}

		int size = possibleDirections.size();

		ForgeDirection dir = possibleDirections.get(size > 1 ? world().rand.nextInt(size - 1) : 0);

		Vector3 spawn = center().translate(dir, 1);

		TileEntity tile = spawn.getTileEntity(world());
		ItemStack remain = sortStack;

		if (tile instanceof IInventory)
			remain = InventoryUtility.putStackInInventory((IInventory) tile, remain, dir.ordinal(), false);

		if (remain != null)
		{
			if (!world().isRemote)
				InventoryUtility.dropItemStack(world(), spawn, remain, 20, 0);

			remain = null;
		}

	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return new int[] { side + 6 };
	}

	@Override
	public Cuboid getSelectBounds()
	{
		return Cuboid.full();
	}

	@SyncedInput
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		nbt.getBoolean("isInverted");
	}

	@SyncedOutput
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isInverted", isInverted);
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected TileRender newRenderer()
	{
		return new TileRender()
		{
			final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "sorter.tcn");
			final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "sorter.png");

			@Override
			public boolean renderStatic(RenderBlocks renderer, Vector3 position)
			{
				return true;
			}

			@Override
			public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
			{
				GL11.glPushMatrix();
				RenderUtility.enableBlending();
				GL11.glTranslated(position.x + 0.5, position.y + 0.5, position.z + 0.5);
				RenderUtility.bind(TEXTURE);

				if (!isItem)
				{
					for (int i = 0; i < TileSorter.this.getSizeInventory(); i++)
					{
						if (TileSorter.this.getStackInSlot(i) != null)
						{
							ForgeDirection dir = ForgeDirection.getOrientation(i);
							GL11.glPushMatrix();

							if (dir.ordinal() == 0)
								GL11.glRotatef(-90, 0, 0, 1);

							if (dir.ordinal() == 1)
								GL11.glRotatef(90, 0, 0, 1);

							RenderUtility.rotateBlockBasedOnDirection(dir);
							MODEL.renderOnly("port");
							GL11.glPopMatrix();
						}
					}
				}

				MODEL.renderAllExcept("port");
				RenderUtility.disableBlending();
				GL11.glPopMatrix();
				return true;
			}

		};
	}
}
