package resonantinduction.atomic.machine.plasma;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonant.lib.render.RenderTaggedTile;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPlasmaHeater extends RenderTaggedTile
{
    public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain(), Reference.modelPath() + "fusionReactor.tcn"));
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.domain(), Reference.modelPath() + "fusionReactor.png");

    @Override
    public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
    {
        TilePlasmaHeater tileEntity = (TilePlasmaHeater) t;

        if (tileEntity.world() != null)
        {
            super.renderTileEntityAt(t, x, y, z, f);
        }


    }
}