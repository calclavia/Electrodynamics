package edx.mechanical.mech.process.mixer

import java.util.{LinkedHashSet, List, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import edx.mechanical.mech.TileMechanical
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.{AxisAlignedBB, ResourceLocation}
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import net.minecraftforge.fluids.IFluidBlock
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._
import resonantengine.api.recipe.{MachineRecipes, RecipeType}
import resonantengine.lib.prefab.Timer
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.transform.rotation.Quaternion
import resonantengine.lib.transform.vector.Vector3

import scala.collection.JavaConversions._

/**
 * @author Calclavia
 */
object TileMixer
{
  final val PROCESS_TIME: Int = 12 * 20
  final val MIXER_ITEM_TIMER: Timer[EntityItem] = new Timer[EntityItem]
  @SideOnly(Side.CLIENT) val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "mixer.tcn"))
  @SideOnly(Side.CLIENT) var TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "mixer.png")
}

class TileMixer extends TileMechanical(Material.iron)
{
  private var areaBlockedFromMoving: Boolean = false

  //Constructor
  mechanicalNode = new MixerNode(this)
  isOpaqueCube = false
  normalRender = false
  customItemRender = true
  textureName = "material_metal_top"

  override def update
  {
    super.update
    if (!world.isRemote && ticks % 3 == 0)
    {
      this.areaBlockedFromMoving = checkIsBlocked
    }
    if (canWork)
    {
      doWork
    }
  }

  /** Checks to see if the area around the mixer is blocked (3x3 - excluding center)
    * @return true if there is a non-fluid block inside the bounds
    */
  def checkIsBlocked: Boolean =
  {
    for (x <- -1 to 1)
    {
      for (z <- -1 to 1)
      {
        if (x != 0 && z != 0)
        {
          val block: Block = toVector3.add(x, 0, z).getBlock(world)
          if (block != null && !(block.isInstanceOf[IFluidBlock]))
          {
            return true
          }
        }
      }
    }
    return false
  }

  /**
   * Can this machine work this tick?
   *
   * @return
   */
  def canWork: Boolean =
  {
    return mechanicalNode.angularVelocity != 0 && !areaBlockedFromMoving
  }

  def doWork
  {
    var didWork: Boolean = false
    val aabb: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.xCoord - 1, this.yCoord, this.zCoord - 1, this.xCoord + 2, this.yCoord + 1, this.zCoord + 2)
    val entities: List[_] = this.worldObj.getEntitiesWithinAABB(classOf[Entity], aabb)
    val processItems: Set[EntityItem] = new LinkedHashSet[EntityItem]
    for (obj <- entities)
    {
      val entity: Entity = obj.asInstanceOf[Entity]
      val originalPosition: Vector3 = new Vector3(entity)
      val relativePosition: Vector3 = originalPosition - toVector3.add(0.5)
      relativePosition.transform(new Quaternion(-mechanicalNode.angularVelocity, new Vector3(1, 0, 0)))
      val newPosition: Vector3 = toVector3 + 0.5 + relativePosition
      val difference: Vector3 = (newPosition - originalPosition) * 0.5
      entity.addVelocity(difference.x, difference.y, difference.z)
      entity.onGround = false
      if (entity.isInstanceOf[EntityItem])
      {
        if (MachineRecipes.instance.getOutput(RecipeType.MIXER.name, (entity.asInstanceOf[EntityItem]).getEntityItem).length > 0)
        {
          processItems.add(entity.asInstanceOf[EntityItem])
        }
      }
    }

    for (processingItem <- processItems)
    {
      if (!TileMixer.MIXER_ITEM_TIMER.containsKey(processingItem))
      {
        TileMixer.MIXER_ITEM_TIMER.put(processingItem, TileMixer.PROCESS_TIME)
      }
      if (!processingItem.isDead && (toVector3 + 0.5).distance(new Vector3(processingItem)) < 2)
      {
        val timeLeft: Int = TileMixer.MIXER_ITEM_TIMER.decrease(processingItem)
        if (timeLeft <= 0)
        {
          if (doneWork(processingItem))
          {
            processingItem.getEntityItem.stackSize -= 1

            if (processingItem.getEntityItem.stackSize <= 0)
            {
              processingItem.setDead
              TileMixer.MIXER_ITEM_TIMER.remove(processingItem)
            }
            else
            {
              processingItem.setEntityItemStack(processingItem.getEntityItem)
              TileMixer.MIXER_ITEM_TIMER.put(processingItem, TileMixer.PROCESS_TIME)
            }
          }
        }
        else
        {
          processingItem.delayBeforeCanPickup = 20
          this.worldObj.spawnParticle("bubble", processingItem.posX, processingItem.posY, processingItem.posZ, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3)
        }
        didWork = true
      }
      else
      {
        TileMixer.MIXER_ITEM_TIMER.remove(processingItem)
      }
    }
    if (didWork)
    {
      if (this.ticks % 20 == 0)
      {
        this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.prefix + "mixer", 0.5f, 1)
      }
    }
  }

  private def doneWork(entity: EntityItem): Boolean =
  {
    val mixPosition: Vector3 = new Vector3(entity.posX, yCoord, entity.posZ)
    if (mixPosition.getBlock(world) ne getBlockType)
    {
      val block: Block = mixPosition.getBlock(worldObj)
      val blockFluidFinite: Block = null //RIResourceFactory.getMixture(ResonantEngine.resourceFactory.getName(entity.getEntityItem))
      if (blockFluidFinite != null)
      {
        /*if (block.isInstanceOf[BlockFluidMixture])
        {
          val itemStack: ItemStack = entity.getEntityItem.copy
          if (block.asInstanceOf[BlockFluidMixture].mix(worldObj, mixPosition.xi, mixPosition.yi, mixPosition.zi, itemStack))
          {
            worldObj.notifyBlocksOfNeighborChange(mixPosition.xi, mixPosition.yi, mixPosition.zi, mixPosition.getBlock(worldObj))
            return true
          }
        }
        else if (block != null && (block == Blocks.water || block == Blocks.flowing_water))
        {
          mixPosition.setBlock(worldObj, blockFluidFinite)
        }*/
      }
    }
    return false
  }

  override def renderDynamic(position: Vector3, frame: Float, pass: Int): Unit =
  {
    GL11.glPushMatrix()
    GL11.glTranslated(position.x + 0.5, position.y + 0.5, position.z + 0.5)
    RenderUtility.bind(RenderMixer.TEXTURE)
    RenderMixer.MODEL.renderOnly("centerTop", "centerBase")
    glPushMatrix()
    glRotatef(Math.toDegrees(mechanicalNode.angle.asInstanceOf[Float]).asInstanceOf[Float], 0, 1, 0)
    RenderMixer.MODEL.renderAllExcept("centerTop", "centerBase")
    glPopMatrix()
    GL11.glPopMatrix()
  }

  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    GL11.glScalef(0.7f, 0.7f, 0.7f)
    glTranslatef(0.5F, 0.5f, 0.5f)
    RenderUtility.bind(RenderMixer.TEXTURE)
    RenderMixer.MODEL.renderAll()
    glPopMatrix()
  }
}