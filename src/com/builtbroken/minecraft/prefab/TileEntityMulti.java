package com.builtbroken.minecraft.prefab;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.interfaces.IMultiBlock;
import com.builtbroken.minecraft.network.ISimplePacketReceiver;
import com.builtbroken.minecraft.network.PacketHandler;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

/** This is a multiblock to be used for blocks that are bigger than one block.
 * 
 * @author Calclavia */
public class TileEntityMulti extends TileEntity implements ISimplePacketReceiver
{
    // The the position of the main block
    public Vector3 mainBlockPosition;

    public TileEntityMulti()
    {

    }

    public void setMainBlock(Vector3 mainBlock)
    {
        this.mainBlockPosition = mainBlock;

        if (!this.worldObj.isRemote)
        {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput data, Player player)
    {
        try
        {
            if (id.equalsIgnoreCase("MainBlock"))
            {
                this.mainBlockPosition = new Vector3(data.readInt(), data.readInt(), data.readInt());
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        if (this.mainBlockPosition != null)
        {
            return PacketHandler.instance().getTilePacket(DarkCore.CHANNEL, "MainBlock", this, this.mainBlockPosition.intX(), this.mainBlockPosition.intY(), this.mainBlockPosition.intZ());
        }

        return null;
    }

    public void onBlockRemoval()
    {
        if (this.mainBlockPosition != null)
        {
            TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.mainBlockPosition.intX(), this.mainBlockPosition.intY(), this.mainBlockPosition.intZ());

            if (tileEntity != null && tileEntity instanceof IMultiBlock)
            {
                IMultiBlock mainBlock = (IMultiBlock) tileEntity;

                if (mainBlock != null)
                {
                    mainBlock.onDestroy(this);
                }
            }
        }
    }

    public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer)
    {
        if (this.mainBlockPosition != null)
        {
            TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.mainBlockPosition.intX(), this.mainBlockPosition.intY(), this.mainBlockPosition.intZ());

            if (tileEntity != null)
            {
                if (tileEntity instanceof IMultiBlock)
                {
                    return ((IMultiBlock) tileEntity).onActivated(par5EntityPlayer);
                }
            }
        }

        return false;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.mainBlockPosition = new Vector3(nbt.getCompoundTag("mainBlockPosition"));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (this.mainBlockPosition != null)
        {
            nbt.setCompoundTag("mainBlockPosition", this.mainBlockPosition.writeToNBT(new NBTTagCompound()));
        }
    }

    /** Determines if this TileEntity requires update calls.
     * 
     * @return True if you want updateEntity() to be called, false if not */
    @Override
    public boolean canUpdate()
    {
        return false;
    }

}