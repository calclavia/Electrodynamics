package resonantinduction.mechanical.machine

import java.util.ArrayList

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{AxisAlignedBB, IIcon}
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import resonant.engine.ResonantEngine
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketIDReceiver
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonantinduction.archaic.blocks.TileFilterable
import resonantinduction.core.Reference
import resonantinduction.mechanical.MechanicalContent

class TileDetector extends TileFilterable with IPacketIDReceiver
{
    private var powering: Boolean = false

    //constructor
    setTextureName(Reference.prefix + "material_metal_side")
    this.canProvidePower(true)

    override def update
    {
        super.update
        if (!this.worldObj.isRemote && this.ticks % 10 == 0)
        {
            val metadata: Int = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord)
            val testArea: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1)
            val dir: ForgeDirection = ForgeDirection.getOrientation(metadata)
            testArea.offset(dir.offsetX, dir.offsetY, dir.offsetZ)
            val entities: ArrayList[Entity] = this.worldObj.getEntitiesWithinAABB(classOf[EntityItem], testArea).asInstanceOf[ArrayList[Entity]]
            var powerCheck: Boolean = false
            if (entities.size > 0)
            {
                if (getFilter != null)
                {
                    {
                        var i: Int = 0
                        while (i < entities.size)
                        {
                            {
                                val e: EntityItem = entities.get(i).asInstanceOf[EntityItem]
                                val itemStack: ItemStack = e.getEntityItem
                                powerCheck = this.isFiltering(itemStack)
                            }
                            ({
                                i += 1;
                                i - 1
                            })
                        }
                    }
                }
                else
                {
                    powerCheck = true
                }
            }
            else
            {
                powerCheck = false
            }
            if (powerCheck != this.powering)
            {
                this.powering = powerCheck
                this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, MechanicalContent.blockDetector)
                this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord + 1, this.zCoord, MechanicalContent.blockDetector)

                for (x <- (this.xCoord - 1) to (this.xCoord + 1))
                {
                    for (z <- (this.zCoord - 1) to (this.zCoord + 1))
                    {
                        this.worldObj.notifyBlocksOfNeighborChange(x, this.yCoord + 1, z, MechanicalContent.blockDetector)
                    }
                }
                ResonantEngine.packetHandler.sendToAllAround(new PacketTile(xi, yi, zi, Array[Any](0, this.isInverted)), this)
            }
        }
    }

    override def invalidate
    {
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, MechanicalContent.blockDetector)
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord + 1, this.zCoord, MechanicalContent.blockDetector)
        super.invalidate
    }

    override def readFromNBT(tag: NBTTagCompound)
    {
        super.readFromNBT(tag)
        this.powering = tag.getBoolean("powering")
    }

    override def writeToNBT(tag: NBTTagCompound)
    {
        super.writeToNBT(tag)
        tag.setBoolean("powering", this.powering)
    }

    override def getDescriptionPacket: Packet =
    {
        return ResonantEngine.packetHandler.toMCPacket(new PacketTile(xi, yi, zi, Array[Any](0, this.isInverted)))
    }

    override def read(data: ByteBuf, id: Int, player: EntityPlayer, `type`: PacketType): Boolean =
    {
        if (id == 0) this.setInverted(data.readBoolean)
        return true
    }

    @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister)
    {
        SpatialBlock.icon.put("detector_front_green", iconReg.registerIcon(Reference.prefix + "detector_front_green"))
        SpatialBlock.icon.put("detector_front_red", iconReg.registerIcon(Reference.prefix + "detector_front_red"))
        SpatialBlock.icon.put("detector_side_green", iconReg.registerIcon(Reference.prefix + "detector_side_green"))
        SpatialBlock.icon.put("detector_side_red", iconReg.registerIcon(Reference.prefix + "detector_side_red"))
    }

    @SideOnly(Side.CLIENT) override def getIcon(side: Int, metadata: Int): IIcon =
    {
        if (side == ForgeDirection.SOUTH.ordinal)
        {
            return SpatialBlock.icon.get("detector_front_green")
        }
        return SpatialBlock.icon.get("detector_side_green")
    }

    @SideOnly(Side.CLIENT) override def getIcon(iBlockAccess: IBlockAccess, side: Int): IIcon =
    {
        var isInverted: Boolean = false
        var isFront: Boolean = false
        val tileEntity: TileEntity = iBlockAccess.getTileEntity(xi, yi, zi)
        if (tileEntity.isInstanceOf[TileDetector])
        {
            isFront = side == (tileEntity.asInstanceOf[TileDetector]).getDirection.ordinal
            isInverted = (tileEntity.asInstanceOf[TileDetector]).isInverted
        }
        return if (isInverted) (if (isFront) SpatialBlock.icon.get("detector_front_red") else SpatialBlock.icon.get("detector_side_red")) else (if (isFront) SpatialBlock.icon.get("detector_front_green") else SpatialBlock.icon.get("detector_side_green"))
    }

    override def getStrongRedstonePower(access: IBlockAccess, side: Int): Int =
    {
        if (side != getDirection.getOpposite.ordinal)
        {
            return if (powering) 15 else 0
        }
        return 0
    }
}