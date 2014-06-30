package mffs.fortron

/**
 * The force field transfer mode.
 */
object TransferMode extends Enumeration
{
  type TransferMode = Value
  val EQUALIZE, DISTRIBUTE, DRAIN, FILL = Value

  implicit class TransferModeValue(value: Value)
  {
    def toggle: TransferMode = TransferMode.values((value.id + 1) % TransferMode.values.size)
  }

}