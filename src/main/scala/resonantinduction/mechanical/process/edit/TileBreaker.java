package resonantinduction.mechanical.process.edit;

import java.util.ArrayList;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.IRotatable;
import resonant.content.prefab.java.TileAdvanced;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.utility.inventory.InternalInventoryHandler;
import resonantinduction.core.ResonantInduction;
import universalelectricity.core.transform.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.core.transform.vector.VectorWorld;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class TileBreaker extends TileAdvanced implements IRotatable, IPacketReceiver
{
	@SideOnly(Side.CLIENT)
	private static IIcon iconFront, iconBack;
	private boolean doWork = false;
	private InternalInventoryHandler invHandler;
	private byte place_delay = 0;

	public TileBreaker()
	{
		super(Material.iron);
		normalRender(false);
		//rotationMask = Byte.parseByte("111111", 2);
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
	public void onNeighborChanged(Block block)
	{
		work();
	}

	@Override
	public void update()
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
			Vector3 check = position().add(dir);
			VectorWorld put = (VectorWorld) position().add(dir.getOpposite());

			Block block = check.getBlock(world());

			if (block != null)
			{
				int candidateMeta = world().getBlockMetadata(check.xi(), check.yi(), check.zi());
				boolean flag = true;

				//Get items dropped
				ArrayList<ItemStack> drops = block.getDrops(getWorldObj(), check.xi(), check.yi(), check.zi(), candidateMeta, 0);

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
				ResonantInduction.proxy().renderBlockParticle(worldObj, check.xi(), check.yi(), check.zi(), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), Block.getIdFromBlock(block), 1);

				getWorldObj().setBlockToAir(check.xi(), check.yi(), check.zi());
				getWorldObj().playAuxSFX(1012, check.xi(), check.yi(), check.zi(), 0);

			}
		}
	}

	@Override
	public PacketTile getDescPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new PacketTile(this, nbt);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess access, int side)
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
	public IIcon getIcon(int side, int meta)
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
	public void registerIcons(IIconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconFront = iconRegister.registerIcon(getTextureName() + "_front");
		iconBack = iconRegister.registerIcon(getTextureName() + "_back");
	}

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
	{
		try
		{
			readFromNBT(ByteBufUtils.readTag(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
