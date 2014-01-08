package resonantinduction.fluid.network;

/**
 * Use this if you want to take advantage of the {@link #FluidCraftingHandler} 's auto crafting
 * methods to do as little as work as possible to create recipe results
 * 
 * @author DarkGuardsman
 */
public interface IFluidRecipeCrafter
{
	/**
	 * After calling {@link #FluidCraftingHandler} 's crafting method this will be called to setup
	 * the end result of all 3 objects. That is if crafting was not called for calculations only
	 */
	public void setRecipeObjectContent(Object receivingObject, int usedReceivingVolume, Object inputObject, int usedInputVolume, Object resultObject, int resultCreatedVolume);

	/** Stack that is receiving the input object (ItemStack & FluidStack are best) */
	public Object getReceivingObjectStack();

	/** Stack that will be received by the receiving object (ItemStack & FluidStack are best) */
	public Object getInputObjectStack();
}
