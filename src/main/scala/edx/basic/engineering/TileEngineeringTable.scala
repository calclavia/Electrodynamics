package edx.basic.engineering

import java.util.ArrayList

import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.basic.blocks.ItemImprint
import edx.basic.process.grinding.ItemHammer
import edx.core.{Electrodynamics, Reference}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP, InventoryPlayer}
import net.minecraft.inventory.{IInventory, ISidedInventory, InventoryCrafting}
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.network.Packet
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{ChatComponentText, IIcon}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.oredict.OreDictionary
import org.apache.commons.lang3.ArrayUtils
import org.lwjgl.opengl.GL11
import resonant.api.gui.ISlotPickResult
import resonant.api.recipe.{MachineRecipes, RecipeResource, RecipeType}
import resonant.api.tile.IRotatable
import resonant.engine.ResonantEngine
import resonant.lib.collection.Pair
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.prefab.gui.ContainerDummy
import resonant.lib.prefab.tile.item.ItemBlockSaved
import resonant.lib.prefab.tile.mixed.TileInventory
import resonant.lib.render.RenderItemOverlayUtility
import resonant.lib.transform.region.Cuboid
import resonant.lib.transform.vector.{Vector2, Vector3}
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.inventory.AutoCraftingManager.IAutoCrafter
import resonant.lib.utility.inventory.{AutoCraftingManager, InventoryUtility}

import scala.collection.JavaConversions._

/**
 * Advanced crafting table that stores its crafting grid, can craft out of the player's inv, and be
 * configed to auto craft.
 *
 * @author DarkGuardsman, Calclavia
 */
object TileEngineeringTable
{
  val CRAFTING_MATRIX_END: Int = 9
  val CRAFTING_OUTPUT_END: Int = CRAFTING_MATRIX_END + 1
  val PLAYER_OUTPUT_END: Int = CRAFTING_OUTPUT_END + 40
  val CENTER_SLOT: Int = 4
  val CRAFTING_OUTPUT_SLOT: Int = 0
  /**
   * 9 slots for crafting, 1 slot for a output.
   */
  val CRAFTING_MATRIX_SIZE: Int = 9
  val craftingSlots: Array[Int] = Array(0, 1, 2, 3, 4, 5, 6, 7, 8)

  @SideOnly(Side.CLIENT) private var iconTop: IIcon = null
  @SideOnly(Side.CLIENT) private var iconFront: IIcon = null
  @SideOnly(Side.CLIENT) private var iconSide: IIcon = null
}

class TileEngineeringTable extends TileInventory(Material.wood) with IPacketReceiver with IRotatable with ISidedInventory with ISlotPickResult with IAutoCrafter
{

  var craftingMatrix: Array[ItemStack] = new Array[ItemStack](TileEngineeringTable.CRAFTING_MATRIX_SIZE)
  /**
   * The output inventory containing slots.
   */
  var outputInventory: Array[ItemStack] = new Array[ItemStack](1)
  /**
   * The ability for the engineering table to search nearby inventories.
   */
  var searchInventories: Boolean = true
  private var craftManager: AutoCraftingManager = null
  /**
   * Temporary player inventory stored to draw the player's items.
   */
  private var invPlayer: InventoryPlayer = null
  private var playerSlots: Array[Int] = null

  //Constructor
  bounds = new Cuboid(0, 0, 0, 1, 0.9f, 1)
  isOpaqueCube = false
  itemBlock(classOf[ItemBlockSaved])

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    return if (side == 1) TileEngineeringTable.iconTop else (if (side == meta) TileEngineeringTable.iconFront else TileEngineeringTable.iconSide)
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister)
  {
    TileEngineeringTable.iconTop = iconRegister.registerIcon(getTextureName + "_top")
    TileEngineeringTable.iconFront = iconRegister.registerIcon(getTextureName + "_front")
    TileEngineeringTable.iconSide = iconRegister.registerIcon(getTextureName + "_side")
  }

  override def click(player: EntityPlayer)
  {
    if (!world.isRemote && ControlKeyModifer.isControlDown(player))
    {
      {
        var i: Int = 0
        while (i < getSizeInventory - 1)
        {
          {
            if (getStackInSlot(i) != null)
            {
              InventoryUtility.dropItemStack(world, new Vector3(player), getStackInSlot(i))
              setInventorySlotContents(i, null)
            }
          }
          ({
            i += 1;
            i
          })
        }
      }
    }
  }

  override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
  {
    if (player.getCurrentEquippedItem != null && player.getCurrentEquippedItem.getItem.isInstanceOf[ItemHammer])
    {
      for (slot <- 0 to TileEngineeringTable.CRAFTING_OUTPUT_END)
      {
        val inputStack: ItemStack = getStackInSlot(slot)
        if (inputStack != null)
        {
          val oreName: String = OreDictionary.getOreName(OreDictionary.getOreID(inputStack))
          if (oreName != null && !(oreName == "Unknown"))
          {
            val outputs: Array[RecipeResource] = MachineRecipes.instance.getOutput(RecipeType.GRINDER.name, oreName)
            if (outputs != null && outputs.length > 0)
            {
              if (!world.isRemote && world.rand.nextFloat < 0.2)
              {
                for (resource <- outputs)
                {
                  val outputStack: ItemStack = resource.getItemStack.copy
                  if (outputStack != null)
                  {
                    InventoryUtility.dropItemStack(world, new Vector3(player), outputStack, 0)
                    setInventorySlotContents(slot, if (({
                      inputStack.stackSize -= 1;
                      inputStack.stackSize
                    }) <= 0) null
                    else inputStack)
                  }
                }
              }
              Electrodynamics.proxy.renderBlockParticle(world, new Vector3(x + 0.5, y + 0.5, z + 0.5), new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), Item.getIdFromItem(inputStack.getItem), 1)
              world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.prefix + "hammer", 0.5f, 0.8f + (0.2f * world.rand.nextFloat))
              player.addExhaustion(0.1f)
              player.getCurrentEquippedItem.damageItem(1, player)
              return true
            }
          }
        }
      }
      return true
    }
    if (hitSide == 1)
    {
      if (!world.isRemote)
      {
        val hitVector: Vector3 = new Vector3(hit.x, 0, hit.z)
        val regionLength: Double = 1d / 3d

        for (j <- 0 to 3)
        {
          for (k <- 0 to 3)
          {
            val check: Vector2 = new Vector2(j, k).multiply(regionLength)
            if (check.distance(hitVector.toVector2) < regionLength)
            {
              val slotID: Int = j * 3 + k
              interactCurrentItem(this, slotID, player)
              onInventoryChanged
              return true
            }
          }
        }
        onInventoryChanged
      }
      return true
    }
    else if (hitSide != 0)
    {
      if (!world.isRemote)
      {
        setPlayerInventory(player.inventory)
        var output: ItemStack = getStackInSlot(9)
        var firstLoop: Boolean = true
        while (output != null && (firstLoop || ControlKeyModifer.isControlDown(player)))
        {
          onPickUpFromSlot(player, 9, output)
          if (output.stackSize > 0)
          {
            InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
          }
          setInventorySlotContents(9, null)
          onInventoryChanged
          output = getStackInSlot(9)
          firstLoop = false
        }
        setPlayerInventory(null)
      }
      return true
    }
    return false
  }

  /**
   * Creates a "fake inventory" and hook the player up to the crafter to use the player's items.
   */
  def setPlayerInventory(invPlayer: InventoryPlayer)
  {
    if (searchInventories)
    {
      if (invPlayer != null)
      {
        playerSlots = new Array[Int](invPlayer.getSizeInventory)

        for (i <- 0 until playerSlots.length)
        {
          playerSlots(i) = i + TileEngineeringTable.CRAFTING_OUTPUT_END

        }
      }
      else
      {
        playerSlots = null
      }
      this.invPlayer = invPlayer
    }
  }

  def onPickUpFromSlot(entityPlayer: EntityPlayer, slotID: Int, itemStack: ItemStack)
  {
    if (!worldObj.isRemote)
    {
      if (itemStack != null)
      {
        val idealRecipeItem: Pair[ItemStack, Array[ItemStack]] = getCraftingManager.getIdealRecipe(itemStack)
        if (idealRecipeItem != null)
        {
          getCraftingManager.consumeItems(idealRecipeItem.right.clone: _*)
        }
        else
        {
          itemStack.stackSize = 0
        }
      }
    }
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      searchInventories = !searchInventories
      if (!world.isRemote)
      {
        if (searchInventories)
        {
          player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("engineerTable.config.inventory.true")))
        }
        else
        {
          player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("engineerTable.config.inventory.false")))
        }
      }
      markUpdate
      return true
    }
    return super.configure(player, side, hit)
  }

  override def getDrops(metadata: Int, fortune: Int): ArrayList[ItemStack] = new ArrayList[ItemStack]

  override def onRemove(block: Block, par6: Int)
  {
    val stack: ItemStack = ItemBlockSaved.getItemStackWithNBT(block, world, xi, yi, zi)
    InventoryUtility.dropItemStack(world, center, stack)
  }

  override def canUpdate: Boolean =
  {
    return false
  }

  override def getDescriptionPacket: Packet =
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    this.writeToNBT(nbt)
    return ResonantEngine.packetHandler.toMCPacket(new PacketTile(this, nbt))
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    val nbtList: NBTTagList = new NBTTagList

    for (i <- 0 to this.getSizeInventory)
    {
      if (this.getStackInSlot(i) != null)
      {
        val var4: NBTTagCompound = new NBTTagCompound
        var4.setByte("Slot", i.asInstanceOf[Byte])
        this.getStackInSlot(i).writeToNBT(var4)
        nbtList.appendTag(var4)
      }
    }
    nbt.setTag("Items", nbtList)
    nbt.setBoolean("searchInventories", this.searchInventories)
  }

  override def getSizeInventory: Int =
  {
    return 10 + (if (this.invPlayer != null) this.invPlayer.getSizeInventory else 0)
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    try
    {
      readFromNBT(ByteBufUtils.readTag(data))
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
   * NBT Data
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    val nbtList: NBTTagList = nbt.getTagList("Items", 0)
    this.craftingMatrix = new Array[ItemStack](9)
    this.outputInventory = new Array[ItemStack](1)

    for (i <- 0 to nbtList.tagCount)
    {
      val stackTag: NBTTagCompound = nbtList.getCompoundTagAt(i)
      val id: Byte = stackTag.getByte("Slot")
      if (id >= 0 && id < this.getSizeInventory)
      {
        this.setInventorySlotContents(id, ItemStack.loadItemStackFromNBT(stackTag))
      }
    }

    this.searchInventories = nbt.getBoolean("searchInventories")
  }

  override def decrStackSize(i: Int, amount: Int): ItemStack =
  {
    if (getStackInSlot(i) != null)
    {
      var stack: ItemStack = null
      if (getStackInSlot(i).stackSize <= amount)
      {
        stack = getStackInSlot(i)
        setInventorySlotContents(i, null)
        return stack
      }
      else
      {
        stack = getStackInSlot(i).splitStack(amount)
        if (getStackInSlot(i).stackSize == 0)
        {
          setInventorySlotContents(i, null)
        }
        return stack
      }
    }
    else
    {
      return null
    }
  }

  /**
   * When some containers are closed they call this on each slot, then drop whatever it returns as
   * an EntityItem - like when you close a workbench GUI.
   */
  override def getStackInSlotOnClosing(slot: Int): ItemStack =
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

  override def setInventorySlotContents(slot: Int, itemStack: ItemStack)
  {
    if (slot < TileEngineeringTable.CRAFTING_MATRIX_END)
    {
      craftingMatrix(slot) = itemStack
    }
    else if (slot < TileEngineeringTable.CRAFTING_OUTPUT_END)
    {
      outputInventory(slot - TileEngineeringTable.CRAFTING_MATRIX_END) = itemStack
    }
    else if (slot < TileEngineeringTable.PLAYER_OUTPUT_END && this.invPlayer != null)
    {
      this.invPlayer.setInventorySlotContents(slot - TileEngineeringTable.CRAFTING_OUTPUT_END, itemStack)
      val player: EntityPlayer = this.invPlayer.player
      if (player.isInstanceOf[EntityPlayerMP])
      {
        (player.asInstanceOf[EntityPlayerMP]).sendContainerToPlayer(player.inventoryContainer)
      }
    }
    else if (searchInventories)
    {
      var idDisplacement: Int = TileEngineeringTable.PLAYER_OUTPUT_END
      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val tile: TileEntity = toVectorWorld.add(dir).getTileEntity
        if (tile.isInstanceOf[IInventory])
        {
          val inventory: IInventory = tile.asInstanceOf[IInventory]
          val slotID: Int = slot - idDisplacement
          if (slotID >= 0 && slotID < inventory.getSizeInventory)
          {
            inventory.setInventorySlotContents(slotID, itemStack)
          }
          idDisplacement += inventory.getSizeInventory
        }
      }
    }
    onInventoryChanged
  }

  /**
   * Updates all the output slots. Call this to update the Engineering Table.
   */
  override def onInventoryChanged
  {
    if (worldObj != null)
    {
      if (!worldObj.isRemote)
      {
        this.outputInventory(TileEngineeringTable.CRAFTING_OUTPUT_SLOT) = null
        var didCraft: Boolean = false
        val inventoryCrafting: InventoryCrafting = this.getCraftingMatrix
        val matrixOutput: ItemStack = CraftingManager.getInstance.findMatchingRecipe(inventoryCrafting, this.worldObj)
        if (matrixOutput != null && this.getCraftingManager.getIdealRecipe(matrixOutput) != null)
        {
          this.outputInventory(TileEngineeringTable.CRAFTING_OUTPUT_SLOT) = matrixOutput
          didCraft = true
        }
        if (!didCraft)
        {
          val filterStack: ItemStack = craftingMatrix(TileEngineeringTable.CENTER_SLOT)
          if (filterStack != null && filterStack.getItem.isInstanceOf[ItemImprint])
          {
            val filters: java.util.List[ItemStack] = ItemImprint.getFilters(filterStack)
            for (o <- filters)
            {
              val outputStack: ItemStack = o
              if (outputStack != null)
              {
                val idealRecipe: Pair[ItemStack, Array[ItemStack]] = this.getCraftingManager.getIdealRecipe(outputStack)
                if (idealRecipe != null)
                {
                  val recipeOutput: ItemStack = idealRecipe.left
                  if (recipeOutput != null & recipeOutput.stackSize > 0)
                  {
                    this.outputInventory(TileEngineeringTable.CRAFTING_OUTPUT_SLOT) = recipeOutput
                    didCraft = true
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
                    return
                  }
                }
              }
            }
          }
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      }
    }
  }

  /**
   * Gets the AutoCraftingManager that does all the crafting results
   */
  def getCraftingManager: AutoCraftingManager =
  {
    if (craftManager == null)
    {
      craftManager = new AutoCraftingManager(this)
    }
    return craftManager
  }

  /**
   * Construct an InventoryCrafting Matrix on the fly.
   *
   * @return
   */
  def getCraftingMatrix: InventoryCrafting =
  {
    val inventoryCrafting: InventoryCrafting = new InventoryCrafting(new ContainerDummy(this), 3, 3)

    for (i <- 0 until this.craftingMatrix.length)
    {
      inventoryCrafting.setInventorySlotContents(i, this.craftingMatrix(i))
    }
    return inventoryCrafting
  }

  /**
   * DO NOT USE THIS INTERNALLY. FOR EXTERNAL USE ONLY!
   */
  override def getStackInSlot(slot: Int): ItemStack =
  {
    if (slot < TileEngineeringTable.CRAFTING_MATRIX_END)
    {
      return this.craftingMatrix(slot)
    }
    else if (slot < TileEngineeringTable.CRAFTING_OUTPUT_END)
    {
      return outputInventory(slot - TileEngineeringTable.CRAFTING_MATRIX_END)
    }
    else if (slot < TileEngineeringTable.PLAYER_OUTPUT_END && invPlayer != null)
    {
      return this.invPlayer.getStackInSlot(slot - TileEngineeringTable.CRAFTING_OUTPUT_END)
    }
    else if (searchInventories)
    {
      var idDisplacement: Int = TileEngineeringTable.PLAYER_OUTPUT_END
      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val tile: TileEntity = toVectorWorld.add(dir).getTileEntity
        if (tile.isInstanceOf[IInventory])
        {
          val inventory: IInventory = tile.asInstanceOf[IInventory]
          val slotID: Int = slot - idDisplacement
          if (slotID >= 0 && slotID < inventory.getSizeInventory)
          {
            return inventory.getStackInSlot(slotID)
          }
          idDisplacement += inventory.getSizeInventory
        }
      }
    }
    return null
  }

  override def isItemValidForSlot(i: Int, itemstack: ItemStack): Boolean =
  {
    return true
  }

  override def getInventoryStackLimit: Int =
  {
    return 64
  }

  override def isUseableByPlayer(entityplayer: EntityPlayer): Boolean =
  {
    return if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) ne this) false else entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D
  }

  override def getAccessibleSlotsFromSide(side: Int): Array[Int] =
  {
    return new Array[Int](0)
  }

  /**
   * Auto-crafting methods.
   */
  override def canInsertItem(slot: Int, itemstack: ItemStack, side: Int): Boolean =
  {
    if (getStackInSlot(4) != null && getStackInSlot(4).getItem.isInstanceOf[ItemImprint])
    {
      return true
    }
    var minSize: Int = 64
    var optimalSlot: Int = -1

    for (i <- 0 to craftingMatrix.length)
    {
      val checkStack: ItemStack = getStackInSlot(i)
      if (checkStack != null && checkStack.isItemEqual(itemstack))
      {
        if (checkStack.stackSize < minSize || optimalSlot < 0)
        {
          optimalSlot = i
          minSize = checkStack.stackSize
        }
      }
    }
    return slot == optimalSlot
  }

  override def canExtractItem(slot: Int, itemstack: ItemStack, side: Int): Boolean =
  {
    val outputStack: ItemStack = getStackInSlot(TileEngineeringTable.CRAFTING_MATRIX_END)
    if (outputStack != null)
    {
      val idealRecipeItem: Pair[ItemStack, Array[ItemStack]] = this.getCraftingManager.getIdealRecipe(outputStack)
      val doubleResults: Array[ItemStack] = ArrayUtils.addAll(idealRecipeItem.right, idealRecipeItem.right: _*)
      if (!getCraftingManager.consumeItems(false, doubleResults: _*))
      {
        return false
      }
    }
    return slot == TileEngineeringTable.CRAFTING_MATRIX_END
  }

  def getCraftingInv: Array[Int] =
  {
    var slots: Array[Int] = TileEngineeringTable.craftingSlots
    if (playerSlots != null)
    {
      slots = ArrayUtils.addAll(playerSlots, slots: _*)
    }
    if (searchInventories)
    {
      var temporaryInvID: Int = TileEngineeringTable.PLAYER_OUTPUT_END
      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val tile: TileEntity = toVectorWorld.add(dir).getTileEntity(worldObj)
        if (tile.isInstanceOf[IInventory])
        {
          val inventory: IInventory = tile.asInstanceOf[IInventory]
          val nearbySlots: Array[Int] = new Array[Int](inventory.getSizeInventory)

          for (i <- 0 to inventory.getSizeInventory)
          {
            temporaryInvID = temporaryInvID + 1;
            nearbySlots(i) = temporaryInvID;

          }
          slots = ArrayUtils.addAll(nearbySlots, slots: _*)
        }
      }
    }
    return slots
  }

  override def renderDynamic(position: Vector3, frame: Float, pass: Int)
  {
    GL11.glPushMatrix
    RenderItemOverlayUtility.renderItemOnSides(TileEngineeringTable.this, getStackInSlot(9), position.x, position.y, position.z)
    RenderItemOverlayUtility.renderTopOverlay(TileEngineeringTable.this, craftingMatrix, getDirection, position.x, position.y - 0.1, position.z)
    GL11.glPopMatrix
  }

  override def getDirection: ForgeDirection =
  {
    return null
  }

  override def setDirection(direction: ForgeDirection)
  {
  }
}