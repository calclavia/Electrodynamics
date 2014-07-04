package mffs.base

/**
 * The various types of packets.
 * @author Calclavia
 */
object TilePacketType extends Enumeration
{
  val NONE, DESCRIPTION, FREQUENCY, FORTRON, TOGGLE_ACTIVATION, TOGGLE_MODE, INVENTORY, STRING, FXS, TOGGLE_MODE_2, TOGGLE_MODE_3, TOGGLE_MODE_4, FIELD, RENDER = Value
}