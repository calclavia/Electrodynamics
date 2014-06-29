package mffs.fortron

/**
 * The force field transfer mode.
 */
object TransferMode extends Enumeration
{
  type TransferMode = Value
  val EQUALIZE, DISTRIBUTE, DRAIN, FILL = Value

  def toggle: TransferMode =
  {
    var newOrdinal = this.ordinal + 1

    if (newOrdinal >= TransferMode.values.length)
    {
      newOrdinal = 0
    }

    return TransferMode.values(newOrdinal)
  }
}