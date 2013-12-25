package mffs.block;

import mffs.tileentity.TileBiometricIdentifier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBiometricIdentifier extends BlockMachineBlock
{
	public BlockBiometricIdentifier(int i)
	{
		super(i, "biometricIdentifier");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileBiometricIdentifier();
	}
}