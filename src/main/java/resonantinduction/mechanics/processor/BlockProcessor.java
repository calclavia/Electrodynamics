package resonantinduction.mechanics.processor;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.client.render.BlockRenderingHandler;
import resonantinduction.client.render.RenderProcessor;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import resonantinduction.transport.ResonantInductionTransport;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.BlockMachine;
import com.builtbroken.minecraft.recipes.ProcessorType;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockProcessor extends BlockMachine
{

	public BlockProcessor()
	{
		super(Settings.CONFIGURATION, "OreProcessor", UniversalElectricity.machine);
		this.setCreativeTab(ResonantInductionTabs.tabIndustrial());
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (entityPlayer != null)
		{
			if (world.isRemote)
			{
				return true;
			}
			else
			{
				entityPlayer.openGui(ResonantInductionTransport.instance, CommonProxy.GUI_PROCESSOR, world, x, y, z);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (world.getBlockMetadata(x, y, z) % 4 < 3)
		{
			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) + 1, 3);
			return true;
		}
		else
		{
			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 3, 3);
			return true;
		}
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair<String, Class<? extends TileEntity>>("ALOreProcessor", TileEntityProcessor.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
		list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityProcessor.class, new RenderProcessor()));
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityProcessor();
	}

	@Override
	public boolean hasExtraConfigs()
	{
		return true;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		super.loadExtraConfigs(config);
		for (ProcessorData data : ProcessorData.values())
		{
			data.enabled = config.get(data.unlocalizedName, "Enabled", true).getBoolean(true);
			data.allowCrafting = config.get(data.unlocalizedName, "CanCraft", true).getBoolean(true);
			data.wattPerTick = config.get(data.unlocalizedName, "WattPerTick", data.wattPerTick).getInt();
			data.processingTicks = config.get(data.unlocalizedName, "ProcessingTicks", data.processingTicks).getInt();
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			{
				data.doAnimation = config.get(data.unlocalizedName, "DoAnimation", true).getBoolean(true);
			}
		}
	}

	@Override
	public void loadOreNames()
	{
		for (ProcessorData data : ProcessorData.values())
		{
			OreDictionary.registerOre(data.unlocalizedName + "OreProcessor", new ItemStack(this.blockID, 1, data.startMeta));
		}
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.BLOCK_RENDER_ID;
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata / 4;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		int id = idPicked(world, x, y, z);

		if (id == 0)
		{
			return null;
		}

		Item item = Item.itemsList[id];

		if (item == null)
		{
			return null;
		}

		int metadata = getDamageValue(world, x, y, z);

		return new ItemStack(id, 1, metadata);
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List list)
	{
		for (ProcessorData data : ProcessorData.values())
		{
			if (data.enabled)
			{
				list.add(new ItemStack(par1, 1, data.startMeta));
			}
		}
	}

	public static enum ProcessorData
	{
		CRUSHER(ProcessorType.CRUSHER, "crusher", 125, 100, 0),
		GRINDER(ProcessorType.GRINDER, "grinder", 125, 120, 4);
		// PRESS(ProcessorType.PRESS, "press", 200, 50, 8);
		public ProcessorType type;
		public String unlocalizedName;
		public long wattPerTick;
		public int processingTicks, startMeta;
		public boolean enabled = true;
		public boolean allowCrafting = true;
		public boolean doAnimation = true;

		private ProcessorData(ProcessorType type, String name, long watts, int ticks, int meta)
		{
			this.type = type;
			this.unlocalizedName = name;
			this.wattPerTick = watts;
			this.processingTicks = ticks;
			this.startMeta = meta;
		}
	}

}
