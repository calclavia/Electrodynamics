package mffs.render;

import mffs.tile.TileForceField;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import resonant.lib.render.RenderUtility;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderForceField implements ISimpleBlockRenderingHandler
{
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		RenderUtility.renderNormalBlockAsItem(block, metadata, renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess iBlockAccess, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		int renderType = 0;
		ItemStack camoStack = null;
		Block camoBlock = null;
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceField)
		{
			camoStack = ((TileForceField) tileEntity).camoStack;

			if (camoStack != null && camoStack.getItem() instanceof ItemBlock)
			{
				camoBlock = Block.blocksList[((ItemBlock) camoStack.getItem()).getBlockID()];

				if (camoBlock != null)
				{
					renderType = camoBlock.getRenderType();
				}
			}
		}

		if (renderType >= 0)
		{
			try
			{
				/**
				 * Set block bounds
				 */
				if (camoBlock != null)
				{
					renderer.setRenderBoundsFromBlock(camoBlock);
				}

				/**
				 * Render based on renderType.
				 */
				switch (renderType)
				{
					case 4:
						renderer.renderBlockFluids(block, x, y, z);
						break;
					case 31:
						renderer.renderBlockLog(block, x, y, z);
						break;
					case 1:
						renderer.renderCrossedSquares(block, x, y, z);
						break;
					case 20:
						renderer.renderBlockVine(block, x, y, z);
						break;
					case 39:
						renderer.renderBlockQuartz(block, x, y, z);
						break;
					case 5:
						renderer.renderBlockRedstoneWire(block, x, y, z);
						break;
					case 13:
						renderer.renderBlockCactus(block, x, y, z);
						break;
					case 23:
						renderer.renderBlockLilyPad(block, x, y, z);
						break;
					case 6:
						renderer.renderBlockCrops(block, x, y, z);
						break;
					case 8:
						renderer.renderBlockLadder(block, x, y, z);
						break;
					case 7:
						renderer.renderBlockDoor(block, x, y, z);
						break;
					case 12:
						renderer.renderBlockLever(block, x, y, z);
						break;
					case 29:
						renderer.renderBlockTripWireSource(block, x, y, z);
						break;
					case 30:
						renderer.renderBlockTripWire(block, x, y, z);
						break;
					case 14:
						renderer.renderBlockBed(block, x, y, z);
						break;
					case 16:
						renderer.renderPistonBase(block, x, y, z, false);
						break;
					case 17:
						renderer.renderPistonExtension(block, x, y, z, true);
						break;
					default:
						renderer.renderStandardBlock(block, x, y, z);
						break;
				}
			}
			catch (Exception e)
			{
				if (camoStack != null && camoBlock != null)
				{
					renderer.renderBlockAsItem(camoBlock, camoStack.getItemDamage(), 1);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return ID;
	}

}
