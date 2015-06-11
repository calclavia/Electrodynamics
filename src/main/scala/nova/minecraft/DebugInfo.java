package nova.minecraft;

import java.util.List;

/**
 * Applied to Tiles that will display information in the F3 panel for debug
 * @author Calclavia
 */
public interface DebugInfo {
	/**
	 * Returns a list of string to render in the F3 debug screen.
	 */
	List<String> getDebugInfo();
}
