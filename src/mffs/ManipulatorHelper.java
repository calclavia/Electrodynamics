package mffs;

import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * Helper class for the Force Manipulator
 * 
 * @author Calclavia
 * @author Based off of Jakj's code. Licensed under WTFPL.
 * 
 */
public class ManipulatorHelper
{
	/**
	 * Obfuscation names for reflection
	 */
	public static final String[] CHUNK_RELIGHT_BLOCK = { "relightBlock", "func_76615_h" };
	public static final String[] CHUNK_PROPOGATE_SKY_LIGHT_OCCLUSION = { "propagateSkylightOcclusion", "func_76595_e" };

	/**
	 * Sets a block in a sneaky way to bypass some restraints.
	 * 
	 * @param world
	 * @param position
	 * @param id
	 * @param metadata
	 * @param tileEntity
	 */
	public static void setBlockSneaky(World world, Vector3 position, int id, int metadata, TileEntity tileEntity)
	{
		Chunk chunk = world.getChunkFromChunkCoords(position.intX() >> 4, position.intZ() >> 4);
		Vector3 chunkPosition = new Vector3(position.intX() & 0xF, position.intY() & 0xF, position.intZ() & 0xF);

		int heightMapIndex = chunkPosition.intZ() << 4 | chunkPosition.intX();

		if (position.intY() >= chunk.precipitationHeightMap[heightMapIndex] - 1)
		{
			chunk.precipitationHeightMap[heightMapIndex] = -999;
		}

		int heightMapValue = chunk.heightMap[heightMapIndex];

		world.removeBlockTileEntity(position.intX(), position.intY(), position.intZ());

		ExtendedBlockStorage extendedBlockStorage = chunk.getBlockStorageArray()[position.intY() >> 4];

		if (extendedBlockStorage == null)
		{
			extendedBlockStorage = new ExtendedBlockStorage((position.intY() >> 4) << 4, !world.provider.hasNoSky);

			chunk.getBlockStorageArray()[position.intY() >> 4] = extendedBlockStorage;
		}

		extendedBlockStorage.setExtBlockID(chunkPosition.intX(), chunkPosition.intY(), chunkPosition.intZ(), id);
		extendedBlockStorage.setExtBlockMetadata(chunkPosition.intX(), chunkPosition.intY(), chunkPosition.intZ(), metadata);

		if (position.intY() >= heightMapValue)
		{
			chunk.generateSkylightMap();
		}
		else
		{
			if (chunk.getBlockLightOpacity(chunkPosition.intX(), position.intY(), chunkPosition.intZ()) > 0)
			{
				if (position.intY() >= heightMapValue)
				{
					relightBlock(chunk, Vector3.translate(chunkPosition, new Vector3(0, 1, 0)));
				}
			}
			else if (position.intY() == heightMapValue - 1)
			{
				relightBlock(chunk, chunkPosition);
			}

			propagateSkylightOcclusion(chunk, chunkPosition);
		}

		chunk.isModified = true;

		world.updateAllLightTypes(position.intX(), position.intY(), position.intZ());

		if (tileEntity != null)
		{
			world.setBlockTileEntity(position.intX(), position.intY(), position.intZ(), tileEntity);
		}

		world.markBlockForUpdate(position.intX(), position.intY(), position.intZ());
	}

	/**
	 * Re-lights the block in a specific position.
	 * 
	 * @param chunk
	 * @param position
	 */
	public static void relightBlock(Chunk chunk, Vector3 position)
	{
		try
		{
			Method m = ReflectionHelper.findMethod(Chunk.class, null, CHUNK_RELIGHT_BLOCK, int.class, int.class, int.class);
			m.invoke(chunk, position.intX(), position.intY(), position.intZ());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Propogates skylight occlusion in a specific chunk's position.
	 * 
	 * @param chunk
	 * @param position
	 */
	public static void propagateSkylightOcclusion(Chunk chunk, Vector3 position)
	{
		try
		{
			Method m = ReflectionHelper.findMethod(Chunk.class, null, CHUNK_PROPOGATE_SKY_LIGHT_OCCLUSION, int.class, int.class);
			m.invoke(chunk, position.intX(), position.intZ());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
