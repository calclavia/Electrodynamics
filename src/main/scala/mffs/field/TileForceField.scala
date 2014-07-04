package mffs.field

import com.google.common.io.ByteArrayDataInput
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.field.TileElectromagnetProjector
import mffs.security.access.MFFSPermissions
import mffs.{MFFSHelper, ModularForceFieldSystem}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{Entity, EntityLiving}
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.potion.{Potion, PotionEffect}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraft.world.{IBlockAccess, World}
import resonant.api.mffs.IProjector
import resonant.api.mffs.fortron.IFortronStorage
import resonant.api.mffs.modules.IModule
import resonant.content.spatial.block.SpatialTile
import resonant.lib.network.IPacketReceiver
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class TileForceField extends SpatialTile(Material.glass) with IPacketReceiver
{
  private var camoStack: ItemStack = null
  private var projector: Nothing = null

  /**
   * Constructor
   */
  blockHardness = -1
  blockResistance = Integer.MAX_VALUE
  creativeTab = null
  isOpaqueCube = false
  normalRender = false

  override def canSilkHarvest(player: EntityPlayer, metadata: Int): Boolean = false

  override def quantityDropped(meta: Int, fortune: Int): Int = 0

  /**
   * Rendering
   */
  override def getRenderBlockPass: Int = 1

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3, pass: Int): Boolean =
  {
    var renderType: Int = 0
    var camoBlock: Block = null
    val tileEntity: TileEntity = iBlockAccess.getTileEntity(x, y, z)

    if (camoStack != null && camoStack.getItem().isInstanceOf[ItemBlock])
    {
      camoBlock = camoStack.getItem().asInstanceOf[ItemBlock].field_150939_a

      if (camoBlock != null)
      {
        renderType = camoBlock.getRenderType()
      }
    }

    if (renderType >= 0)
    {
      try
      {
        if (camoBlock != null)
        {
          renderer.setRenderBoundsFromBlock(camoBlock)
        }

        renderType match
        {
          case 4 =>
            renderer.renderBlockFluids(block, x, y, z)
          case 31 =>
            renderer.renderBlockLog(block, x, y, z)
          case 1 =>
            renderer.renderCrossedSquares(block, x, y, z)
          case 20 =>
            renderer.renderBlockVine(block, x, y, z)
          case 39 =>
            renderer.renderBlockQuartz(block, x, y, z)
          case 5 =>
            renderer.renderBlockRedstoneWire(block, x, y, z)
          case 13 =>
            renderer.renderBlockCactus(block, x, y, z)
          case 23 =>
            renderer.renderBlockLilyPad(block, x, y, z)
          case 6 =>
            renderer.renderBlockCrops(block, x, y, z)
          case 8 =>
            renderer.renderBlockLadder(block, x, y, z)
          case 7 =>
            renderer.renderBlockDoor(block, x, y, z)
          case 12 =>
            renderer.renderBlockLever(block, x, y, z)
          case 29 =>
            renderer.renderBlockTripWireSource(block, x, y, z)
          case 30 =>
            renderer.renderBlockTripWire(block, x, y, z)
          case 14 =>
            renderer.renderBlockBed(block, x, y, z)
          case 16 =>
            renderer.renderPistonBase(block, x, y, z, false)
          case 17 =>
            renderer.renderPistonExtension(block, x, y, z, true)
          case _ =>
            renderer.renderStandardBlock(block, x, y, z)
        }
      }
      catch
        {
          case e: Exception =>
          {
            if (camoStack != null && camoBlock != null)
            {
              renderer.renderBlockAsItem(camoBlock, camoStack.getItemDamage, 1)
            }
          }
        }

      return true
    }

    return false
  }

  /**
   * Block Logic
   */
  @SideOnly(Side.CLIENT)
  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean =
  {
    if (camoStack != null)
    {
      try
      {
        val block: Block = Block.blocksList(((tileEntity.asInstanceOf[TileForceField]).camoStack.getItem.asInstanceOf[ItemBlock]).getBlockID)
        return block.shouldSideBeRendered(world, x, y, z, par5)
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
      return true
    }

    val i1: Int = world.getBlockId(x, y, z)
    return if (i1 == this.blockID) false else super.shouldSideBeRendered(world, x, y, z, par5)
  }

  override def click(player: EntityPlayer)
  {
    val projector = getProjector()

    if (projector != null)
    {
      projector.getModuleStacks(projector.getModuleSlots()).forall(stack => stack.getItem().asInstanceOf[IModule].onCollideWithForceField(world, x, y, z, entity, stack))
    }
  }

  override def getCollisionBoxes(intersect: Cuboid, entity: Entity): Iterable[Cuboid] =
  {
    //TODO: Check if the entity filter actually works...
    val projector = getProjector()

    if (projector != null && entity.isInstanceOf[EntityPlayer])
    {
      val biometricIdentifier = projector.getBiometricIdentifier()

      val entityPlayer = entity.asInstanceOf[EntityPlayer]

      if (entityPlayer.isSneaking)
      {
        if (entityPlayer.capabilities.isCreativeMode)
        {
          return null
        }
        else if (biometricIdentifier != null)
        {
          if (biometricIdentifier.hasPermission(entityPlayer.username, MFFSPermissions.forceFieldWrap))
          {
            return null
          }
        }
      }
    }

    return super.getCollisionBoxes(intersect, entity)
  }

  override def collide(entity: Entity)
  {
    val projector = getProjector()

    if (projector != null)
    {
      if (!projector.getModuleStacks(projector.getModuleSlots()).forall(stack => stack.getItem().asInstanceOf[IModule].onCollideWithForceField(world, x, y, z, entity, stack)))
        return

      val biometricIdentifier = projector.getBiometricIdentifier()

      if (center.distance(new Vector3(entity)) < 0.5)
      {
        if (!world.isRemote && entity.isInstanceOf[EntityLiving])
        {
          val entityLiving = entity.asInstanceOf[EntityLiving]

          entityLiving.addPotionEffect(new PotionEffect(Potion.confusion.id, 4 * 20, 3))
          entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 1))

          if (entity.isInstanceOf[EntityPlayer])
          {
            val player = entity.asInstanceOf[EntityPlayer]

            if (player.isSneaking)
            {
              if (player.capabilities.isCreativeMode)
              {
                return
              }
              else if (biometricIdentifier != null)
              {
                if (biometricIdentifier.hasPermission(entityPlayer.username, MFFSPermissions.forceFieldWrap))
                {
                  return
                }
              }
            }
          }

          entity.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, 100)
        }
      }
    }

  }

  @SideOnly(Side.CLIENT)
  override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    if (camoStack != null)
    {
      try
      {
        val block = camoStack.getItem().asInstanceOf[ItemBlock].field_150939_a
        val icon = block.getIcon(side, camoStack.getItemDamage)

        if (icon != null)
        {
          return icon
        }
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
    }

    return super.getIcon(access, side)
  }

  /**
   * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color.
   * Note only called when first determining what to render.
   */
  def colorMultiplier(): Int =
  {
    if (camoStack != null)
    {
      try
      {
        return camoStack.getItem().asInstanceOf[ItemBlock].field_150939_a.colorMultiplier(access, x, y, x)
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
    }
    return super.colorMultiplier()
  }

  def getLightValue(access: IBlockAccess): Int =
  {
    try
    {
      val projector = getProjectorSafe()
      if (projector != null)
      {
        return ((Math.min(projector.getModuleCount(ModularForceFieldSystem.itemModuleGlow), 64).asInstanceOf[Float] / 64) * 15f).toInt
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }

    return 0
  }

  def getExplosionResistance(entity: Entity, world: World, x: Int, y: Int, z: Int, d: Double, d1: Double, d2: Double): Float =
  {
    return Integer.MAX_VALUE
  }

  def weakenForceField(world: World, x: Int, y: Int, z: Int, joules: Int)
  {
    val projector: IProjector = this.getProjector(world, x, y, z)
    if (projector != null)
    {
      (projector.asInstanceOf[IFortronStorage]).provideFortron(joules, true)
    }
    if (!world.isRemote)
    {
      world.setBlockToAir(x, y, z)
    }
  }

  override def getPickBlock(target: MovingObjectPosition): ItemStack = null

  /**
   * Tile Logic
   */
  override def canUpdate: Boolean = false

  override def getDescriptionPacket: Packet =
  {
    if (getProjector() != null)
    {
      var itemID: Int = -1
      var itemMetadata: Int = -1
      if (camoStack != null)
      {
        itemID = camoStack.itemID
        itemMetadata = camoStack.getItemDamage
      }
      return ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.projector.intX, this.projector.intY, this.projector.intZ, itemID, itemMetadata)
    }

    return null
  }

  override def onReceivePacket(data: ByteArrayDataInput, player: EntityPlayer, obj: AnyRef*)
  {
    try
    {
      this.setProjector(new Nothing(data.readInt, data.readInt, data.readInt))
      this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord)
      this.camoStack = null
      val itemID: Int = data.readInt
      val itemMetadata: Int = data.readInt
      if (itemID != -1 && itemMetadata != -1)
      {
        this.camoStack = new ItemStack(Block.blocksList(itemID), 1, itemMetadata)
      }
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
   * @return Gets the projector block controlling this force field. Removes the force field if no
   *         projector can be found.
   */
  def getProjector(): TileElectromagnetProjector =
  {
    if (this.getProjectorSafe != null)
    {
      return getProjectorSafe
    }

    if (!this.worldObj.isRemote)
    {
      this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, 0)
    }

    return null
  }

  def getProjectorSafe: TileElectromagnetProjector =
  {
    if (this.projector != null)
    {
      if (this.projector.getTileEntity(this.worldObj).isInstanceOf[TileElectromagnetProjector])
      {
        if (worldObj.isRemote || (projector.getTileEntity(this.worldObj).asInstanceOf[IProjector]).getCalculatedField.contains(new Nothing(this)))
        {
          return this.projector.getTileEntity(this.worldObj).asInstanceOf[TileElectromagnetProjector]
        }
      }
    }
    return null
  }

  def setProjector(position: Vector3)
  {
    projector = position

    if (!world.isRemote)
    {
      refreshCamoBlock()
    }
  }

  /**
   * Server Side Only
   */
  def refreshCamoBlock
  {
    if (this.getProjectorSafe != null)
    {
      camoStack = MFFSHelper.getCamoBlock(this.getProjector, new Vector3(this))
    }
  }

  def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.projector = new Nothing(nbt.getCompoundTag("projector"))
  }

  /**
   * Writes a tile entity to NBT.
   */
  def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    if (this.getProjector != null)
    {
      nbt.setCompoundTag("projector", this.projector.writeToNBT(new NBTTagCompound))
    }
  }
}