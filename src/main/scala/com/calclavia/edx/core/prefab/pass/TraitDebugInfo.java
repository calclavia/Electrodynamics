package com.calclavia.edx.core.prefab.pass;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import resonantengine.api.tile.IDebugInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Calclavia
 */
public class TraitDebugInfo extends TileMultipart implements IDebugInfo
{
	@Override
	public List<String> getDebugInfo()
	{
		List<String> list = new ArrayList<String>();

		for (TMultiPart node : jPartList())
		{
			if (node instanceof IDebugInfo)
			{
				list.addAll(((IDebugInfo) node).getDebugInfo());
			}
		}

		return list;
	}
}
