package resonantinduction.base;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

public interface IPacketReceiver 
{
	public void handle(ByteArrayDataInput input);
	
	public ArrayList getNetworkedData(ArrayList data);
}
