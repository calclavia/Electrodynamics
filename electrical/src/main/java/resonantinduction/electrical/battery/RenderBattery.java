/**
 * 
 */
package resonantinduction.electrical.battery;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import javax.naming.directory.DirContext;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.common.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static RenderBattery INSTANCE = new RenderBattery();
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "battery/battery.tcn");

	//Renders only if another battery on this side exists
	public static String[][] renderForSideOnly = new String[][] { new String[] {  }, new String[] {  }, new String[] { "frame1", "frame2" }, new String[] { "frame2", "frame3" }, new String[] { "frame3", "frame4" }, new String[] { "frame4", "frame5" } };
	
	//Renders only if another battery on this side does NOT exist
	public static String[][] renderNotForSide = new String[][] { new String[] {"bottom", "coil1"}, new String[] { "top", "frame1con", "frame2con", "frame3con", "frame4con" }, new String[] { }, new String[] {  }, new String[] {  }, new String[] {  } };

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		glPushMatrix();
		GL11.glTranslated(0, 0, 0);
		int energyLevel = (int) (((double) ((ItemBlockBattery) itemStack.getItem()).getEnergy(itemStack) / (double) ((ItemBlockBattery) itemStack.getItem()).getEnergyCapacity(itemStack)) * 8);
		RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "battery/battery_" + energyLevel + ".png");
		MODEL.renderAllExcept("frame1con", "frame2con", "frame3con", "frame4con");
		glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
	
		renderForSideOnly = new String[][] { new String[] { "frame1con", "frame2con", "frame3con", "frame4con" }, new String[] {  }, new String[] { }, new String[] {  }, new String[] { }, new String[] {  } };
		renderNotForSide = new String[][] { new String[] {"bottom", "coil1"}, new String[] { "top" }, new String[] { "frame1" }, new String[] { "frame3"  }, new String[] { "frame2" }, new String[] { "frame4" } };

		
		String[][] 	partToDisable = new String[][] { new String[] {"bottom", "coil1"}, new String[] { "top","frame1con" ,"frame2con" ,"frame3con" ,"frame4con" }, new String[] {  }, new String[] {   }, new String[] {  }, new String[] {  } };

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		TileBattery tile = (TileBattery) t;

		int energyLevel = (int) (((double) tile.energy.getEnergy() / (double) TileBattery.getEnergyForTier(tile.getBlockMetadata())) * 8);
		RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "battery/battery_" + energyLevel + ".png");

		String[] disabledParts = new String[0];
		
		for (ForgeDirection check : ForgeDirection.VALID_DIRECTIONS)
		{
			if (new Vector3(t).translate(check).getTileEntity(t.worldObj) instanceof TileBattery)
			{
				disabledParts = ArrayUtils.addAll(disabledParts, partToDisable[check.ordinal()]);
			}
		}

		GL11.glColor3f(1, 1, 1);
		MODEL.renderAllExcept(disabledParts);

		GL11.glPopMatrix();
	}
}
