package resonantinduction.old.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.prefab.tile.TileEntityMachine;
import resonantinduction.old.client.model.ModelMachine;
import resonantinduction.old.client.model.ModelSteamGen;
import resonantinduction.old.client.model.ModelSteamTurbine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSteamGen extends TileEntitySpecialRenderer
{
	public static final ModelSteamTurbine TURBINE_MODEL = new ModelSteamTurbine();
	public static final ModelSteamGen STEAM_GEN_MODEL = new ModelSteamGen();

	public static final ResourceLocation TURBINE_TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "SmallSteamFan.png");
	public static final ResourceLocation STEAM_GEN_TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "SteamGenerator.png");
	private static float rot1 = 0;

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float size)
	{
		int meta = tileEntity.worldObj.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
		int face = meta % 4;
		ModelMachine model = getModel(meta);

		if (model != null)
		{
			bindTexture(RenderSteamGen.getTexture(meta));
			rot1 = MathHelper.wrapAngleTo180_float(rot1 + 1);

			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);

			if (face == 0)
			{
				GL11.glRotatef(180f, 0f, 1f, 0f);
			}
			if (face == 2)
			{
				GL11.glRotatef(0f, 0f, 1f, 0f);
			}
			else if (face == 3)
			{
				GL11.glRotatef(90f, 0f, 1f, 0f);
			}
			else if (face == 1)
			{
				GL11.glRotatef(270f, 0f, 1f, 0f);
			}

			model.render(0.0625F);

			if (tileEntity instanceof TileEntityMachine)
			{
				if (model instanceof ModelSteamTurbine)
				{
					if (((TileEntityMachine) tileEntity).isFunctioning())
					{
						GL11.glRotatef(RenderSteamGen.rot1, 0f, 1f, 0f);
					}
					((ModelSteamTurbine) model).renderFan(0.0625F);
				}
			}

			GL11.glPopMatrix();
		}

	}

	public static ModelMachine getModel(int meta)
	{
		switch (meta / 4)
		{

			case 0:
				return TURBINE_MODEL;
			case 1:
				return TURBINE_MODEL;
			case 2:
				return STEAM_GEN_MODEL;
			case 3:
				return STEAM_GEN_MODEL;
		}
		return null;
	}

	public static ResourceLocation getTexture(int meta)
	{
		switch (meta / 4)
		{

			case 0:
				return TURBINE_TEXTURE;
			case 1:
				return TURBINE_TEXTURE;
			case 2:
				return STEAM_GEN_TEXTURE;
			case 3:
				return STEAM_GEN_TEXTURE;
		}
		return null;
	}

}