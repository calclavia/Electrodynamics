package resonantinduction.core.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.utility.WorldUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class RenderItemOverlayTile extends TileEntitySpecialRenderer
{
	private static final ForgeDirection[] forge_sides = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

	private static RenderBlocks renderBlocks;
	private static RenderItem renderItem;

	public RenderItemOverlayTile()
	{
		super();
		renderItem = ((RenderItem) RenderManager.instance.getEntityClassRenderObject(EntityItem.class));
		renderBlocks = new RenderBlocks();
	}

	public void renderTopOverlay(TileEntity tileEntity, ItemStack[] inventory, ForgeDirection dir, double x, double y, double z)
	{
		renderTopOverlay(tileEntity, inventory, dir, 3, 3, x, y, z);
	}

	public static void renderTopOverlay(TileEntity tileEntity, ItemStack[] inventory, ForgeDirection dir, int matrixX, int matrixZ, double x, double y, double z)
	{
		GL11.glPushMatrix();

		/** Render the Crafting Matrix */
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		boolean isLooking = false;

		MovingObjectPosition objectPosition = player.rayTrace(8, 1);

		if (objectPosition != null)
		{
			isLooking = objectPosition.blockX == tileEntity.xCoord && objectPosition.blockY == tileEntity.yCoord && objectPosition.blockZ == tileEntity.zCoord;
		}

		for (int i = 0; i < (matrixX * matrixZ); i++)
		{
			if (inventory[i] != null)
			{
				Vector3 translation = new Vector3((double) (i / matrixX) / ((double) matrixX) + (0.5 / (matrixX)), 1.1, (double) (i % matrixZ) / ((double) matrixZ) + (0.5 / (matrixZ))).translate(-0.5);
				translation.scale(0.9);
				translation.translate(0, 0, 0.06);
				GL11.glPushMatrix();
				GL11.glTranslated(x + 0.5f, y + 0.5f, z + 0.5f);
				RenderUtility.rotateBlockBasedOnDirection(dir);
				GL11.glTranslated(translation.x, translation.y, translation.z);
				GL11.glScalef(0.7f, 0.7f, 0.7f);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				renderItem(tileEntity.worldObj, ForgeDirection.UP, inventory[i], new Vector3(0, 0, 0), 0, 1);
				GL11.glPopMatrix();

				if (isLooking)
				{
					GL11.glPushMatrix();
					GL11.glTranslated(x, y, z);
					int angle = WorldUtility.getAngleFromForgeDirection(WorldUtility.invertX(dir));
					RenderUtility.renderFloatingText("" + inventory[i].stackSize, translation.rotate(angle, Vector3.UP()).translate(0.5).translate(0, 0.3, 0));
					GL11.glPopMatrix();
				}
			}
		}
		GL11.glPopMatrix();

	}

	public static void renderItemOnSides(TileEntity tile, ItemStack itemStack, double x, double y, double z)
	{
		renderItemOnSides(tile, itemStack, x, y, z, "No Output");
	}

	public static void renderItemOnSides(TileEntity tile, ItemStack itemStack, double x, double y, double z, String renderText)
	{
		/** Render the Output */
		String amount = "";

		if (itemStack != null)
		{
			renderText = itemStack.getDisplayName();
			amount = Integer.toString(itemStack.stackSize);
		}

		for (ForgeDirection direction : forge_sides)
		{
			if (tile.worldObj.isBlockSolidOnSide(tile.xCoord + direction.offsetX, tile.yCoord, tile.zCoord + direction.offsetZ, direction.getOpposite()))
			{
				continue;
			}
			renderItemOnSide(tile, itemStack, direction, x, y, z, renderText, amount);
			RenderUtility.renderText(renderText, direction, 0.02f, x, y - 0.35f, z);
			RenderUtility.renderText(amount, direction, 0.02f, x, y - 0.15f, z);
		}
	}

	protected static void renderItemSingleSide(TileEntity tile, double x, double y, double z, ItemStack itemStack, ForgeDirection direction, String renderText)
	{
		if (!tile.worldObj.isBlockSolidOnSide(tile.xCoord + direction.offsetX, tile.yCoord, tile.zCoord + direction.offsetZ, direction.getOpposite()))
		{
			String amount = "";

			if (itemStack != null)
			{
				renderText = itemStack.getDisplayName();
				amount = Integer.toString(itemStack.stackSize);
			}

			renderItemOnSide(tile, itemStack, direction, x, y, z, renderText, amount);
			RenderUtility.renderText(renderText, direction, 0.02f, x, y - 0.35f, z);
			RenderUtility.renderText(amount, direction, 0.02f, x, y - 0.15f, z);

		}
	}

	protected static void renderItemOnSide(TileEntity tile, ItemStack itemStack, ForgeDirection direction, double x, double y, double z, String renderText, String amount)
	{
		setupLight(tile, direction.offsetX, direction.offsetZ);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

		if (itemStack != null)
		{
			GL11.glPushMatrix();

			switch (direction)
			{
				case NORTH:
					GL11.glTranslated(x + 0.65, y + 0.9, z - 0.01);
					break;
				case SOUTH:
					GL11.glTranslated(x + 0.35, y + 0.9, z + 1.01);
					GL11.glRotatef(180, 0, 1, 0);
					break;
				case WEST:
					GL11.glTranslated(x - 0.01, y + 0.9, z + 0.35);
					GL11.glRotatef(90, 0, 1, 0);
					break;
				case EAST:
					GL11.glTranslated(x + 1.01, y + 0.9, z + 0.65);
					GL11.glRotatef(-90, 0, 1, 0);
					break;
			}

			float scale = 0.03125F;
			GL11.glScalef(0.6f * scale, 0.6f * scale, -0.00001f);
			GL11.glRotatef(180, 0, 0, 1);

			TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

			GL11.glDisable(2896);

			if (!ForgeHooksClient.renderInventoryItem(renderBlocks, renderEngine, itemStack, true, 0.0F, 0.0F, 0.0F))
			{
				renderItem.renderItemIntoGUI(Minecraft.getMinecraft().fontRenderer, renderEngine, itemStack, 0, 0);
			}

			GL11.glEnable(2896);
			GL11.glPopMatrix();
		}
	}

	private static void setupLight(TileEntity tileEntity, int xDifference, int zDifference)
	{
		World world = tileEntity.worldObj;

		if (world.isBlockOpaqueCube(tileEntity.xCoord + xDifference, tileEntity.yCoord, tileEntity.zCoord + zDifference))
		{
			return;
		}

		int br = world.getLightBrightnessForSkyBlocks(tileEntity.xCoord + xDifference, tileEntity.yCoord, tileEntity.zCoord + zDifference, 0);
		int var11 = br % 65536;
		int var12 = br / 65536;
		float scale = 0.6F;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var11 * scale, var12 * scale);
	}

	private static void renderItem(World world, ForgeDirection dir, ItemStack itemStack, Vector3 position, float rotationYaw, int angle)
	{
		if (itemStack != null)
		{
			EntityItem entityItem = new EntityItem(world, 0.0D, 0.0D, 0.0D, itemStack.copy());
			entityItem.getEntityItem().stackSize = 1;
			entityItem.hoverStart = 0.0F;
			GL11.glPushMatrix();
			GL11.glTranslated(-0.453125F * dir.offsetX + position.x, position.y, -0.453125F * dir.offsetZ + position.z);
			GL11.glRotatef(180.0F + rotationYaw, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(90 * angle, 1, 0, 0);

			RenderItem renderItem = ((RenderItem) RenderManager.instance.getEntityClassRenderObject(EntityItem.class));

			boolean fancyGraphics = RenderManager.instance.options.fancyGraphics;
			RenderManager.instance.options.fancyGraphics = true;
			renderItem.doRenderItem(entityItem, 0, 0, 0, 0, 0);
			RenderManager.instance.options.fancyGraphics = fancyGraphics;

			GL11.glPopMatrix();
		}
	}

}
