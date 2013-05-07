package mffs;

/**
 * The force field transfer mode.
 */
public enum TransferMode
{
	EQUALIZE, DISTRIBUTE, DRAIN, FILL;

	public TransferMode toggle()
	{
		int newOrdinal = this.ordinal() + 1;

		if (newOrdinal >= TransferMode.values().length)
		{
			newOrdinal = 0;
		}
		return TransferMode.values()[newOrdinal];
	}

}