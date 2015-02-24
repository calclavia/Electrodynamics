package mffs.util;

/**
 * @author Calclavia
 */
public enum TransferMode {
	equalize, distribute, drain, fill;

	public TransferMode toggle() {
		return TransferMode.values()[ordinal() + 1 % TransferMode.values().length];
	}
}
