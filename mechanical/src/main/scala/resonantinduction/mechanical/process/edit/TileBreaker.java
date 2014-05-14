package resonantinduction.mechanical.process.edit;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IRotatable;
import resonant.lib.content.module.TileBase;
import resonant.lib.content.module.TileRender;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.render.RotatedTextureRenderer;
import resonant.lib.utility.inventory.InternalInventoryHandler;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class TileBreaker extends TileBase implements IRotatable, IPacketReceiver
{
	@SideOnly(Side.CLIENT)
	private static Icon iconFront, iconBack;
	private boolean doWork = false;
	private InternalInventoryHandler invHandler;
	private byte place_delay = 0;

	public TileBreaker()
	{
		super(Material.iron);
		normalRender = false;
		rotationMask = Byte.parseByte("111111", 2);
	}

	public InternalInventoryHandler getInvHandler()
	{
		if (invHandler == null)
		{
			invHandler = new InternalInventoryHandler(this);
		}
		return invHandler;
	}

	@Override
	public void onAdded()
	{
		work();
	}

	@Override
	public void onNeighborChanged()
	{
		work();
	}

	@Override
	public void updateEntity()
	{
		if (doWork)
		{
			if (place_delay < Byte.MAX_VALUE)
			{
				place_delay++;
			}

			if (place_delay >= 10)
			{
				doWork();
				doWork = false;
				place_delay = 0;
			}
		}
	}

	public void work()
	{
		if (isIndirectlyPowered())
		{
			doWork = true;
			place_delay = 0;
		}
	}

	public void doWork()
	{
		if (isIndirectlyPowered())
		{
			ForgeDirection dir = getDirection();
			Vector3 check = position().translate(dir);
			VectorWorld put = (VectorWorld) position().translate(dir.getOpposite());

			Block block = Block.blocksList[check.getBlockID(world())];

			if (block != null)
			{
				int candidateMeta = world().getBlockMetadata(check.intX(), check.intY(), check.intZ());
				boolean flag = true;

				//Get items dropped
				ArrayList<ItemStack> drops = block.getBlockDropped(getWorldObj(), check.intX(), check.intY(), check.intZ(), candidateMeta, 0);

				for (ItemStack stack : drops)
				{
					//Insert into tile if one exists
					ItemStack insert = stack.copy();
					insert = getInvHandler().storeItem(insert, this.getDirection().getOpposite());
					//If not spit items into world
					if (insert != null)
					{
						getInvHandler().throwItem(this.getDirection().getOpposite(), insert);
					}
				}

				//Destroy block
				ResonantInduction.proxy.renderBlockParticle(worldObj, check.intX(), check.intY(), check.intZ(), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), world().getBlockId(check.intX(), check.intY(), check.intZ()), 1);

				getWorldObj().destroyBlock(check.intX(), check.intY(), check.intZ(), false);
				getWorldObj().playAuxSFX(1012, check.intX(), check.intY(), check.intZ(), 0);

			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(IBlockAccess access, int side)
	{
		int meta = access.getBlockMetadata(x(), y(), z());

		if (side == meta)
		{
			return iconFront;
		}
		else if (side == (meta ^ 1))
		{
			return iconBack;
		}

		return getIcon();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == (meta ^ 1))
		{
			return iconFront;
		}
		else if (side == meta)
		{
			return iconBack;
		}

		return getIcon();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconFront = iconRegister.registerIcon(getTextureName() + "_front");
		iconBack = iconRegister.registerIcon(getTextureName() + "_back");
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected TileRender newRenderer()
	{
		return new RotatedTextureRenderer(this);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			readFromNBT(PacketHandler.readNBTTagCompound(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
