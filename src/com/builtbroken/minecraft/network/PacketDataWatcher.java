package com.builtbroken.minecraft.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;

public class PacketDataWatcher
{
    HashMap<Pair<World, Vector3>, List<Integer>> packetSizes = new HashMap<Pair<World, Vector3>, List<Integer>>();

    public static PacketDataWatcher instance = new PacketDataWatcher();

    public boolean enable = false;

    public void onPacketData(TileEntity entity, Packet250CustomPayload data, long t)
    {
        if (entity != null && enable)
        {
            Pair<World, Vector3> location = new Pair<World, Vector3>(entity.worldObj, new Vector3(entity));
            List<Integer> l = this.packetSizes.get(location);
            if (l == null)
            {
                l = new ArrayList<Integer>();
            }
            l.add(data.getPacketSize());
            this.packetSizes.put(location, l);
        }
    }

    @ForgeSubscribe
    public void playerRightClickEvent(PlayerInteractEvent event)
    {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.capabilities.isCreativeMode && event.entityPlayer.getHeldItem() != null && event.entityPlayer.getHeldItem().itemID == Item.blazeRod.itemID)
        {
            if (event.entityPlayer.worldObj.isRemote)
            {
                if (event.entityPlayer.isSneaking())
                {
                    this.enable = !this.enable;
                    event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("PacketWatcher is now " + (this.enable ? "Enabled. Now caching packet sizes." : "Disabled. Data cache has been cleared")));
                    this.packetSizes.clear();
                }
                else
                {
                    TileEntity ent = event.entityPlayer.worldObj.getBlockTileEntity(event.x, event.y, event.z);
                    if (ent != null)
                    {
                        System.out.println("Entity Check");
                        Pair<World, Vector3> location = new Pair(ent.worldObj, new Vector3(ent));
                        int p = 0, a = 0;
                        if (this.packetSizes.get(location) != null)
                        {
                            for (int i : this.packetSizes.get(location))
                            {
                                a += i;
                            }
                            p = this.packetSizes.get(location).size();
                            a /= (p > 0 ? p : 1);
                        }
                        event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("AveragePacketSize: " + a + "bits  for " + p + " packets"));

                    }
                }
            }
        }
    }
}
