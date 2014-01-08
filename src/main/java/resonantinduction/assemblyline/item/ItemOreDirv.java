package resonantinduction.assemblyline.item;

import java.util.List;

import resonantinduction.assemblyline.AssemblyLine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.EnumMaterial;
import com.builtbroken.minecraft.EnumOrePart;
import com.builtbroken.minecraft.IExtraInfo.IExtraItemInfo;
import com.builtbroken.minecraft.LaserEvent;
import com.builtbroken.minecraft.prefab.ItemBasic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A series of items that are derived from a basic material
 * 
 * @author DarkGuardsman */
public class ItemOreDirv extends ItemBasic implements IExtraItemInfo
{
    public ItemOreDirv()
    {
        super(DarkCore.getNextItemId(), "Metal_Parts", AssemblyLine.CONFIGURATION);
        this.setHasSubtypes(true);
        this.setCreativeTab(CreativeTabs.tabMaterials);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        if (itemStack != null)
        {
            return "item." + AssemblyLine.PREFIX + EnumOrePart.getFullName(itemStack.getItemDamage());
        }
        else
        {
            return this.getUnlocalizedName();
        }
    }

    @Override
    public Icon getIconFromDamage(int i)
    {
        return EnumMaterial.getIcon(i);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        for (EnumMaterial mat : EnumMaterial.values())
        {
            mat.itemIcons = new Icon[EnumOrePart.values().length];
            for (EnumOrePart part : EnumOrePart.values())
            {
                if (mat.shouldCreateItem(part))
                {
                    mat.itemIcons[part.ordinal()] = iconRegister.registerIcon(AssemblyLine.PREFIX + mat.simpleName + part.simpleName);
                }
            }
        }
    }

    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (EnumMaterial mat : EnumMaterial.values())
        {
            for (EnumOrePart part : EnumOrePart.values())
            {
                ItemStack stack = EnumMaterial.getStack(this, mat, part, 1);
                if (stack != null && mat.shouldCreateItem(part) && mat.itemIcons[part.ordinal()] != null)
                {
                    par3List.add(stack);
                }
            }
        }
    }

    @Override
    public boolean hasExtraConfigs()
    {
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
            //System.out.println("  [OrenameDebug]Mat:" + mat.simpleName);
            for (EnumOrePart part : EnumOrePart.values())
            {
                if (mat.shouldCreateItem(part))
                {
                    String name = mat.getOreName(part);
                    ItemStack stack = mat.getStack(this, part, 1);
                    //System.out.println("    [OrenameDebug]Name:" + name + " Stack:" + stack.toString());
                    OreDictionary.registerOre(name, stack);
                }
            }
        }
    }

    @ForgeSubscribe
    public void LaserSmeltEvent(LaserEvent.LaserDropItemEvent event)
    {
        if (event.items != null)
        {
            for (int i = 0; i < event.items.size(); i++)
            {
                if (event.items.get(i).itemID == Block.blockIron.blockID)
                {
                    event.items.set(i, EnumMaterial.getStack(this, EnumMaterial.IRON, EnumOrePart.MOLTEN, event.items.get(i).stackSize * 9));
                }
                else if (event.items.get(i).itemID == Block.blockGold.blockID)
                {
                    event.items.set(i, EnumMaterial.getStack(this, EnumMaterial.GOLD, EnumOrePart.MOLTEN, event.items.get(i).stackSize * 9));
                }
                else if (event.items.get(i).itemID == Block.oreIron.blockID)
                {
                    event.items.set(i, EnumMaterial.getStack(this, EnumMaterial.IRON, EnumOrePart.MOLTEN, event.items.get(i).stackSize));
                }
                else if (event.items.get(i).itemID == Block.oreGold.blockID)
                {
                    event.items.set(i, EnumMaterial.getStack(this, EnumMaterial.GOLD, EnumOrePart.MOLTEN, event.items.get(i).stackSize));
                }

                String oreName = OreDictionary.getOreName(OreDictionary.getOreID(event.items.get(i)));

                if (oreName != null)
                {
                    for (EnumMaterial mat : EnumMaterial.values())
                    {
                        if (oreName.equalsIgnoreCase("ore" + mat.simpleName) || oreName.equalsIgnoreCase(mat.simpleName + "ore"))
                        {
                            event.items.set(i, mat.getStack(this, EnumOrePart.MOLTEN, event.items.get(i).stackSize + 1 + event.world.rand.nextInt(3)));
                            break;
                        }
                        else if (oreName.equalsIgnoreCase("ingot" + mat.simpleName) || oreName.equalsIgnoreCase(mat.simpleName + "ingot"))
                        {
                            event.items.set(i, mat.getStack(this, EnumOrePart.MOLTEN, event.items.get(i).stackSize));
                            break;
                        }
                    }
                }
            }
        }
    }
}
