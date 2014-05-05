package resonantinduction.electrical.laser.gun;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import resonantinduction.core.ResonantInduction;
import net.minecraft.block.Block;
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
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.item.ItemElectric;
import universalelectricity.api.vector.IVector3;
import universalelectricity.api.vector.Vector3;
import calclavia.api.resonantinduction.electrical.LaserEvent;
import calclavia.lib.type.Pair;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Stream laser mining tool, When held down it will slowly mine away at the block in front of it.
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
 * @author DarkGuardsman */
@UniversalClass
public class ItemMiningLaser extends ItemElectric
{
    long batterySize = 500000;
    float wattPerShot = 1;
    float damageToEntities = 3.3f;
    int blockRange = 50;
    int firingDelay = 5;
    int breakTime = 15;
    boolean createLava = true, setFire = true;

    HashMap<EntityPlayer, Pair<Vector3, Integer>> miningMap = new HashMap<EntityPlayer, Pair<Vector3, Integer>>();

    public ItemMiningLaser(int id)
    {
        super(id);
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
        //TODO change render of the laser too show it slowly over heat, when it over heats eg gets to max use damage the player, and tool
        return 1000;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World par2World, Entity entity, int par4, boolean par5)
    {
        //Slow any entity that carries this down as a side effect of using heavy mining gear
        if (entity instanceof EntityLivingBase)
        {
            boolean flag = entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;

            if (!flag)
            {
                //((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 5, 0));
            }
            else
            {
                //((EntityPlayer) entity).setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
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

            //TODO fix sound
            if (hit != null)
            {
                LaserEvent event = new LaserEvent.LaserFiredPlayerEvent(player, hit, stack);
                MinecraftForge.EVENT_BUS.post(event);
                if (!player.worldObj.isRemote && !event.isCanceled())
                {
                    if (hit.typeOfHit == EnumMovingObjectType.ENTITY && hit.entityHit != null)
                    {
                        //TODO re-implements laser damage source
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
                                Block b = Block.blocksList[player.worldObj.getBlockId(hit.blockX, hit.blockY, hit.blockZ)];
                                if (b != null && b.getBlockHardness(player.worldObj, hit.blockX, hit.blockY, hit.blockZ) > -1)
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
                                        //TODO get the actual hit side from the angle of the ray trace
                                        LaserEvent.onLaserHitBlock(player.worldObj, player, new Vector3(hit), ForgeDirection.UP);
                                        player.worldObj.destroyBlockInWorldPartially(0, hit.blockX, hit.blockY, hit.blockZ, time);
                                    }
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
            //TODO make beam brighter the longer it has been used
            //TODO adjust the laser for the end of the gun            
            float x = (float) (MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.4) - MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
            float z = (float) (MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.4) + MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
            ResonantInduction.proxy.renderBeam(player.worldObj, (IVector3) new Vector3(p).translate(new Vector3(x, -.25, z)), (IVector3) new Vector3(playerViewOffset), Color.ORANGE, 1);
            ResonantInduction.proxy.renderBeam(player.worldObj, (IVector3) new Vector3(p).translate(new Vector3(x, -.45, z)), (IVector3) new Vector3(playerViewOffset), Color.ORANGE, 1);
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

}
