package resonantinduction.mechanical.gear;

import calclavia.lib.prefab.block.BlockAdvanced;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import resonantinduction.mechanical.network.PartMechanical;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia
 * 
 */
public class PartGear extends PartMechanical implements IMechanical
{
	private int manualCrankTime = 0;

	@Override
	public void update()
	{
		if (!this.world().isRemote)
		{
			if (manualCrankTime > 0)
			{
				getNetwork().onReceiveEnergy(this, 20, 0.4f);
				manualCrankTime--;
			}
		}

		super.update();
	}

	@Override
	public float getResistance()
	{
		return 0.1f;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		System.out.println(world().isRemote + ": " + getNetwork());

		if (BlockAdvanced.isUsableWrench(player, player.getCurrentEquippedItem(), tile().xCoord, tile().yCoord, tile().zCoord))
		{
			if (player.isSneaking())
			{
				if (!world().isRemote)
				{
					setClockwise(!isClockwise());
					player.addChatMessage("Flipped gear to rotate " + (isClockwise() ? "clockwise" : "anticlockwise") + ".");
				}
			}
			else
			{

				this.manualCrankTime = 10;
			}
		}

		return false;
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemGear);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderGear.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z, frame);
		}
	}

	@Override
	public boolean isRotationInversed()
	{
		return true;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_gear";
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return this;
	}

}