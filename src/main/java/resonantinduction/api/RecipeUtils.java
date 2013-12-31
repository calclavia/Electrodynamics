package resonantinduction.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeUtils
{
    public static abstract class Resource
    {
        public final boolean hasChance;
        public final float chance;
        
        protected Resource()
        {
            this.hasChance = false;
            this.chance = 100;
        }
        
        protected Resource(float chance)
        {
            this.hasChance = true;
            this.chance = chance;
        }
        
        public abstract boolean isEqual(ItemStack is);
        
        public boolean hasChance()
        {
            return this.hasChance;
        }

        public float getChance()
        {
            return this.chance;
        }
    }
    
    public static class ItemStackResource extends Resource
    {
        public final ItemStack itemStack;

        public ItemStackResource(ItemStack is)
        {
            super();
            this.itemStack = is;
        }
        
        public ItemStackResource(ItemStack is, float chance)
        {
            super(chance);
            this.itemStack = is;
        }

        @Override
        public boolean isEqual(ItemStack is)
        {
            return is.equals(this.itemStack);
        }
    }
    
    public static class OreDictResource extends Resource
    {
        public final String name;

        public OreDictResource(String s)
        {
            super();
            this.name = s;
        }
        
        public OreDictResource(String s, float chance)
        {
            super(chance);
            this.name = s;
        }

        @Override
        public boolean isEqual(ItemStack is)
        {
            return OreDictionary.getOres(this.name).contains(is);
        }
    }

}
