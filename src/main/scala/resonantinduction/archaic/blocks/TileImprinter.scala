package resonantinduction.archaic.blocks

import java.util.{HashSet, Iterator, Set}

import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.ISidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.network.Packet
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonant.content.prefab.java.TileAdvanced
import resonant.content.spatial.block.SpatialBlock
import resonant.engine.ResonantEngine
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.render.RenderItemOverlayUtility
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.core.Reference
import resonant.lib.transform.vector.{Vector2, Vector3}

import scala.collection.JavaConversions._

class TileImprinter extends TileAdvanced(Material.circuits) with ISidedInventory with IPacketReceiver
{
    var inventory: Array[ItemStack] = new Array[ItemStack](10)

    override def getDescriptionPacket: Packet =
    {
        val nbt: NBTTagCompound = new NBTTagCompound
        this.writeToNBT(nbt)
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, nbt))
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

    /**
     * Inventory methods.
     */
    override def canUpdate: Boolean =
    {
        return false
    }

    def getSizeInventory: Int =
    {
        return this.inventory.length
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor
     * sections).
     */
    def setInventorySlotContents(slot: Int, itemStack: ItemStack)
    {
        if (slot < this.getSizeInventory)
        {
            inventory(slot) = itemStack
        }
    }

    def getInventoryName: String =
    {
        return null
    }

    def hasCustomInventoryName: Boolean =
    {
        return false
    }

    def decrStackSize(i: Int, amount: Int): ItemStack =
    {
        if (this.getStackInSlot(i) != null)
        {
            var stack: ItemStack = null
            if (this.getStackInSlot(i).stackSize <= amount)
            {
                stack = this.getStackInSlot(i)
                this.setInventorySlotContents(i, null)
                return stack
            }
            else
            {
                stack = this.getStackInSlot(i).splitStack(amount)
                if (this.getStackInSlot(i).stackSize == 0)
                {
                    this.setInventorySlotContents(i, null)
                }
                return stack
            }
        }
        else
        {
            return null
        }
    }

    def getStackInSlot(slot: Int): ItemStack =
    {
        return this.inventory(slot)
    }

    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as
     * an EntityItem - like when you close a workbench GUI.
     */
    def getStackInSlotOnClosing(slot: Int): ItemStack =
    {
        if (this.getStackInSlot(slot) != null)
        {
            val var2: ItemStack = this.getStackInSlot(slot)
            this.setInventorySlotContents(slot, null)
            return var2
        }
        else
        {
            return null
        }
    }

    def openInventory
    {
        this.onInventoryChanged
    }

    def closeInventory
    {
        this.onInventoryChanged
    }

    /**
     * Updates all the output slots. Call this to update the Imprinter.
     */
    def onInventoryChanged
    {
        if (!this.worldObj.isRemote)
        {
            val fitlerStack: ItemStack = this.inventory(9)
            if (fitlerStack != null && fitlerStack.getItem.isInstanceOf[ItemImprint])
            {
                val outputStack: ItemStack = fitlerStack.copy
                val filters: java.util.List[ItemStack] = ItemImprint.getFilters(outputStack)
                val toAdd: Set[ItemStack] = new HashSet[ItemStack]
                val toBeImprinted: Set[ItemStack] = new HashSet[ItemStack]

                var i: Int = 0
                while (i < 9)
                {
                    val stackInInventory: ItemStack = inventory(i)
                    if (stackInInventory != null)
                    {
                        for (check <- toBeImprinted)
                        {
                            if (check.isItemEqual(stackInInventory))
                            {
                                i = 10
                            }
                        }
                        toBeImprinted.add(stackInInventory)
                    }
                    i += 1;
                }

                for (stackInInventory <- toBeImprinted)
                {
                    val it: Iterator[ItemStack] = filters.iterator
                    var removed: Boolean = false
                    while (it.hasNext)
                    {
                        val filteredStack: ItemStack = it.next
                        if (filteredStack.isItemEqual(stackInInventory))
                        {
                            it.remove
                            removed = true
                        }
                    }
                    if (!removed)
                    {
                        toAdd.add(stackInInventory)
                    }
                }
                filters.addAll(toAdd)
                ItemImprint.setFilters(outputStack, filters)
                this.inventory(9) = outputStack
            }
        }
    }

    /**
     * NBT Data
     */
    override def readFromNBT(nbt: NBTTagCompound)
    {
        super.readFromNBT(nbt)
        val var2: NBTTagList = nbt.getTagList("Items", 0)
        this.inventory = new Array[ItemStack](10)

        for (i <- 0 to var2.tagCount)
        {
            val var4: NBTTagCompound = var2.getCompoundTagAt(i).asInstanceOf[NBTTagCompound]
            val var5: Byte = var4.getByte("Slot")
            if (var5 >= 0 && var5 < this.getSizeInventory)
            {
                this.setInventorySlotContents(var5, ItemStack.loadItemStackFromNBT(var4))
            }
        }

    }

    /**
     * Writes a tile entity to NBT.
     */
    override def writeToNBT(nbt: NBTTagCompound)
    {
        super.writeToNBT(nbt)
        val var2: NBTTagList = new NBTTagList

        for (i <- 0 to this.getSizeInventory)
        {
            if (this.getStackInSlot(i) != null)
            {
                val var4: NBTTagCompound = new NBTTagCompound
                var4.setByte("Slot", i.asInstanceOf[Byte])
                this.getStackInSlot(i).writeToNBT(var4)
                var2.appendTag(var4)
            }
        }
        nbt.setTag("Items", var2)
    }

    def isItemValidForSlot(i: Int, itemstack: ItemStack): Boolean =
    {
        return true
    }

    def getInventoryStackLimit: Int =
    {
        return 64
    }

    def isUseableByPlayer(entityplayer: EntityPlayer): Boolean =
    {
        return if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) ne this) false else entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D
    }

    def getAccessibleSlotsFromSide(side: Int): Array[Int] =
    {
        return if (side == 1) Array[Int](0, 1, 2, 3, 4, 5, 6, 7, 8) else new Array[Int](10)
    }

    def canInsertItem(slot: Int, itemstack: ItemStack, side: Int): Boolean =
    {
        return this.isItemValidForSlot(slot, itemstack)
    }

    def canExtractItem(slot: Int, itemstack: ItemStack, side: Int): Boolean =
    {
        return this.isItemValidForSlot(slot, itemstack)
    }

    override def renderDynamic(position: Vector3, frame: Float, pass: Int)
    {
        GL11.glPushMatrix
        RenderItemOverlayUtility.renderTopOverlay(this, inventory, ForgeDirection.EAST, x, y, z)
        RenderItemOverlayUtility.renderItemOnSides(this, getStackInSlot(9), x, y, z)
        GL11.glPopMatrix
    }

    @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister)
    {
        super.registerIcons(iconReg)
        SpatialBlock.icon.put("imprinter_side", iconReg.registerIcon(Reference.prefix + "imprinter_side"))
        SpatialBlock.icon.put("imprinter_top", iconReg.registerIcon(Reference.prefix + "imprinter_top"))
        SpatialBlock.icon.put("imprinter_bottom", iconReg.registerIcon(Reference.prefix + "imprinter_bottom"))
    }

    @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon =
    {
        if (side == 1)
        {
            return SpatialBlock.icon.get("imprinter_top")
        }
        else if (side == 0)
        {
            return SpatialBlock.icon.get("imprinter_bottom")
        }
        return SpatialBlock.icon.get("imprinter_side")
    }

    override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
    {
        var current: ItemStack = player.inventory.getCurrentItem
        if (hitSide == 1)
        {
            if (!world.isRemote)
            {
                val hitVector: Vector2 = new Vector2(hit.x, hit.z)
                val regionLength: Double = 1d / 3d

                for (j <- 0 to 3)
                {
                    for (k <- 0 to 3)
                    {
                        val check: Vector2 = new Vector2(j, k).multiply(regionLength)
                        if (check.distance(hitVector) < regionLength)
                        {
                            val slotID: Int = j * 3 + k
                            var didInsert: Boolean = false
                            val checkStack: ItemStack = inventory(slotID)
                            if (current != null)
                            {
                                if (checkStack == null || checkStack.isItemEqual(current))
                                {
                                    if (ControlKeyModifer.isControlDown(player))
                                    {
                                        if (checkStack == null)
                                        {
                                            inventory(slotID) = current
                                        }
                                        else
                                        {
                                            inventory(slotID).stackSize += current.stackSize
                                            current.stackSize = 0
                                        }
                                        current = null
                                    }
                                    else
                                    {
                                        if (checkStack == null)
                                        {
                                            inventory(slotID) = current.splitStack(1)
                                        }
                                        else
                                        {
                                            inventory(slotID).stackSize += 1
                                            current.stackSize -= 1
                                        }
                                    }
                                    if (current == null || current.stackSize <= 0)
                                    {
                                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
                                    }
                                    didInsert = true
                                }
                            }
                            if (!didInsert && checkStack != null)
                            {
                                InventoryUtility.dropItemStack(world, new Vector3(player), checkStack, 0)
                                inventory(slotID) = null
                            }
                            world.markBlockForUpdate(xi, yi, zi)
                            return true
                        }
                    }
                }
                world.markBlockForUpdate(xi, yi, zi)
            }
            return true
        }
        else if (hitSide != 0)
        {
            val output: ItemStack = getStackInSlot(9)
            if (output != null)
            {
                InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
                setInventorySlotContents(9, null)
            }
            else if (current != null && current.getItem.isInstanceOf[ItemImprint])
            {
                setInventorySlotContents(9, current)
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
            }
        }
        return false
    }

    override def onNeighborChanged(block: Block)
    {
        val b: Block = toVectorWorld.add(ForgeDirection.getOrientation(1)).getBlock
        if (Blocks.piston_head eq b)
        {
            onInventoryChanged
        }
    }
}