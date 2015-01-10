package resonantinduction.core.resource

import java.awt.Color

import resonant.lib.factory.resources.ResourceFactory

/**
 * Manages alloys and metal mixtures
 * @author Calclavia
 */
object AlloyUtility
{
  /**
   * Weighted material mixture color
   * @param materials - Gets the color of the mixture with weighted percentages
   * @return
   */
  def mixedColor(materials: Map[String, Float]): Int =
  {
    val colorMap = materials.map(keyVal => (keyVal._1, new Color(ResourceFactory.getColor(keyVal._1))))
    val averageRGB = colorMap
      .map(keyVal => (keyVal._2.getRed * materials(keyVal._1), keyVal._2.getGreen * materials(keyVal._1), keyVal._2.getBlue * materials(keyVal._1)))
      .foldLeft((0f, 0f, 0f))((b, a) => (a._1 + b._1, a._2 + b._2, a._3 + b._3))
    return new Color(averageRGB._1.toInt, averageRGB._2.toInt, averageRGB._3.toInt).getRGB()
  }

  def mixedColor(materials: Seq[String]): Int =
  {
    val colors = materials.map(ResourceFactory.getColor).map(new Color(_))
    val totalRGB = colors.map(c => (c.getRed, c.getGreen, c.getBlue)).foldLeft((0f, 0f, 0f))((b, a) => (a._1 + b._1, a._2 + b._2, a._3 + b._3))
    val averageRGB = (totalRGB._1 / materials.size, totalRGB._2 / materials.size, totalRGB._3 / materials.size)
    return new Color(averageRGB._1.toInt, averageRGB._2.toInt, averageRGB._3.toInt).getRGB()
  }

}
