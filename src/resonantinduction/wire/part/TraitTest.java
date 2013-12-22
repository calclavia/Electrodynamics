package resonantinduction.wire.part;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitTest extends TileMultipart implements ITest
{
	public Set<ITest> interfaces = new HashSet<ITest>();

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{

		TMultiPart part = partMap(from.ordinal());

		if (part != null)
		{
			for (ITest conductor : this.interfaces)
			{
				if (conductor == part)
				{
					((ITest) conductor).onReceiveEnergy(from, receive, doReceive);
					System.out.println("RECEIVING");
				}
			}
		}
		return 0;

	}

}
