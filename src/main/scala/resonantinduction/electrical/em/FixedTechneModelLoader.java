package resonantinduction.electrical.em;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.IModelCustomLoader;
import net.minecraftforge.client.model.ModelFormatException;

public class FixedTechneModelLoader implements IModelCustomLoader
{

	@Override
	public String getType()
	{
		return "Techne model";
	}

	private static final String[] types = { "tcn" };

	@Override
	public String[] getSuffixes()
	{
		return types;
	}

	@Override
	public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException
	{
		return new FixedTechneModel(resource);
	}

}
