package resonantinduction.archaic.process

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.recipe.MachineRecipes
import resonant.api.recipe.RecipeResource
import resonant.lib.factory.resources.RecipeType
import resonant.lib.network.discriminator.PacketTile
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.prefab.tile.TileInventory
import resonant.lib.utility.FluidUtility
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.core.Reference
import resonant.lib.transform.vector.Vector3

/**
 * Turns molten fuilds into ingots.
 * <p/>
 * 1 cubed meter of molten fluid = 1 block
 * Approximately 100-110 L of fluid = 1 ingot.

 * @author Calclavia
 */
class TileCastingMold extends TileInventory(Material.rock) with IFluidHandler with IPacketReceiver
{
    private final val amountPerIngot: Int = 100
    protected var tank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

    //Constructor
    setTextureName(Reference.prefix + "material_metal_side")
    normalRender = false
    isOpaqueCube = false

    override def canUpdate: Boolean =
    {
        return false
    }

    override def getDescPacket: PacketTile =
    {
        val nbt: NBTTagCompound = new NBTTagCompound
        this.writeToNBT(nbt)
        return new PacketTile(this, nbt)
    }

    def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
    {
        try
        {
            this.readFromNBT(ByteBufUtils.readTag(data))
        }
        catch
            {
                case e: Exception =>
                {
                    e.printStackTrace
                }
            }
    }

    override def onInventoryChanged
    {
        if (worldObj != null)
        {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
        }
    }

    override def update
    {
        val checkPos: Vector3 = toVector3.add(0, 1, 0)
        val drainStack: FluidStack = FluidUtility.drainBlock(worldObj, checkPos, false)
        if (MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER.name, drainStack).length > 0)
        {
            if (drainStack.amount == tank.fill(drainStack, false))
            {
                tank.fill(FluidUtility.drainBlock(worldObj, checkPos, true), true)
            }
        }
        while (tank.getFluidAmount >= amountPerIngot && (getStackInSlot(0) == null || getStackInSlot(0).stackSize < getStackInSlot(0).getMaxStackSize))
        {
            val outputs: Array[RecipeResource] = MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER.name, tank.getFluid)
            for (output <- outputs)
            {
                incrStackSize(0, output.getItemStack)
            }
            tank.drain(amountPerIngot, true)
        }
    }

    override def readFromNBT(tag: NBTTagCompound)
    {
        super.readFromNBT(tag)
        tank.writeToNBT(tag)
    }

    override def writeToNBT(tag: NBTTagCompound)
    {
        super.writeToNBT(tag)
        tank.readFromNBT(tag)
    }

    def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
        val fill: Int = tank.fill(resource, doFill)
        updateEntity
        return fill
    }

    def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
    {
        return null
    }

    def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
    {
        return null
    }

    def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return fluid != null && fluid.getName.contains("molten")
    }

    def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return false
    }

    def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
    {
        return Array[FluidTankInfo](tank.getInfo)
    }

    override def click(player: EntityPlayer)
    {
        if (!world.isRemote)
        {
            val output: ItemStack = getStackInSlot(0)
            if (output != null)
            {
                InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
                setInventorySlotContents(0, null)
            }
            onInventoryChanged
        }
    }

    override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
    {
        update
        val current: ItemStack = player.inventory.getCurrentItem
        val output: ItemStack = getStackInSlot(0)
        if (output != null)
        {
            InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
            setInventorySlotContents(0, null)
        }
        return true
    }
}