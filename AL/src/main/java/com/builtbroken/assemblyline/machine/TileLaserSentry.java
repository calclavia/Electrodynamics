package com.builtbroken.assemblyline.machine;

import java.awt.Color;

import net.minecraft.network.packet.Packet;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.PacketHandler;

import com.builtbroken.minecraft.CoreRegistry;
import com.builtbroken.minecraft.LaserEvent;
import com.builtbroken.minecraft.helpers.RayTraceHelper;
import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

/** @author DarkGuardsman */
public class TileLaserSentry extends TileEntityEnergyMachine
{
    private Vector3 target;
    private Vector3 hit;
    private int hitTicks = 0;
    private float yaw = 0;
    private float pitch = 0;
    private float range = 20;
    private float powerDrain = .1f;

    @Override
    public boolean canFunction()
    {
        return super.canFunction();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (this.ticks % 3 == 0 && this.isFunctioning())
        {
            this.fireLaser();
        }
    }

    public void rotateYaw(float by)
    {
        this.yaw += by;
        if (!this.worldObj.isRemote)
        {
            PacketHandler.instance().sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 64);
        }
    }

    public float getYaw()
    {
        return this.yaw;
    }

    public void rotatePitch(float by)
    {
        this.pitch += by;
        if (!this.worldObj.isRemote)
        {
            PacketHandler.instance().sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 64);
        }
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
    {
        try
        {
            if (!super.simplePacket(id, dis, player) && this.worldObj.isRemote)
            {
                if (id.equalsIgnoreCase("Desc"))
                {
                    this.functioning = dis.readBoolean();
                    this.yaw = dis.readFloat();
                    this.pitch = dis.readFloat();
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return PacketHandler.instance().getTilePacket(this.getChannel(), "Desc", this, this.functioning, this.yaw, this.pitch);
    }

    public void fireLaser()
    {

        final Vector3 start = RayTraceHelper.getPosFromRotation(new Vector3(this.xCoord + 0.5, this.yCoord + 0.7, this.zCoord + 0.5), .7f, yaw, pitch);
        Vector3 hitSpot = RayTraceHelper.getPosFromRotation(new Vector3(this.xCoord + 0.5, this.yCoord + 0.7, this.zCoord + 0.5), range, yaw, pitch);
        MovingObjectPosition hitPos = RayTraceHelper.ray_trace_do(this.worldObj, start.toVec3(), hitSpot.toVec3(), range, false);

        if (hitPos != null)
        {
            LaserEvent event = new LaserEvent.LaserFireEvent(this, hitPos);
            MinecraftForge.EVENT_BUS.post(event);

            if (!worldObj.isRemote && !event.isCanceled())
            {
                if (hitPos.typeOfHit == EnumMovingObjectType.ENTITY && hitPos.entityHit != null)
                {
                    if (this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
                    {
                        DamageSource damageSource = DamageSource.onFire;
                        hitPos.entityHit.attackEntityFrom(damageSource, 7);
                        hitPos.entityHit.setFire(8);
                    }
                }
                else if (hitPos.typeOfHit == EnumMovingObjectType.TILE)
                {
                    if (this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
                    {
                        if (this.hit != null && this.hit.equals(new Vector3(hitPos)) && !this.hit.equals(new Vector3(this)))
                        {
                            this.hitTicks++;

                            if (hitTicks >= 6)
                            {
                                LaserEvent.onBlockMinedByLaser(this.worldObj, this, this.hit);
                                this.hit = null;
                                this.hitTicks = 0;
                            }
                        }
                        else
                        {
                            this.hitTicks = 1;
                            this.hit = new Vector3(hitPos);
                            LaserEvent.onLaserHitBlock(this.worldObj, this, this.hit, ForgeDirection.UP);
                        }
                    }

                }

            }
            hitSpot = new Vector3(hitPos.hitVec);

        }
        CoreRegistry.proxy().renderBeam(this.worldObj, start, hitSpot, this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) ? Color.ORANGE : Color.blue, 3);
    }

    public Vector3 getTarget()
    {
        return this.target;
    }

    public void setTarget(Vector3 vec)
    {
        if (!this.worldObj.isRemote)
        {
            this.sendPowerUpdate();
        }
    }
}
