package dark.api.reciepes;

/** Advanced version of the assemblyRecipe. This is also used to display the recipe like a blueprint
 * 
 * @author DarkGuardsman */
public interface IBlueprint extends IAssemblyRecipe
{
    /** Check if the blueprint can be used by the object
     * 
     * @param object - player, assembler,drone, entity, block, tileEntity
     * @return true if it can be used. This is mainly used for disabling recipes for players */
    public boolean canUseBlueprint(Object object);

    /** Should a blueprint item be created for this blueprint. */
    public boolean createItemFor();

}
