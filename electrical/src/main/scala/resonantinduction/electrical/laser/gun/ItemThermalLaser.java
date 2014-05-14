package resonantinduction.electrical.laser.gun;

import java.awt.Color;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.api.event.LaserEvent;
import resonant.lib.prefab.vector.RayTraceHelper;
import resonant.lib.thermal.ThermalGrid;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.item.ItemElectric;
import universalelectricity.api.vector.IVector3;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

/** Version of the mining laser that uses the thermal grid to melt blocks down
 * 
 * @author DarkGuardsman */
public class ItemThermalLaser extends ItemElectric
{
    long batterySize = 100000;
    float wattPerShot = 1;
    float damageToEntities = 3.3f;
    int blockRange = 50;
    int firingDelay = 5;
    int breakTime = 15;
    boolean createLava = true, setFire = true;

    public ItemThermalLaser(int id)
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
                        ThermalGrid.addTemperature(new VectorWorld(player.worldObj, hit.blockX, hit.blockY, hit.blockZ), 100f);
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
    public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }

    @Override
    public long getEnergyCapacity(ItemStack theItem)
    {
        return this.batterySize;
    }

}
