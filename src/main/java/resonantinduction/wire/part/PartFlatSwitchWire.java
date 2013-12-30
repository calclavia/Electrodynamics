package resonantinduction.wire.part;

import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TileMultipart;

public class PartFlatSwitchWire extends PartFlatWire 
{
    @Override
    public boolean canConnectTo(Object obj)
    {
        if (this.world().isBlockIndirectlyGettingPowered(x(), y(), z()))
            return super.canConnectTo(obj);
        return false;
    }
    
    @Override
    public String getType()
    {
        return "resonant_induction_flat_switch_wire";
    }
    
    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        TileMultipart tile = tile();
        World w = world();
        
        if (item.getItem().itemID == Block.lever.blockID)
        {
            if (!w.isRemote)
            {
                PartFlatSwitchWire wire = (PartFlatSwitchWire) MultiPartRegistry.createPart("resonant_induction_flat_wire", false);
                wire.isInsulated = this.isInsulated;
                wire.color = this.color;
                wire.connections = this.connections;
                wire.material = this.material;
                wire.side = this.side;
                wire.connMap = this.connMap;
                
                if (tile.canReplacePart(this, wire))
                {
                    tile.remPart(this);
                    TileMultipart.addPart(w, new BlockCoord(tile), wire);
                    
                    if (!player.capabilities.isCreativeMode)
                    {
                        tile.dropItems(Collections.singletonList(new ItemStack(Block.lever, 1)));
                    }
                }
            }
            return true;
        }
        else
        {
            return super.activate(player, part, item);
        }
    }
}
