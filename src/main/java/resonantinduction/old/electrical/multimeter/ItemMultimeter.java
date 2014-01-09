package resonantinduction.old.electrical.multimeter;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.ItemMultipartBase;
import resonantinduction.old.energy.multimeter.PartMultimeter;
import resonantinduction.old.energy.wire.EnumWireMaterial;
import calclavia.lib.utility.LanguageUtility;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class ItemMultimeter extends ItemMultipartBase
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemMultimeter(int id)
	{
		super("multimeter", id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		if (world.getBlockTileEntity(pos.x, pos.y, pos.z) instanceof TileMultipart)
		{
			pos.offset(side ^ 1, -1);
		}

		PartMultimeter part = (PartMultimeter) MultiPartRegistry.createPart("resonant_induction_multimeter", false);

		if (part != null)
		{
			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		par3List.add(LanguageUtility.getLocal("tooltip.multimeter.line1"));
		par3List.add(LanguageUtility.getLocal("tooltip.multimeter.line2"));

		float detection = this.getDetection(itemStack);

		if (detection != -1)
		{
			par3List.add(LanguageUtility.getLocal("tooltip.multimeter.lastSave").replace("%v", detection + ""));
		}
		else
		{
			par3List.add(LanguageUtility.getLocal("tooltip.multimeter.noSave"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!par2EntityPlayer.isSneaking())
		{
			if (!world.isRemote)
			{
				par2EntityPlayer.addChatMessage(LanguageUtility.getLocal("message.multimeter.onUse").replace("%v", "" + PartMultimeter.getDetectedEnergy(ForgeDirection.getOrientation(par7), world.getBlockTileEntity(x, y, z))));
			}

			return true;
		}

		return super.onItemUse(par1ItemStack, par2EntityPlayer, world, x, y, z, par7, par8, par9, par10);
	}

	public float getDetection(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("detection"))
		{
			return -1;
		}

		return itemStack.stackTagCompound.getFloat("detection");
	}

	public void setDetection(ItemStack itemStack, float detection)
	{
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("detection", detection);
	}
}
