package resonantinduction.core.resource.item;

import java.util.List;

import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonantinduction.core.Reference;
import resonantinduction.core.TabRI;
import resonantinduction.core.resource.ResourceGenerator;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.oredict.OreDictionary;

/** Modified version of the MC bucket to meet the needs of a dynamic fluid registry system
 * 
 * @author Darkguardsman */
public class ItemFluidBucket extends ItemBucket
{
    public ItemFluidBucket(int id)
    {
        super(id, 0);
        setContainerItem(Item.bucketEmpty);
        setUnlocalizedName(Reference.PREFIX + "Bucket_Molten");
        setTextureName(Reference.PREFIX + "Bucket_Molten");
        setCreativeTab(CreativeTabs.tabMisc);
        setHasSubtypes(true);
        setMaxDamage(0);
    }
    
    @Override
    public String getItemDisplayName(ItemStack is)
    {
        String dustName = getMaterialFromStack(is);

        if (dustName != null)
        {
            List<ItemStack> list = OreDictionary.getOres("ingot" + dustName.substring(0, 1).toUpperCase() + dustName.substring(1));

            if (list.size() > 0)
            {
                ItemStack type = list.get(0);

                String name = type.getDisplayName().replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "");
                return (LanguageUtility.getLocal(this.getUnlocalizedName() + ".name")).replace("%v", name).replace("  ", " ");
            }
        }

        return "";
    }

    @Override
    public ItemStack onItemRightClick(ItemStack bucket, World world, EntityPlayer player)
    {        
        //TODO pull fluid instance from metadata
        return super.onItemRightClick(bucket, world, player);
    }

    @Override
    public boolean tryPlaceContainedLiquid(World par1World, int par2, int par3, int par4)
    {
        return super.tryPlaceContainedLiquid(par1World, par2, par3, par4);
    }
    
    public static String getMaterialFromStack(ItemStack itemStack)
    {
        if (ResourceGenerator.materialNames.size() > itemStack.getItemDamage())
            return ResourceGenerator.materialNames.get(itemStack.getItemDamage());
        return null;
    }
    
    public ItemStack getStackFromMaterial(String name)
    {
        ItemStack itemStack = new ItemStack(this);
        itemStack.setItemDamage(ResourceGenerator.materialNames.indexOf(name));
        return itemStack;
    }

    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (String materialName : ResourceGenerator.materialNames)
        {
            par3List.add(getStackFromMaterial(materialName));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack itemStack, int par2)
    {
        /**
         * Auto-color based on the texture of the ingot.
         */
        String name = ItemOreResource.getMaterialFromStack(itemStack);

        if (ResourceGenerator.materialColors.containsKey(name))
        {
            return ResourceGenerator.materialColors.get(name);
        }

        return 16777215;
    }
}
