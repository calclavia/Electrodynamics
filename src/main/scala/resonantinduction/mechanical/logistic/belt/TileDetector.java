package resonantinduction.mechanical.logistic.belt;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.content.spatial.block.SpatialBlock;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketIDReceiver;
import resonantinduction.archaic.blocks.TileFilterable;
import resonantinduction.core.Reference;
import resonantinduction.mechanical.Mechanical;

public class TileDetector extends TileFilterable implements IPacketIDReceiver
{
	private boolean powering = false;
    IIcon front_red, front_green, side_green, side_red;

    public TileDetector()
    {
        super();
        setTextureName(Reference.prefix() + "material_metal_side");
        this.isOpaqueCube(false);
        this.normalRender(false);
        this.canProvidePower(true);
    }

	@Override
	public void update()
	{
		super.update();

		if (!this.worldObj.isRemote && this.ticks() % 10 == 0)
		{
			int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
			AxisAlignedBB testArea = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
			ForgeDirection dir = ForgeDirection.getOrientation(metadata);
			testArea.offset(dir.offsetX, dir.offsetY, dir.offsetZ);

			ArrayList<Entity> entities = (ArrayList<Entity>) this.worldObj.getEntitiesWithinAABB(EntityItem.class, testArea);
			boolean powerCheck = false;

			if (entities.size() > 0)
			{
				if (getFilter() != null)
				{
					for (int i = 0; i < entities.size(); i++)
					{
						EntityItem e = (EntityItem) entities.get(i);
						ItemStack itemStack = e.getEntityItem();

						powerCheck = this.isFiltering(itemStack);
					}
				}
				else
				{
					powerCheck = true;
				}
			}
			else
			{
				powerCheck = false;
			}

			if (powerCheck != this.powering)
			{
				this.powering = powerCheck;
				this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, Mechanical.blockDetector);
				this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord + 1, this.zCoord, Mechanical.blockDetector);
				for (int x = this.xCoord - 1; x <= this.xCoord + 1; x++)
				{
					for (int z = this.zCoord - 1; z <= this.zCoord + 1; z++)
					{
						this.worldObj.notifyBlocksOfNeighborChange(x, this.yCoord + 1, z, Mechanical.blockDetector);
					}
				}

				ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile(this, 0, this.isInverted()), this);
			}
		}
	}

	@Override
	public void invalidate()
	{
		this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, Mechanical.blockDetector);
		this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord + 1, this.zCoord, Mechanical.blockDetector);
		super.invalidate();
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		this.powering = tag.getBoolean("powering");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		tag.setBoolean("powering", this.powering);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, 0, this.isInverted()));
	}

	@Override
	public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType type)
	{
        if(id == 0)
		    this.setInverted(data.readBoolean());
        return true;
	}

	public int isPoweringTo(ForgeDirection side)
	{
		return this.powering && this.getDirection() != side.getOpposite() ? 15 : 0;
	}

	public boolean isIndirectlyPoweringTo(ForgeDirection side)
	{
		return this.isPoweringTo(side) > 0;
	}

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconReg)
    {
        SpatialBlock.icon().put("detector_front_green", iconReg.registerIcon(Reference.prefix() + "detector_front_green"));
        SpatialBlock.icon().put("detector_front_red", iconReg.registerIcon(Reference.prefix() + "detector_front_red"));
        SpatialBlock.icon().put("detector_side_green", iconReg.registerIcon(Reference.prefix() + "detector_side_green"));
        SpatialBlock.icon().put("detector_side_red", iconReg.registerIcon(Reference.prefix() + "detector_side_red"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata)
    {
        if (side == ForgeDirection.SOUTH.ordinal())
        {
            return SpatialBlock.icon().get("detector_front_green");
        }

        return SpatialBlock.icon().get("detector_side_green");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess iBlockAccess, int side)
    {
        boolean isInverted = false;
        boolean isFront = false;
        TileEntity tileEntity = iBlockAccess.getTileEntity(x(), y(), z());

        if (tileEntity instanceof TileDetector)
        {
            isFront = side == ((TileDetector) tileEntity).getDirection().ordinal();
            isInverted = ((TileDetector) tileEntity).isInverted();
        }

        return isInverted ? (isFront ? front_red : side_red) : (isFront ? front_green : side_green);
    }

    @Override
    public int getStrongRedstonePower(IBlockAccess access, int side)
    {
        if(side != getDirection().ordinal())
        {
            return powering ? 15 : 0;
        }
        return 0;
    }
}
