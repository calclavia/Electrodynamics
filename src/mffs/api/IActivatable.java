package mffs.api;

public interface IActivatable
{
	public boolean isActive();

	public void toggleActive();

	public void setActive(boolean flag);
}
