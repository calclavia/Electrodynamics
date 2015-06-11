package com.resonant.wrapper.lib.utility.text;

import java.awt.*;

public enum TextColor {
	BLACK("\u00a70", 0x000000, 0, 0, 0),
	DARKBLUE("\u00a71", 0x0000AA, 0, 0, 170),
	DARKGREEN("\u00a72", 0x00AA00, 0, 170, 0),
	DARKAQUA("\u00a73", 0x00AAAA, 0, 170, 170),
	DARKRED("\u00a74", 0xAA0000, 170, 0, 0),
	PURPLE("\u00a75", 0xAA00AA, 170, 0, 170),
	GOLD("\u00a76", 0xFFAA00, 255, 170, 0),
	GREY("\u00a77", 0xAAAAAA, 170, 170, 170),
	DARKGREY("\u00a78", 0x555555, 85, 85, 85),
	INDIGO("\u00a79", 0x5555FF, 85, 85, 255),
	BRIGHTGREEN("\u00a7a", 0x55FF55, 85, 255, 85),
	AQUA("\u00a7b", 0x55FFFF, 85, 255, 255),
	RED("\u00a7c", 0xFF5555, 255, 85, 85),
	PINK("\u00a7d", 0xFF55FF, 255, 85, 255),
	YELLOW("\u00a7e", 0xFFFF55, 255, 255, 85),
	WHITE("\u00a7f", 0xFFFFFF, 255, 255, 255);

	private String colorString;
	private int hexadecimal;
	private Color colorInstance;

	TextColor(String color, int hexadecimalColor, int red, int green, int blue) {
		colorString = color;
		hexadecimal = hexadecimalColor;
		colorInstance = new Color(red, green, blue);
	}

	/**
	 * Retrieves the Hexadecimal integer value for the specified Color
	 * @return The Hexadecimal int for the Color *
	 */
	public int getHexValue() {
		return hexadecimal;
	}

	/**
	 * Retrieves the <code>java.awt.Color</code> instance for the Color
	 * @return the java.awt.Color instance for this color **
	 */
	public Color getColor() {
		return colorInstance;
	}

	/**
	 * Retrieves the String that specifies the color of Text within Minecraft
	 * @return String that can be added to the beginning of another String to specify coloration
	 * in Minecraft *
	 */
	public String getColorString() {
		return colorString;
	}

	/**
	 * Retrieves an int Array to retrieve the RGB values for the specified Color. Index 0 is the
	 * Red value, Index 1 is the Green value, and Index 2 is the Blue value.
	 * @return Array of the primitive type int containing the RGB values for the specified color *
	 */
	public int[] getRGBIntArray() {
		return new int[] { colorInstance.getRed(), colorInstance.getGreen(), colorInstance.getBlue() };
	}
}