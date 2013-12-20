package com.builtbroken.minecraft.recipes;

import java.util.HashMap;

import net.minecraft.item.ItemStack;

import com.builtbroken.common.Pair;

public class AssemblyObjectManager
{
    /** Generic item or block based recipes. Entity recipes are handled by the entity */
    private HashMap<Pair<Integer, Integer>, IAssemblyRecipe> itemRecipes = new HashMap();

    private static AssemblyObjectManager instance;

    public static AssemblyObjectManager instance()
    {
        if (instance == null)
        {
            instance = new AssemblyObjectManager();
        }
        return instance;
    }

    public IAssemblyRecipe getRecipeFor(Object object)
    {
        IAssemblyRecipe re = null;

        if (re instanceof IAssemblyObject)
        {
            re = ((IAssemblyObject) object).getRecipe(object);
        }

        if (re == null && object instanceof ItemStack)
        {
            re = itemRecipes.get(new Pair<Integer, Integer>(((ItemStack) object).itemID, ((ItemStack) object).getItemDamage()));
            if (re == null && ((ItemStack) object).getItem() instanceof IAssemblyObject)
            {
                re = ((IAssemblyObject) ((ItemStack) object).getItem()).getRecipe(object);
            }
        }

        return re;
    }
}
