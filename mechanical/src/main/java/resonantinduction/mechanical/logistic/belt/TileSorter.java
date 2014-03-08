package resonantinduction.mechanical.logistic.belt;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileSorter extends TileInventory
{
	private boolean isInverted = false;

	public TileSorter()
	{
		super(UniversalElectricity.machine);
		textureName = "material_metal_side";
		maxSlots = 6;
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
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return stack.getItem() instanceof IFilterable;
	}

	@Override
	public void collide(Entity entity)
	{
		if (!world().isRemote)
		{
			if (entity instanceof EntityItem)
			{
				EntityItem entityItem = (EntityItem) entity;
				List<ForgeDirection> possibleDirections = new ArrayList<ForgeDirection>();

				/**
				 * Move item to position where a filter allows it.
				 */
				for (int i = 0; i < getSizeInventory(); i++)
				{
					ItemStack stack = getStackInSlot(i);

					if (isInverted == ItemImprint.isFiltering(stack, entityItem.getEntityItem()))
					{
						ForgeDirection dir = ForgeDirection.getOrientation(i);

						int blockID = position().translate(dir).getBlockID(world());
						Block block = Block.blocksList[blockID];

						if (block == null || block.isNormalCube(blockID))
						{
							possibleDirections.add(dir);
						}
					}
				}

				int size = possibleDirections.size();

				if (size > 0)
				{
					ForgeDirection dir = possibleDirections.get(size > 1 ? world().rand.nextInt(size - 1) : 0);
					Vector3 set = center().translate(dir, 1);
					entityItem.setPosition(set.x, set.y, set.z);
				}
			}
		}
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
	protected TileRender newRenderer()
	{
		return new TileRender()
		{
			final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "sorter.tcn");
			final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "sorter.png");

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
