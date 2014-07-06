package mffs.util

/**
 * The force field transfer mode.
 */
object TransferMode extends Enumeration
{
  type TransferMode = Value
  val EQUALIZE, DISTRIBUTE, DRAIN, FILL = Value

  implicit class TransferModeValue(value: Value)
  {
    def toggle: TransferMode = TransferMode((value.id + 1) % TransferMode.values.size)
  }

}