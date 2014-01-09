package resonantinduction.old.core.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.old.lib.EnumMaterial;
import resonantinduction.old.lib.IExtraInfo.IExtraItemInfo;

import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Flexible tool class that uses NBT to store damage and effect rather than metadata. Metadata
 * instead is used to store sub items allowing several different tools to exist within the same item
 * 
 * @author DarkGuardsman
 */
public class ItemCommonTool extends Item implements IExtraItemInfo
{
	protected int enchant = 5;
	public static final String TOOL_DAMAGE = "toolDamage";
	public static final String BROKEN_NBT = "broken";
	public static final String REINFORCED_NBT = "reinforced";
	public static final String HEATTREATED_NBT = "heattreated";
	public static final String HANDLE_NBT = "handle";

	public ItemCommonTool()
	{
		super(Settings.CONFIGURATION.getItem("Items", "CommonTools", Settings.getNextItemID()).getInt());
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.tabTools);
	}

	@Override
	public void onCreated(ItemStack itemStack, World world, EntityPlayer entityPlayer)
	{
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		itemStack.getTagCompound().setString("Creator", (entityPlayer != null ? entityPlayer.username : "Magical Dwarfs"));
		itemStack.getTagCompound().setInteger("ToolID", itemStack.getItemDamage());
	}

	@Override
	public Multimap getItemAttributeModifiers()
	{
		Multimap multimap = super.getItemAttributeModifiers();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Tool modifier", 6, 0));
		return multimap;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			if (itemStack.getTagCompound().hasKey("Creator"))
			{
				list.add("Created by " + itemStack.getTagCompound().getString("Creator"));
			}
			if (itemStack.getTagCompound().getBoolean(BROKEN_NBT))
			{
				list.add("Blunted");
			}
			else
			{
				EnumMaterial mat = EnumTool.getToolMatFromMeta(itemStack.getItemDamage());
				int currentDamage = itemStack.getTagCompound().getInteger(TOOL_DAMAGE);
				list.add("D: " + currentDamage + "/" + mat.maxUses);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		if (itemStack.getTagCompound().getBoolean(BROKEN_NBT))
		{
			return Color.RED.getRGB();
		}
		return super.getColorFromItemStack(itemStack, par2);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		if (itemStack.getTagCompound().getBoolean(BROKEN_NBT))
		{
			return false;
		}
		if (EnumTool.getToolFromMeta(itemStack.getItemDamage()) == EnumTool.HOE)
		{
			if (!player.canPlayerEdit(x, y, z, par7, itemStack))
			{
				return false;
			}
			else
			{
				UseHoeEvent event = new UseHoeEvent(player, itemStack, world, x, y, z);
				if (MinecraftForge.EVENT_BUS.post(event))
				{
					return false;
				}

				if (event.getResult() == Result.ALLOW)
				{
					this.damage(itemStack, 1, player);
					return true;
				}

				int blockID = world.getBlockId(x, y, z);
				boolean air = world.isAirBlock(x, y + 1, z);

				if (par7 != 0 && air && (blockID == Block.grass.blockID || blockID == Block.dirt.blockID))
				{
					Block block = Block.tilledField;
					world.playSoundEffect((x + 0.5F), (y + 0.5F), (z + 0.5F), block.stepSound.getStepSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);

					if (world.isRemote)
					{
						return true;
					}
					else
					{
						world.setBlock(x, y, z, block.blockID);
						this.damage(itemStack, 1, player);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		if (EnumTool.getToolFromMeta(par1ItemStack.getItemDamage()) == EnumTool.SWORD)
		{
			return EnumAction.block;
		}
		return super.getItemUseAction(par1ItemStack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		if (EnumTool.getToolFromMeta(par1ItemStack.getItemDamage()) == EnumTool.SWORD)
		{
			return 72000;
		}
		return super.getMaxItemUseDuration(par1ItemStack);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		if (EnumTool.getToolFromMeta(par1ItemStack.getItemDamage()) == EnumTool.SWORD)
		{
			par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
		}
		return par1ItemStack;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity)
	{
		if (entity.worldObj.isRemote)
		{
			return false;
		}
		if (entity instanceof IShearable && EnumTool.getToolFromMeta(itemstack.getItemDamage()) == EnumTool.SHEAR)
		{
			IShearable target = (IShearable) entity;
			if (target.isShearable(itemstack, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ))
			{
				ArrayList<ItemStack> drops = target.onSheared(itemstack, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, itemstack));

				Random rand = new Random();
				for (ItemStack stack : drops)
				{
					EntityItem ent = entity.entityDropItem(stack, 1.0F);
					ent.motionY += rand.nextFloat() * 0.05F;
					ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
					ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
				}
				this.damage(itemstack, 1, entity);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
	{
		if (player.worldObj.isRemote)
		{
			return false;
		}
		int id = player.worldObj.getBlockId(x, y, z);
		if (Block.blocksList[id] instanceof IShearable && EnumTool.getToolFromMeta(itemstack.getItemDamage()) == EnumTool.SHEAR)
		{
			IShearable target = (IShearable) Block.blocksList[id];
			if (target.isShearable(itemstack, player.worldObj, x, y, z))
			{
				ArrayList<ItemStack> drops = target.onSheared(itemstack, player.worldObj, x, y, z, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, itemstack));
				Random rand = new Random();

				for (ItemStack stack : drops)
				{
					float f = 0.7F;
					double d = (rand.nextFloat() * f) + (1.0F - f) * 0.5D;
					double d1 = (rand.nextFloat() * f) + (1.0F - f) * 0.5D;
					double d2 = (rand.nextFloat() * f) + (1.0F - f) * 0.5D;
					EntityItem entityitem = new EntityItem(player.worldObj, x + d, y + d1, z + d2, stack);
					entityitem.delayBeforeCanPickup = 10;
					player.worldObj.spawnEntityInWorld(entityitem);
				}

				itemstack.damageItem(1, player);
				player.addStat(StatList.mineBlockStatArray[id], 1);
			}
		}
		return false;
	}

	/** Applies damage to the item using NBT as well uses the normal ItemStack damage checks */
	public void damage(ItemStack itemStack, int damage, EntityLivingBase entity)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		EnumMaterial mat = EnumTool.getToolMatFromMeta(itemStack.getItemDamage());
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)
		{
			return;
		}
		if (damage > 0)
		{
			int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack);
			int k = 0;

			for (int l = 0; j > 0 && l < damage; ++l)
			{
				if (EnchantmentDurability.negateDamage(itemStack, j, entity.worldObj.rand))
				{
					++k;
				}
			}

			damage -= k;

			if (damage <= 0)
			{
				return;
			}
		}
		int currentDamage = itemStack.getTagCompound().getInteger(TOOL_DAMAGE) + damage;
		damage = Math.max(Math.min(damage, mat.maxUses), 0);
		itemStack.getTagCompound().setInteger(TOOL_DAMAGE, currentDamage);
		if (entity instanceof EntityPlayer)
		{
			((EntityPlayer) entity).inventory.onInventoryChanged();
		}
		if (currentDamage > mat.maxUses)
		{
			entity.renderBrokenItemStack(itemStack);
			itemStack.getTagCompound().setBoolean(BROKEN_NBT, true);
			if (entity instanceof EntityPlayer)
			{
				EntityPlayer entityplayer = (EntityPlayer) entity;
				entityplayer.addStat(StatList.objectBreakStats[this.itemID], 1);
			}
		}

	}

	@Override
	public boolean isDamaged(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		if (itemStack.getTagCompound().getBoolean(BROKEN_NBT))
		{
			return true;
		}
		return itemStack.getTagCompound().getInteger(TOOL_DAMAGE) > 0;
	}

	@Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase)
	{
		this.damage(itemStack, EnumTool.getToolFromMeta(itemStack.getItemDamage()) == EnumTool.SWORD ? 1 : 2, par2EntityLivingBase);
		return true;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemStack, World par2World, int par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase)
	{
		if (EnumTool.getToolFromMeta(itemStack.getItemDamage()) == EnumTool.SHEAR && par3 != Block.leaves.blockID && par3 != Block.web.blockID && par3 != Block.tallGrass.blockID && par3 != Block.vine.blockID && par3 != Block.tripWire.blockID && !(Block.blocksList[par3] instanceof IShearable))
		{
			return false;
		}
		if (Block.blocksList[par3].getBlockHardness(par2World, par4, par5, par6) != 0.0D)
		{
			this.damage(itemStack, EnumTool.getToolFromMeta(itemStack.getItemDamage()) == EnumTool.SWORD ? 2 : 1, par7EntityLivingBase);
		}

		return true;
	}

	@Override
	public int getItemEnchantability()
	{
		return this.enchant;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		// TODO,, We will have to check on how this is done to prevent issues with the way damage is
		// actually saved
		return false;
	}

	@Override
	public float getStrVsBlock(ItemStack itemStack, Block block)
	{
		if (itemStack != null && block != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			if (itemStack.getTagCompound().getBoolean(BROKEN_NBT))
			{
				return 0;
			}
			EnumTool tool = EnumTool.getToolFromMeta(itemStack.getItemDamage());
			EnumMaterial mat = EnumTool.getToolMatFromMeta(itemStack.getItemDamage());
			if (tool.effecticVsMaterials.contains(block.blockMaterial))
			{
				return mat.materialEffectiveness + (tool == EnumTool.SHEAR && block.blockMaterial == Material.leaves ? 9f : 0f);
			}
			return 1.0F;
		}
		return 0;

	}

	@Override
	public boolean canHarvestBlock(Block par1Block)
	{
		return true;
	}

	@Override
	public boolean canHarvestBlock(Block block, ItemStack itemStack)
	{
		if (itemStack != null && block != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			if (itemStack.getTagCompound().getBoolean(BROKEN_NBT))
			{
				return false;
			}
			EnumTool tool = EnumTool.getToolFromMeta(itemStack.getItemDamage());
			EnumMaterial mat = EnumTool.getToolMatFromMeta(itemStack.getItemDamage());
			if (tool.effecticVsMaterials.contains(block.blockMaterial))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			return "item." + Reference.PREFIX + EnumTool.getFullName(itemStack.getItemDamage());
		}
		else
		{
			return this.getUnlocalizedName();
		}
	}

	@Override
	public int getDisplayDamage(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		int damage = itemStack.getTagCompound().getInteger(TOOL_DAMAGE);
		EnumMaterial mat = EnumTool.getToolMatFromMeta(itemStack.getItemDamage());
		return (damage / mat.maxUses) * 100;
	}

	@Override
	public Icon getIconFromDamage(int i)
	{
		return EnumTool.getToolIcon(i);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		for (EnumTool tool : EnumTool.values())
		{
			for (EnumMaterial mat : EnumMaterial.values())
			{
				if (mat.hasTools)
				{
					tool.toolIcons = new Icon[EnumMaterial.values().length];

					if (tool.enabled)
					{
						tool.toolIcons[mat.ordinal()] = iconRegister.registerIcon(Reference.PREFIX + "tool." + mat.simpleName + tool.name);
					}
				}
			}
		}
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (EnumMaterial mat : EnumMaterial.values())
		{
			if (mat.hasTools)
			{
				for (EnumTool tool : EnumTool.values())
				{
					ItemStack stack = tool.getTool(mat);
					if (tool.enabled && stack != null)
					{
						this.onCreated(stack, null, null);
						par3List.add(stack);
					}
				}
			}
		}
	}

	@Override
	public boolean hasExtraConfigs()
	{
		// TODO Add configs such as enable broken tools, and allow tools to be salvaged
		return false;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void loadOreNames()
	{
		for (EnumMaterial mat : EnumMaterial.values())
		{
			if (mat.hasTools)
			{
				for (EnumTool tool : EnumTool.values())
				{
					ItemStack stack = tool.getTool(mat);
					if (tool.enabled && stack != null)
					{
						OreDictionary.registerOre(EnumTool.getFullName(stack.getItemDamage()), stack);
					}
				}
			}
		}

	}
}
