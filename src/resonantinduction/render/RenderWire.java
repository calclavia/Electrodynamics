package resonantinduction.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.model.ModelCopperWire;
import resonantinduction.wire.TileEntityWire;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TODO: Use ISBRH.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderWire extends TileEntitySpecialRenderer
{
	private static final ResourceLocation COPPER_TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "wireCopper.png");
	public static final ModelCopperWire WIRE_MODEL = new ModelCopperWire();

	public void renderModelAt(TileEntityWire tileEntity, double x, double y, double z, float f)
	{
		// Texture file
		FMLClientHandler.instance().getClient().renderEngine.func_110577_a(COPPER_TEXTURE);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);

		tileEntity.adjacentConnections = null;
		TileEntity[] adjacentConnections = tileEntity.getAdjacentConnections();

		if (adjacentConnections[0] != null)
		{
			WIRE_MODEL.renderBottom();
		}

		if (adjacentConnections[1] != null)
		{
			WIRE_MODEL.renderTop();
		}

		if (adjacentConnections[2] != null)
		{
			WIRE_MODEL.renderBack();
		}

		if (adjacentConnections[3] != null)
		{
			WIRE_MODEL.renderFront();
		}

		if (adjacentConnections[4] != null)
		{
			WIRE_MODEL.renderLeft();
		}

		if (adjacentConnections[5] != null)
		{
			WIRE_MODEL.renderRight();
		}

		WIRE_MODEL.renderMiddle();
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
	{
		this.renderModelAt((TileEntityWire) tileEntity, var2, var4, var6, var8);
	}
}