package resonantinduction;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;

/**
 * @author Calclavia
 * 
 */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = ResonantInduction.VERSION)
public class ResonantInduction
{
	public static final String ID = "ResonantInduction";
	public static final String NAME = "Resonant Induction";

	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String BUILD_VERSION = "@BUILD@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;

	@Instance(ID)
	public static ResonantInduction INSTNACE;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;
}
