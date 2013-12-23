package resonantinduction.transformer;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import resonantinduction.wire.EnumWireMaterial;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemTransformer extends JItemMultiPart
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemTransformer(int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, "multimeter", id).getInt(id));
		this.setUnlocalizedName(ResonantInduction.PREFIX + "multimeter");
		this.setCreativeTab(TabRI.INSTANCE);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		PartTransformer part = (PartTransformer) MultiPartRegistry.createPart("resonant_induction_transformer", false);

		if (part != null)
		{
			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}
}
