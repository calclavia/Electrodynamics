package resonantinduction.archaic.fluid.gutter;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import resonantinduction.archaic.fluid.tank.TileTank;
import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.FluidRenderUtility;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import calclavia.lib.utility.FluidUtility;
import calclavia.lib.utility.WorldUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGutter extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final RenderGutter INSTANCE = new RenderGutter();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gutter.tcn");
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "gutter.png");

	public static void render(int meta, byte sides)
	{
		RenderUtility.bind(TEXTURE);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (dir != ForgeDirection.UP && dir != ForgeDirection.DOWN)
			{
				if (!WorldUtility.isEnabledSide(sides, dir))
				{
					GL11.glPushMatrix();
					RenderUtility.rotateBlockBasedOnDirection(dir);
					MODEL.renderOnly("left", "backCornerL", "frontCornerL");
					GL11.glPopMatrix();
				}
			}
		}

		if (!WorldUtility.isEnabledSide(sides, ForgeDirection.DOWN))
		{
			MODEL.renderOnly("base");
		}
		else
		{
			GL11.glPushMatrix();
			GL11.glRotatef(-90, 0, 0, 1);
			MODEL.renderOnly("backCornerL", "frontCornerL");
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glRotatef(90, 0, 1, 0);
			GL11.glRotatef(-90, 0, 0, 1);
			MODEL.renderOnly("backCornerL", "frontCornerL");
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		TileGutter tile = ((TileGutter) tileEntity);

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		FluidStack liquid = tile.getInternalTank().getFluid();
		int capacity = tile.getInternalTank().getCapacity();
		byte renderSides = (tile instanceof TileGutter ? tile.renderSides : (byte) 0);

		render(0, renderSides);

		if (tileEntity.worldObj != null)
		{
			GL11.glPushMatrix();
			GL11.glScaled(0.99, 0.99, 0.99);
			FluidTank tank = ((TileGutter) tileEntity).getInternalTank();
			double percentageFilled = (double) tank.getFluidAmount() / (double) tank.getCapacity();

			double ySouthEast = FluidUtility.getAveragePercentageFilledForSides(percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.SOUTH, ForgeDirection.EAST);
			double yNorthEast = FluidUtility.getAveragePercentageFilledForSides(percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.NORTH, ForgeDirection.EAST);
			double ySouthWest = FluidUtility.getAveragePercentageFilledForSides(percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.SOUTH, ForgeDirection.WEST);
			double yNorthWest = FluidUtility.getAveragePercentageFilledForSides(percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.NORTH, ForgeDirection.WEST);

			FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest);
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(0.5, 0.5, 0.5);
		render(itemStack.getItemDamage(), Byte.parseByte("001100", 2));
		GL11.glPopMatrix();
	}
}