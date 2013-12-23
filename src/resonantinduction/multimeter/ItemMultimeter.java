package resonantinduction.multimeter;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import resonantinduction.Utility;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.part.PartFlatWire;
import resonantinduction.wire.render.RenderFlatWire;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMultimeter extends JItemMultiPart
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemMultimeter(int id)
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
		BlockCoord onPos = pos.copy().offset(side ^ 1);
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
		par3List.add("Shift-right click to place,");
		par3List.add("Right click to scan data.");

		float detection = this.getDetection(itemStack);

		if (detection != -1)
		{
			par3List.add("Last Detection: " + detection + " KJ");
		}
		else
		{
			par3List.add("No detection saved.");
		}
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!par2EntityPlayer.isSneaking())
		{
			if (!world.isRemote)
			{
				par2EntityPlayer.addChatMessage("Energy: " + PartMultimeter.getDetectedEnergy(ForgeDirection.getOrientation(par7), world.getBlockTileEntity(x, y, z)) + " J");
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
