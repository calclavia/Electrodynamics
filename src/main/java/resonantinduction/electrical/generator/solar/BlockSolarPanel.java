package resonantinduction.electrical.generator.solar;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.prefab.block.BlockMachine;
import resonantinduction.old.client.render.BlockRenderingHandler;
import resonantinduction.old.client.render.RenderBlockSolarPanel;
import resonantinduction.old.lib.IExtraInfo.IExtraBlockInfo;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSolarPanel extends BlockMachine implements IExtraBlockInfo
{
	public static int tickRate = 10;
	public static long wattDay = 120;
	public static long wattNight = 1;
	public static long wattStorm = 5;

	public BlockSolarPanel()
	{
		super("BlockSolarPanel");
		this.setBlockBounds(0, 0, 0, 1f, .6f, 1f);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntitySolarPanel();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.BLOCK_RENDER_ID;
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair<String, Class<? extends TileEntity>>("DMSolarCell", TileEntitySolarPanel.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
			list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntitySolarPanel.class, new RenderBlockSolarPanel()));
		
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		tickRate = config.get("settings", "PanelUpdateRate", tickRate).getInt();
		wattDay = config.get("settings", "WattDayLight", 120).getInt();
		wattNight = config.get("settings", "WattMoonLight", 1).getInt();
		wattStorm = config.get("settings", "WattStorm", 6).getInt();
	}

	@Override
	public void loadOreNames()
	{
		OreDictionary.registerOre("SolarPanel", new ItemStack(this, 1, 0));
	}

    @Override
    public boolean hasExtraConfigs()
    {
        // TODO Auto-generated method stub
        return false;
    }
}
