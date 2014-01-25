package resonantinduction.mechanical.belt;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.belt.TileConveyorBelt.SlantType;
import calclavia.lib.render.block.ICustomBlockRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderConveyorBelt extends TileEntitySpecialRenderer implements ICustomBlockRenderer
{
	public static final ModelConveyorBelt MODEL = new ModelConveyorBelt();
	public static final ModelAngledBelt MODEL2 = new ModelAngledBelt();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileConveyorBelt tileEntity = (TileConveyorBelt) t;
		SlantType slantType = tileEntity.getSlant();
		int face = tileEntity.getDirection().ordinal();

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glRotatef(180f, 0f, 0f, 1f);

		int frame = tileEntity.getAnimationFrame();

		if (slantType != null && slantType != SlantType.NONE)
		{
			switch (face)
			{
				case 2:
					GL11.glRotatef(180f, 0f, 1f, 0f);
					break;
				case 3:
					GL11.glRotatef(0f, 0f, 1f, 0f);
					break;
				case 4:
					GL11.glRotatef(90f, 0f, 1f, 0f);
					break;
				case 5:
					GL11.glRotatef(-90f, 0f, 1f, 0f);
					break;
			}

			if (slantType == SlantType.UP)
			{
				ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "slantedbelt/frame" + frame + ".png");
				bindTexture(name);

				GL11.glTranslatef(0f, 0.8f, -0.8f);
				GL11.glRotatef(180f, 0f, 1f, 1f);
				boolean slantAdjust = false;
				TileEntity test = tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord + tileEntity.getDirection().offsetX, tileEntity.yCoord, tileEntity.zCoord + tileEntity.getDirection().offsetZ);
				if (test != null)
				{
					if (test instanceof TileConveyorBelt)
					{
						if (((TileConveyorBelt) test).getSlant() == SlantType.TOP)
						{
							GL11.glRotatef(10f, 1f, 0f, 0f);
							slantAdjust = true;
						}
					}
				}
				MODEL2.render(0.0625F, true);
			}
			else if (slantType == SlantType.DOWN)
			{
				ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "slantedbelt/frame" + frame + ".png");
				bindTexture(name);
				GL11.glRotatef(180f, 0f, 1f, 0f);
				boolean slantAdjust = false;
				TileEntity test = tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord - tileEntity.getDirection().offsetX, tileEntity.yCoord, tileEntity.zCoord - tileEntity.getDirection().offsetZ);
				if (test != null)
				{
					if (test instanceof TileConveyorBelt)
					{
						if (((TileConveyorBelt) test).getSlant() == SlantType.TOP)
						{
							GL11.glRotatef(-10f, 1f, 0f, 0f);
							GL11.glTranslatef(0f, 0.25f, 0f);
							slantAdjust = true;
						}
					}
				}
				MODEL2.render(0.0625F, slantAdjust);
			}
			else
			{
				ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame" + frame + ".png");
				bindTexture(name);
				GL11.glRotatef(180, 0f, 1f, 0f);
				GL11.glTranslatef(0f, -0.68f, 0f);
				MODEL.render(0.0625f, (float) Math.toRadians(tileEntity.angle), false, false, false, false);
			}
		}
		else
		{
			switch (face)
			{
				case 2:
					GL11.glRotatef(0f, 0f, 1f, 0f);
					break;
				case 3:
					GL11.glRotatef(180f, 0f, 1f, 0f);
					break;
				case 4:
					GL11.glRotatef(-90f, 0f, 1f, 0f);
					break;
				case 5:
					GL11.glRotatef(90f, 0f, 1f, 0f);
					break;
			}
			ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame" + frame + ".png");
			bindTexture(name);
			MODEL.render(0.0625F, (float) Math.toRadians(tileEntity.angle), false, false, false, true);

		}

		int ent = tileEntity.worldObj.getBlockId(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventory(Block block, int metadata, int modelID, RenderBlocks renderer)
	{

	}

	@Override
	public boolean renderStatic(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		return false;
	}

	@Override
	public void renderDynamic(TileEntity tile, Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 1.7F, 0.5f);
		GL11.glRotatef(180f, 0f, 0f, 1f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame0.png"));
		MODEL.render(0.0625F, 0, false, false, false, false);
		GL11.glPopMatrix();
	}
}