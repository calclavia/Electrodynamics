package com.calclavia.edx.mechanical.mech.process.grinder

import com.calclavia.edx.mechanical.mech.TileMechanical
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{Electrodynamics, Reference}
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.util.{DamageSource, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonantengine.api.edx.recipe.{MachineRecipes, RecipeType}
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.transform.region.Cuboid
import resonantengine.lib.transform.rotation.AngleAxis
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.misc.Timer

/**
 * The grinding wheel. This block will face the direction in which it can rotate.
 * @author Calclavia
 */
object TileGrindingWheel
{
  final val processTime = 20 * 20
  /**
   * A map of ItemStacks and their remaining grind-time left.
   */
  final val grindingTimer = new Timer[EntityItem]

  @SideOnly(Side.CLIENT)
  final val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "grinder.obj"))
}

class TileGrindingWheel extends TileMechanical(Material.rock)
{
  private final val requiredTorque: Long = 250
  private var grindingItem: EntityItem = null
  private var counter = 0d

  mechanicalNode = new NodeGrinder(this)
  bounds = new Cuboid(0.05f, 0.05f, 0.05f, 0.95f, 0.95f, 0.95f)
  isOpaqueCube = false
  normalRender = false
  textureName = "material_steel_dark"
  rotationMask = 0x3F

  override def update()
  {
    super.update()
    counter = Math.max(counter + Math.abs(mechanicalNode.torque), 0)
    doWork()
  }

  def doWork()
  {
    if (canWork)
    {
      var didWork = false

      if (grindingItem != null)
      {
        if (TileGrindingWheel.grindingTimer.containsKey(grindingItem) && !grindingItem.isDead && position.add(0.5).distance(new Vector3(grindingItem)) < 1)
        {
          val timeLeft: Int = TileGrindingWheel.grindingTimer.decrease(grindingItem)
          if (timeLeft <= 0)
          {
            if (this.doGrind(grindingItem))
            {
              grindingItem.getEntityItem.stackSize -= 1;
              if (grindingItem.getEntityItem.stackSize <= 0)
              {
                grindingItem.setDead()
                TileGrindingWheel.grindingTimer.remove(grindingItem)
                grindingItem = null
              }
              else
              {
                grindingItem.setEntityItemStack(grindingItem.getEntityItem)
                TileGrindingWheel.grindingTimer.put(grindingItem, TileGrindingWheel.processTime)
              }
            }
          }
          else
          {
            grindingItem.delayBeforeCanPickup = 20
            if (grindingItem.getEntityItem.getItem.isInstanceOf[ItemBlock])
            {
              Electrodynamics.proxy.renderBlockParticle(worldObj, new Vector3(grindingItem), new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), 3, 1)
            }
            else
            {
              worldObj.spawnParticle("crit", grindingItem.posX, grindingItem.posY, grindingItem.posZ, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3)
            }
          }
          didWork = true
        }
        else
        {
          TileGrindingWheel.grindingTimer.remove(grindingItem)
          grindingItem = null
        }
      }
      if (didWork)
      {
        if (this.ticks % 8 == 0)
        {
          worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.prefix + "grinder", 0.5f, 1)
        }
        counter -= requiredTorque
      }
    }
  }

  private def doGrind(entity: EntityItem): Boolean =
  {
    val itemStack: ItemStack = entity.getEntityItem
    val results = MachineRecipes.instance.getOutput(RecipeType.SIFTER.name, itemStack)

    for (resource <- results)
    {
      val outputStack: ItemStack = resource.getItemStack

      if (!world.isRemote)
      {
        val entityItem: EntityItem = new EntityItem(this.worldObj, entity.posX, entity.posY - 1.2, entity.posZ, outputStack)
        entityItem.delayBeforeCanPickup = 20
        entityItem.motionX = 0
        entityItem.motionY = 0
        entityItem.motionZ = 0
        world.spawnEntityInWorld(entityItem)
      }
    }
    return results.length > 0
  }

  /**
   * Can this machine work this tick?
   */
  def canWork: Boolean = counter >= requiredTorque

  override def collide(entity: Entity)
  {
    if (entity.isInstanceOf[EntityItem])
    {
      entity.asInstanceOf[EntityItem].age -= 1
    }

    if (canWork)
    {
      if (entity.isInstanceOf[EntityItem])
      {
        if (canGrind(entity.asInstanceOf[EntityItem].getEntityItem))
        {
          if (grindingItem == null)
          {
            grindingItem = entity.asInstanceOf[EntityItem]
          }
          if (!TileGrindingWheel.grindingTimer.containsKey(entity.asInstanceOf[EntityItem]))
          {
            TileGrindingWheel.grindingTimer.put(entity.asInstanceOf[EntityItem], TileGrindingWheel.processTime)
          }
        }
      }
      else
      {
        entity.attackEntityFrom(new DamageSource("grinder"), 2)
      }
    }

    if (mechanicalNode.angularVelocity != 0)
    {
      //The velocity added should be tangent to the circle
      val deltaVector = new Vector3(entity) - center
      val deltaAngle = Math.toDegrees(mechanicalNode.angularVelocity / 20)
      var dir = getDirection
      dir = ForgeDirection.getOrientation(if (dir.ordinal() % 2 != 0) dir.ordinal() - 1 else dir.ordinal()).getOpposite
      val rotation = new AngleAxis(deltaAngle, new Vector3(dir))
      val deltaPos = deltaVector.transform(rotation) - deltaVector
      val velocity = deltaPos / 20
      entity.addVelocity(velocity.x, velocity.y, velocity.z)
    }
  }

  def canGrind(itemStack: ItemStack): Boolean = MachineRecipes.instance.getOutput(RecipeType.SIFTER.name, itemStack).length > 0

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int): Unit =
  {
    glPushMatrix()
    glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
    glScalef(0.51f, 0.5f, 0.5f)

    var dir = getDirection
    dir = ForgeDirection.getOrientation(if (dir.ordinal() % 2 != 0) dir.ordinal() - 1 else dir.ordinal())

    if (dir.offsetY == 0)
      glRotated(90, 0, 1, 0)
    else
      glRotated(90, 0, 1, 0)

    RenderUtility.rotateBlockBasedOnDirection(dir)
    glRotated(Math.toDegrees(mechanicalNode.angle), 0, 0, 1)
    RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
    TileGrindingWheel.model.renderAllExcept("teeth")
    RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
    TileGrindingWheel.model.renderOnly("teeth")
    glPopMatrix()
  }
}