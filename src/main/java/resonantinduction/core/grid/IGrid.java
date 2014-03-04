package resonantinduction.core.grid;

import java.util.Set;

public interface IGrid<N>
{
	public void add(N node);

	public void remove(N node);

	public Set<N> getNodes();

	public void reconstruct();
}
