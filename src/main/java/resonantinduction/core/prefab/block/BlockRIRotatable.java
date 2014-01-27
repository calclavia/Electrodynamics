package resonantinduction.core.prefab.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockRotatable;
import codechicken.multipart.ControlKeyModifer;

/**
 * @author Calclavia
 * 
 */
public class BlockRIRotatable extends BlockRotatable
{
	public BlockRIRotatable(String name)
	{
		this(name, Settings.getNextBlockID());
	}

	public BlockRIRotatable(String name, int id)
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), UniversalElectricity.machine);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
		this.setHardness(1f);
	}

	@Override
	public boolean isControlDown(EntityPlayer player)
	{
		return ControlKeyModifer.isControlDown(player);
	}
}
