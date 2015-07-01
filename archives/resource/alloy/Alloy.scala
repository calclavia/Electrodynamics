package com.calclavia.edx.core.resource.alloy

import net.minecraft.nbt.NBTTagCompound
import resonantengine.api.misc.ISave
import resonantengine.lib.wrapper.NBTWrapper._

/**
 * A class that stores alloy objects. Alloys are materials that are composed of other materials.
 * @author Calclavia
 */
class Alloy(val max: Int) extends ISave
{
  var content = Map.empty[String, Int]

  def this(nbt: NBTTagCompound, max: Int = 8)
  {
    this(max)
    load(nbt)
  }

  override def load(nbt: NBTTagCompound)
  {
    content = nbt.getMap("mixture")
  }

  def percentage(material: String): Float = content(material) / size.toFloat

  def percentage = size / max.toFloat

  /**
   * Mixes a dust material into this jar
   */
  def mix(material: String): Boolean =
  {
    if (size < max)
    {
      content += material -> (content.getOrElse(material, 0) + 1)
      return true
    }
    return false
  }

  def color = AlloyUtility.mixedColor(content.map(keyVal => (keyVal._1, keyVal._2 / size.toFloat)))

  def size = content.values.foldLeft(0)(_ + _)

  override def save(nbt: NBTTagCompound)
  {
    nbt.setMap("mixture", content)
  }
}
