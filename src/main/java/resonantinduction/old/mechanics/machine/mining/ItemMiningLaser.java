package resonantinduction.old.mechanics.machine.mining;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.api.events.LaserEvent;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.item.ItemElectric;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.content.IExtraInfo.IExtraItemInfo;
import calclavia.lib.utility.RayTraceHelper;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Stream laser mining tool, When held down it will slowly mine away at the block in front of it.
 * 
 * 
 * TODO create model for this that is 3D. The front should spin around the barrel as its mines
 * generating a laser. As well the player should be wearing a battery pack when the laser is out.
 * Other option is to force the player to wear a battery pack as armor when using the tool
 * 
 * TODO when the laser hits the block there should be a flaring effect that simi blinds the player.
 * That way they are force to wear wielding googles. As well this will gear the player more towards
 * mining and less to fighting. Though the laser should still be a very effect fighting weapon, with
 * only down side being its battery, and that it slows you down when held. Eg its a heavy peace of
 * mining gear and the player will be simi-stationary when using it
 * 
 * @author DarkGuardsman
 */
public class ItemMiningLaser extends ItemElectric implements IExtraItemInfo
{
	long batterySize = 100000;
	float wattPerShot = 1;
	float damageToEntities = 3.3f;
	int blockRange = 50;
	int firingDelay = 5;
	int breakTime = 15;
	boolean createLava = true, setFire = true;

	HashMap<EntityPlayer, Pair<Vector3, Integer>> miningMap = new HashMap<EntityPlayer, Pair<Vector3, Integer>>();

	public ItemMiningLaser()
	{
		super(Mechanical.CONFIGURATION.getItem("LaserMiningGun", DarkCore.getNextItemId()).getInt());
		this.setUnlocalizedName("MiningLaser");
		this.setMaxStackSize(1);
		this.setCreativeTab(CreativeTabs.tabTools);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		// TODO change render of the laser too show it slowly over heat, when it over heats eg gets
		// to max use damage the player, and tool
		return 1000;
	}

	@Override
	public void onUpdate(ItemStack itemStack, World par2World, Entity entity, int par4, boolean par5)
	{
		// Slow any entity that carries this down as a side effect of using heavy mining gear
		if (entity instanceof EntityLivingBase)
		{
			boolean flag = entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;

			if (!flag)
			{
				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 5, 0));
			}
			else
			{
				// ((EntityPlayer) entity).setItemInUse(itemStack,
				// this.getMaxItemUseDuration(itemStack));
			}
		}
	}

	@Override
	public void onCreated(ItemStack stack, World par2World, EntityPlayer entityPlayer)
	{
		this.setEnergy(stack, 0);
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		if (entityPlayer != null)
		{
			stack.getTagCompound().setString("Creator", entityPlayer.username);
		}
	}

	@Override
	public void onUsingItemTick(ItemStack stack, EntityPlayer player, int count)
	{

		if (count > 5)
		{
			Vec3 playerPosition = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
			Vec3 playerLook = RayTraceHelper.getLook(player, 1.0f);
			Vec3 p = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord, playerPosition.yCoord + playerLook.yCoord, playerPosition.zCoord + playerLook.zCoord);

			Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * blockRange, playerPosition.yCoord + playerLook.yCoord * blockRange, playerPosition.zCoord + playerLook.zCoord * blockRange);
			MovingObjectPosition hit = RayTraceHelper.do_rayTraceFromEntity(player, new Vector3().toVec3(), blockRange, true);

			// TODO fix sound
			if (hit != null)
			{
				LaserEvent event = new LaserEvent.LaserFiredPlayerEvent(player, hit, stack);
				MinecraftForge.EVENT_BUS.post(event);
				if (!player.worldObj.isRemote && !event.isCanceled())
				{
					if (hit.typeOfHit == EnumMovingObjectType.ENTITY && hit.entityHit != null)
					{
						// TODO re-implements laser damage source
						DamageSource damageSource = DamageSource.causeMobDamage(player);
						hit.entityHit.attackEntityFrom(damageSource, damageToEntities);
						hit.entityHit.setFire(5);
					}
					else if (hit.typeOfHit == EnumMovingObjectType.TILE)
					{
						int time = 1;
						boolean mined = false;
						if (miningMap.containsKey(player))
						{
							Pair<Vector3, Integer> lastHit = miningMap.get(player);
							if (lastHit != null && lastHit.left() != null && lastHit.left().equals(new Vector3(hit)))
							{
								time = lastHit.right() + 1;
								if (time >= breakTime)
								{

									LaserEvent.onBlockMinedByLaser(player.worldObj, player, new Vector3(hit));
									mined = true;
									miningMap.remove(player);
								}
								else
								{
									// TODO get the actual hit side from the angle of the ray trace
									LaserEvent.onLaserHitBlock(player.worldObj, player, new Vector3(hit), ForgeDirection.UP);
								}
							}
						}
						if (!mined)
						{
							miningMap.put(player, new Pair<Vector3, Integer>(new Vector3(hit), time));
						}
					}

				}
				playerViewOffset = hit.hitVec;
			}
			// TODO make beam brighter the longer it has been used
			// TODO adjust the laser for the end of the gun
			float x = (float) (MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.4) - MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
			float z = (float) (MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.4) + MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
			ContentRegistry.proxy().renderBeam(player.worldObj, new Vector3(p).translate(new Vector3(x, -.25, z)), new Vector3(playerViewOffset), Color.ORANGE, 1);
			ContentRegistry.proxy().renderBeam(player.worldObj, new Vector3(p).translate(new Vector3(x, -.45, z)), new Vector3(playerViewOffset), Color.ORANGE, 1);
		}

	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World par2World, EntityPlayer player)
	{
		if (player.capabilities.isCreativeMode || this.getEnergy(itemStack) > this.wattPerShot)
		{
			player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
		}
		return itemStack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer player, int par4)
	{
		if (miningMap.containsKey(player))
		{
			miningMap.remove(player);
		}
	}

	@Override
	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		return par1ItemStack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		super.addInformation(stack, player, list, par4);
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		String creator = stack.getTagCompound().getString("Creator");
		if (!creator.equalsIgnoreCase("creative") && creator != "")
		{
			list.add("Created by: " + creator);
		}
		else if (creator.equalsIgnoreCase("creative"))
		{
			list.add("Created by Magic Dwarfs");
		}

	}

	@Override
	public long getEnergyCapacity(ItemStack theItem)
	{
		return this.batterySize;
	}

	@Override
	public boolean hasExtraConfigs()
	{
		return true;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		this.blockRange = config.get("Laser", "Range", this.blockRange).getInt(this.blockRange);
		this.firingDelay = config.get("Laser", "Delay", this.firingDelay).getInt(this.firingDelay);
		this.damageToEntities = (float) config.get("Laser", "Damage", this.damageToEntities).getDouble(this.damageToEntities);
		this.batterySize = (config.get("Energy", "BatteryCap", this.batterySize).getInt());
		this.wattPerShot = (float) (config.get("Energy", "FiringCost", this.wattPerShot * 1000).getDouble(this.wattPerShot * 1000) / 1000);
		this.setFire = config.get("Laser", "SetFire", true, "Set plants, and wood on fire").getBoolean(true);
		this.createLava = config.get("Laser", "MakeLava", true, "Turn some blocks into lava like obby").getBoolean(true);

	}

	@Override
	public void loadOreNames()
	{
		OreDictionary.registerOre("MiningLaserGun", this);

	}

}
