/**
 * 
 */
package resonantinduction.base;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * @author Calclavia
 * 
 */
public class TileEntityBase extends TileEntity
{
	protected long ticks = 0;
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();
	public boolean doPacket = true;

	public void initiate()
	{

	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.ticks++ == 0)
		{
			this.initiate();
		}

		if (doPacket && !worldObj.isRemote)
		{
			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(this.getDescriptionPacket(), (Player) player);
			}
		}
	}
}
