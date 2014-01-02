package com.builtbroken.minecraft.save;

import java.io.DataOutputStream;

import com.google.common.io.ByteArrayDataInput;

/** Used for object that only have one set of data to send and receive
 * 
 * @author DarkGuardsman */
public interface IPacketLoad
{
    public void readPacket(ByteArrayDataInput data);
    
    public void loadPacket(DataOutputStream data);    
}
