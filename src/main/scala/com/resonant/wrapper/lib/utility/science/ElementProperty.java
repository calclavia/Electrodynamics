package com.resonant.wrapper.lib.utility.science;

public enum ElementProperty {
	nonmetal("Non-metal"),
	inertGas("Inert gas"),
	halogen("Halogen"),
	alkaliMetal("Alkali metal"),
	alkalineEarthMetal("Alkaline earth metal"),
	semimetallic("Metalloid"),
	otherMetal("Other metal"),
	transitionMetal("Transition metal"),
	lanthanide("Lanthanide"),
	actinide("Actinide");

	final String name;

	private ElementProperty(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
