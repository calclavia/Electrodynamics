/**
 *
 */
package resonantinduction.electrical.battery;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonant.lib.utility.WorldUtility;
import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TODO: Make this more efficient.
 *
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static RenderBattery INSTANCE = new RenderBattery();
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "battery/battery.tcn");

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		glPushMatrix();
		GL11.glTranslated(0, 0, 0);
		int energyLevel = (int) (((double) ((ItemBlockBattery) itemStack.getItem()).getEnergy(itemStack) / (double) ((ItemBlockBattery) itemStack.getItem()).getEnergyCapacity(itemStack)) * 8);
		RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "battery/battery.png");

		List<String> disabledParts = new ArrayList<String>();
		disabledParts.addAll(Arrays.asList(new String[] { "connector", "connectorIn", "connectorOut" }));
		disabledParts.addAll(Arrays.asList(new String[] { "coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8" }));
		disabledParts.addAll(Arrays.asList(new String[] { "coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit" }));
		disabledParts.addAll(Arrays.asList(new String[] { "frame1con", "frame2con", "frame3con", "frame4con" }));
		MODEL.renderAllExcept(disabledParts.toArray(new String[0]));

		for (int i = 1; i <= 8; i++)
		{
			if (i != 1 || !disabledParts.contains("coil1"))
			{
				if ((8 - i) <= energyLevel)
					MODEL.renderOnly("coil" + i + "lit");
				else
					MODEL.renderOnly("coil" + i);
			}
		}

		glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		final String[][] partToDisable = new String[][] { new String[] { "bottom" }, new String[] { "top" }, new String[] { "frame1", "frame2" }, new String[] { "frame3", "frame4" }, new String[] { "frame4", "frame1" }, new String[] { "frame2", "frame3" } };
		final String[][] connectionPartToEnable = new String[][] { null, null, new String[] { "frame1con", "frame2con" }, new String[] { "frame3con", "frame4con" }, new String[] { "frame4con", "frame1con" }, new String[] { "frame2con", "frame3con" } };
		// final String[][] connectionPartSideToEnable = new String[][] { null, null, new String[] {
		// "frame1conSide", "frame2conSide" }, new String[] { "frame3conSide", "frame4conSide" },
		// new String[] { "frame4conSide", "frame1conSide" }, new String[] { "frame2conSide",
		// "frame3conSide" } };

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		TileBattery tile = (TileBattery) t;

		int energyLevel = (int) Math.round(((double) tile.getEnergyHandler().getEnergy() / (double) TileBattery.getEnergyForTier(tile.getBlockMetadata())) * 8);
		RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "battery/battery.png");

		List<String> disabledParts = new ArrayList();
		List<String> enabledParts = new ArrayList();

		for (ForgeDirection check : ForgeDirection.VALID_DIRECTIONS)
		{
			if (new Vector3(t).translate(check).getTileEntity(t.worldObj) instanceof TileBattery)
			{
				disabledParts.addAll(Arrays.asList(partToDisable[check.ordinal()]));

				if (check == ForgeDirection.UP)
				{
					enabledParts.addAll(Arrays.asList(partToDisable[check.ordinal()]));
					enabledParts.add("coil1");
				}
				else if (check == ForgeDirection.DOWN)
				{
					List<String> connectionParts = new ArrayList<String>();

					for (ForgeDirection sideCheck : ForgeDirection.VALID_DIRECTIONS)
						if (sideCheck.offsetY == 0)
							connectionParts.addAll(Arrays.asList(connectionPartToEnable[sideCheck.ordinal()]));

					for (ForgeDirection sideCheck : ForgeDirection.VALID_DIRECTIONS)
					{
						if (sideCheck.offsetY == 0)
						{
							if (new Vector3(t).translate(sideCheck).getTileEntity(t.worldObj) instanceof TileBattery)
							{
								connectionParts.removeAll(Arrays.asList(connectionPartToEnable[sideCheck.ordinal()]));
								// connectionParts.addAll(Arrays.asList(connectionPartSideToEnable[sideCheck.ordinal()]));
							}
						}
					}

					enabledParts.addAll(connectionParts);
				}
			}

			/**
			 * Render IO interface.
			 */
			if (check.offsetY == 0)
			{
				GL11.glPushMatrix();
				RenderUtility.rotateBlockBasedOnDirection(check);

				//TODO: Fix this horrible patch.
				switch (check)
				{
					case NORTH:
						glRotatef(0, 0, 1, 0);
						break;
					case SOUTH:
						glRotatef(0, 0, 1, 0);
						break;
					case WEST:
						glRotatef(-180, 0, 1, 0);
						break;
					case EAST:
						glRotatef(180, 0, 1, 0);
						break;
				}

				GL11.glRotatef(-90, 0, 1, 0);

				int io = tile.getIO(check);

				if (io == 1)
				{
					MODEL.renderOnly("connectorIn");
				}
				else if (io == 2)
				{
					MODEL.renderOnly("connectorOut");
				}

				GL11.glPopMatrix();
			}
		}

		enabledParts.removeAll(disabledParts);

		for (int i = 1; i <= 8; i++)
		{
			if (i != 1 || enabledParts.contains("coil1"))
			{
				if ((8 - i) < energyLevel)
					MODEL.renderOnly("coil" + i + "lit");
				else
					MODEL.renderOnly("coil" + i);
			}
		}

		disabledParts.addAll(Arrays.asList(new String[] { "connector", "connectorIn", "connectorOut" }));
		disabledParts.addAll(Arrays.asList(new String[] { "coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8" }));
		disabledParts.addAll(Arrays.asList(new String[] { "coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit" }));
		disabledParts.addAll(Arrays.asList(new String[] { "frame1con", "frame2con", "frame3con", "frame4con" }));

		enabledParts.removeAll(Arrays.asList(new String[] { "coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8" }));
		MODEL.renderAllExcept(disabledParts.toArray(new String[0]));
		MODEL.renderOnly(enabledParts.toArray(new String[0]));

		GL11.glPopMatrix();
	}
}
