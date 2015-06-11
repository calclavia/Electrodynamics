package com.resonant.wrapper.lib.utility.text;

public enum TextFormat {
	RANDOMCHARS("\247k"),
	BOLD("\247l"),
	STRIKE("\247m"),
	UNDERLINE("\247n"),
	ITALICS("\247o"),
	RESETFORMAT("\247r");

	private final String formatString;

	TextFormat(String format) {
		this.formatString = format;
	}

	public final String getFormatString() {
		return formatString;
	}
}