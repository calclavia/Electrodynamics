package resonantinduction.base;

import com.google.common.io.ByteArrayDataInput;

public interface IPacketReceiver 
{
	public void handle(ByteArrayDataInput input);
}
