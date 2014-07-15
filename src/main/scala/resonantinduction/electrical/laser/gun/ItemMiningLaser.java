package resonantinduction.electrical.laser.gun;

import java.awt.Color;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import resonant.api.event.LaserEvent;
import resonant.api.event.LaserFiredPlayerEvent;
import resonant.lib.prefab.vector.RayTraceHelper;
import resonant.lib.type.Pair;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.items.ItemEnergyTool;
import universalelectricity.api.vector.IVector3;
import universalelectricity.core.transform.vector.Vector3;

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
public class ItemMiningLaser extends ItemEnergyTool
{
    /** Cost per tick of using the item */
    long joulesPerTick = 100;
    /** Damage to entities hit by the laser */
    float damageToEntities = 3.3f;
    /** Range of the laser ray trace */
    int blockRange = 50;
    /** Time to break a single block */
    int breakTime = 15;

    /** Map of players and how long they have focused the laser on a single block */
    HashMap<EntityPlayer, Pair<Vector3, Integer>> miningMap = new HashMap<EntityPlayer, Pair<Vector3, Integer>>();
    /** Used to track energy used while the player uses the laser rather then direct editing the nbt */
    HashMap<EntityPlayer, Long> energyUsedMap = new HashMap<EntityPlayer, Long>();

    public static final int MODE_REMOVE = 0, MODE_SMELT = 1, MODE_DAMAGE = 2;

    public ItemMiningLaser(int id)
    {
        super(id);
        hasModes = true;
        this.energyTiers = 1;
        toolModes = new String[] { "laser.toolmode.remove", "laser.toolmode.smelt", "laser.toolmode.damage" };
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int slot, boolean currentHeldItem)
    {
        //Remove player from mining map if he puts the laser gun away
        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            if (!currentHeldItem)
            {
                if (this.miningMap.containsKey(player))
                {
                    this.miningMap.remove(player);
                }
                if (this.energyUsedMap.containsKey(player))
                {
                    this.energyUsedMap.remove(player);
                }
            }
        }
    }

    @Override
    public void onUsingItemTick(ItemStack stack, EntityPlayer player, int count)
    {
        //Small delay to prevent unwanted usage of the item
        //TODO increase this delay to simulate warm up time
        //TODO increase break time longer the laser has been running
        //TODO match hardness of block for break time
        //TODO add audio 
        if (count > 5 && (player.capabilities.isCreativeMode || discharge(stack, joulesPerTick, false) >= joulesPerTick && (!this.energyUsedMap.containsKey(player) || this.energyUsedMap.get(player) <= this.getEnergy(stack))))
        {
            Vec3 playerPosition = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3 playerLook = RayTraceHelper.getLook(player, 1.0f);
            Vec3 p = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord, playerPosition.yCoord + playerLook.yCoord, playerPosition.zCoord + playerLook.zCoord);

            Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * blockRange, playerPosition.yCoord + playerLook.yCoord * blockRange, playerPosition.zCoord + playerLook.zCoord * blockRange);
            MovingObjectPosition hit = RayTraceHelper.do_rayTraceFromEntity(player, new Vector3().toVec3(), blockRange, true);

            if (!player.capabilities.isCreativeMode)
            {
                long energyUsed = this.energyUsedMap.containsKey(player) ? this.energyUsedMap.get(player) : 0;

                switch (getMode(stack))
                {
                    case 0:
                        energyUsed += joulesPerTick;
                    case 1:
                        energyUsed += joulesPerTick / 2;
                    case 2:
                        energyUsed += joulesPerTick / 3;
                }
                this.energyUsedMap.put(player, energyUsed);
            }

            if (hit != null)
            {
                LaserEvent event = new LaserFiredPlayerEvent(player, hit, stack);
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
                                    if (time >= breakTime && this.getMode(stack) == MODE_REMOVE)
                                    {
                                        LaserEvent.onBlockMinedByLaser(player.worldObj, player, new Vector3(hit));
                                        mined = true;
                                        miningMap.remove(player);
                                    }
                                    else if (this.getMode(stack) == MODE_SMELT)
                                    {
                                        //TODO get the actual hit side from the angle of the ray trace
                                        LaserEvent.onLaserHitBlock(player.worldObj, player, new Vector3(hit), ForgeDirection.UP);
                                        player.worldObj.destroyBlockInWorldPartially(player.entityId, hit.blockX, hit.blockY, hit.blockZ, time);
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

            //Only call client as the server can render stuff threw packets
            if (player.worldObj.isRemote)
            {
                float x = (float) (MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.4) - MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
                float z = (float) (MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.4) + MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
                ResonantInduction.proxy.renderBeam(player.worldObj, (IVector3) new Vector3(p).add(new Vector3(x, -.25, z)), (IVector3) new Vector3(playerViewOffset), Color.red, 5);
                ResonantInduction.proxy.renderBeam(player.worldObj, (IVector3) new Vector3(p).add(new Vector3(x, -.45, z)), (IVector3) new Vector3(playerViewOffset), Color.red, 5);
            }

        }

    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        if (!player.isSneaking())
        {
            player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
        }
        return super.onItemRightClick(itemStack, world, player);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int par4)
    {
        if (miningMap.containsKey(player))
        {
            Pair<Vector3, Integer> vec = miningMap.get(player);
            if (vec != null && vec.left() != null)
            {
                player.worldObj.destroyBlockInWorldPartially(player.entityId, vec.left().xi(), vec.left().yi(), vec.left().zi(), -1);
            }
            miningMap.remove(player);
        }
        if (this.energyUsedMap.containsKey(player))
        {
            discharge(stack, this.energyUsedMap.get(player), true);
            this.energyUsedMap.remove(player);
        }
    }

    @Override
    public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

}
