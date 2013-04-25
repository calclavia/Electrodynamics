package mffs.api;

import java.util.List;

import mffs.api.security.IBiometricIdentifier;

/**
 * Applied to TileEntities that can be linked with a Biometric Identifier.
 * 
 * @author Calclavia
 * 
 */
public interface IBiometricIdentifierLink
{
	public IBiometricIdentifier getBiometricIdentifier();

	public List<IBiometricIdentifier> getBiometricIdentifiers();
}
