package resonantinduction.mechanical.process.crusher;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMechanicalPiston extends TileEntitySpecialRenderer
{
    public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "piston/mechanicalPiston.tcn");
    public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "piston/mechanicalPiston_iron.png");

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        TileMechanicalPiston tile = (TileMechanicalPiston) tileEntity;

        GL11.glRotated(-90, 0, 1, 0);
        GL11.glRotated(180, 0, 0, 1);

        if (tile.worldObj != null)
        {
            if (tile.getDirection() != ForgeDirection.UP && tile.getDirection() != ForgeDirection.DOWN)
                RenderUtility.rotateBlockBasedOnDirection(tile.getDirection().getOpposite());
            else
                RenderUtility.rotateBlockBasedOnDirection(tile.getDirection());
        }

        RenderUtility.bind(TEXTURE);

        // Angle in radians of the rotor.
        double angle = tile.mechanicalNode.renderAngle;
        final String[] staticParts = { "baseRing", "leg1", "leg2", "leg3", "leg4", "connector", "basePlate", "basePlateTop", "connectorBar", "centerPiston" };
        final String[] shaftParts = { "topPlate", "outerPiston" };

        /** Render Piston Rotor */
        GL11.glPushMatrix();
        GL11.glRotated(-Math.toDegrees(angle), 0, 0, 1);
        MODEL.renderAllExcept(ArrayUtils.addAll(shaftParts, staticParts));
        GL11.glPopMatrix();

        /** Render Piston Shaft */
        GL11.glPushMatrix();

        if (tile.worldObj != null)
        {
            ForgeDirection dir = tile.getDirection();

            if (tile.world().isAirBlock(tile.x() + dir.offsetX, tile.y() + dir.offsetY, tile.z() + dir.offsetZ))
            {
                GL11.glTranslated(0, 0, (0.4 * Math.sin(angle)) - 0.5);
            }
            else
            {
                GL11.glTranslated(0, 0, (0.06 * Math.sin(angle)) - 0.03);
            }
        }

        MODEL.renderOnly(shaftParts);
        GL11.glPopMatrix();

        MODEL.renderOnly(staticParts);
        GL11.glPopMatrix();
    }
}