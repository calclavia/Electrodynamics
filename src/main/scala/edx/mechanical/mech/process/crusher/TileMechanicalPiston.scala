package edx.mechanical.mech.process.crusher

import java.lang.reflect.Method

import edx.core.Electrodynamics
import edx.mechanical.mech.TileMechanical
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.api.recipe.{MachineRecipes, RecipeResource, RecipeType}
import resonant.lib.mod.config.Config
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.MovementUtility
import resonant.lib.utility.inventory.InventoryUtility

/**
 * Mechanical driven piston that can be used to move basic blocks and crush ores
 * @author Calclavia
 */
object TileMechanicalPiston
{
  @Config private var mechanicalPistonMultiplier: Int = 2
}

class TileMechanicalPiston extends TileMechanical(Material.piston)
{
  var markRevolve: Boolean = false

  //Constructor
  mechanicalNode = new NodeMechanicalPiston(this)
  isOpaqueCube = false
  normalRender = false
  customItemRender = true
  rotationMask = 63
  setTextureName("material_steel_dark")

  override def update
  {
    super.update
    if (markRevolve)
    {
      val movePosition: Vector3 = toVector3.add(getDirection)
      if (!hitOreBlock(movePosition))
      {
        if (!worldObj.isRemote)
        {
          val moveNewPosition: Vector3 = movePosition.clone.add(getDirection)
          if (canMove(movePosition, moveNewPosition))
          {
            move(movePosition, moveNewPosition)
          }
        }
      }
      markRevolve = false
    }
  }

  def hitOreBlock(blockPos: Vector3): Boolean =
  {
    val block: Block = blockPos.getBlock(world)
    if (block != null)
    {
      val blockStack: ItemStack = new ItemStack(block)
      val resources: Array[RecipeResource] = MachineRecipes.instance.getOutput(RecipeType.GRINDER.name, blockStack)
      if (resources.length > 0)
      {
        if (!worldObj.isRemote)
        {
          for (recipe <- resources)
          {
            if (Math.random <= recipe.getChance)
            {
              InventoryUtility.dropItemStack(world, blockPos.clone.add(0.5), recipe.getItemStack, 10, 0)
            }
          }
          blockPos.setBlockToAir(world)
        }
        Electrodynamics.proxy.renderBlockParticle(worldObj, blockPos.clone.add(0.5), new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), Block.getIdFromBlock(block), 1)
        return true
      }
    }
    if (!worldObj.isRemote)
    {
      world.destroyBlockInWorldPartially(0, blockPos.xi, blockPos.yi, blockPos.zi, -1)
    }
    return false
  }

  def canMove(from: Vector3, to: Vector3): Boolean =
  {
    if (this == to.getTileEntity(getWorldObj))
    {
      return false
    }
    val targetBlock: Block = to.getBlock(worldObj)
    if (!(worldObj.isAirBlock(to.xi, to.yi, to.zi) || (targetBlock != null && (targetBlock.canBeReplacedByLeaves(worldObj, to.xi, to.yi, to.zi)))))
    {
      return false
    }
    return true
  }

  def move(from: Vector3, to: Vector3)
  {
    val blockID: Block = from.getBlock(worldObj)
    val blockMetadata: Int = from.getBlockMetadata(worldObj)
    val tileEntity: TileEntity = from.getTileEntity(worldObj)
    val tileData: NBTTagCompound = new NBTTagCompound
    if (tileEntity != null)
    {
      tileEntity.writeToNBT(tileData)
    }
    MovementUtility.setBlockSneaky(worldObj, from, null, 0, null)
    if (tileEntity != null && tileData != null)
    {
      val isMultipart: Boolean = tileData.getString("id") == "savedMultipart"
      var newTile: TileEntity = null
      if (isMultipart)
      {
        try
        {
          val multipart: Class[_] = Class.forName("codechicken.multipart.MultipartHelper")
          val m: Method = multipart.getMethod("createTileFromNBT", classOf[World], classOf[NBTTagCompound])
          newTile = m.invoke(null, worldObj, tileData).asInstanceOf[TileEntity]
        }
        catch
          {
            case e: Exception =>
            {
              e.printStackTrace
            }
          }
      }
      else
      {
        newTile = TileEntity.createAndLoadEntity(tileData)
      }
      MovementUtility.setBlockSneaky(worldObj, to, blockID, blockMetadata, newTile)
      if (newTile != null && isMultipart)
      {
        try
        {
          val multipart: Class[_] = Class.forName("codechicken.multipart.MultipartHelper")
          multipart.getMethod("sendDescPacket", classOf[World], classOf[TileEntity]).invoke(null, worldObj, newTile)
          val tileMultipart: Class[_] = Class.forName("codechicken.multipart.TileMultipart")
          tileMultipart.getMethod("onMoved").invoke(newTile)
        }
        catch
          {
            case e: Exception =>
            {
              e.printStackTrace
            }
          }
      }
    }
    else
    {
      MovementUtility.setBlockSneaky(worldObj, to, blockID, blockMetadata, null)
    }
    notifyChanges(from)
    notifyChanges(to)
  }

  def notifyChanges(pos: Vector3)
  {
    worldObj.notifyBlocksOfNeighborChange(pos.xi, pos.yi, pos.zi, pos.getBlock(worldObj))
    /* val newTile: TileEntity = pos.getTileEntity(worldObj)
    if (newTile != null)
    {
        if (Loader.isModLoaded("BuildCraft|Factory"))
        {
            try
            {
                val clazz: Class[_ <: Any] = Class.forName("buildcraft.factory.TileQuarry")
                if (newTile == clazz)
                {
                    ReflectionHelper.setPrivateValue(clazz, newTile, true, "isAlive")
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
    } */
  }
}