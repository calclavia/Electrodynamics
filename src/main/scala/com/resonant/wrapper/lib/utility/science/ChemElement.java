package com.resonant.wrapper.lib.utility.science;

/**
 * List of element from the periodic table of elements for any kind of use. This list is not
 * complete for all parts but each element should have a listed names, symbol, and atomic mass.
 * Atomic number should be the placement # in the list. Var zero should not be used as its designed
 * to offset the placement of all elements by one. As well is returned instead of null.
 * @author Robert Seifert
 * @Source http://www.periodictable.com/Properties/A/SpecificHeat.an.html
 * @source http://www.chemicalelements.com/
 * @source http://www.lenntech.com/periodic/periodic-chart.htm
 */
public enum ChemElement {
	/**
	 * Placeholder so that hydrogen starts as number one
	 */
	ZERO("zero", "zero", 0, 0, null, null),
	Hydrogen("Hydrogen", "H", 1.00794f, 0.08988f, ElementProperty.nonmetal, MatterPhase.gas, new HeatingData(14.01f, 20.28f, 0.558f, 0.558f, 14300f)),
	Helium("Helium", "He", 4.002602f, 0.1785f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(0, 4.22f, 0.02f, 0.083f, 5193.1f)),
	Lithium("Lithium", "Li", 6.941f, 0.53f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(543.69f, 1615f, 3f, 147f, 3570f)),
	Beryllium("Beryllium", "Be", 9.012182f, 1.8477f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(1560f, 2743f, 7.95f, 297f, 1820f)),
	Boron("Boron", "B", 10.811f, 2.46f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(2348f, 4273f, 50f, 507f, 1030f)),
	Carbon("Carbon", "C", 12.0107f, 2.26f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(3823f, 4300f, 105f, 715f, 710f)),
	Nitrogen("Nitrogen", "N", 14.0067f, 1.251f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(63.05f, 77.36f, 0.36f, 2.79f, 1040)),
	Oxygen("Oxygen", "O", 15.9994f, 1.429f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(54.8f, 90.2f, 0.222f, 3.41f, 919f)),
	Fluorine("Fluorine", "F", 18.9994f, 1.696f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(53.5f, 85.03f, 0.26f, 3.27f, 824f)),
	Neon("Neon", "Ne", 20.1797f, 0.9f, ElementProperty.inertGas, MatterPhase.gas, new HeatingData(24.56f, 27.07f, 0.34f, 1.75f, 1030f)),
	Sodium("Sodium", "Na", 22.98976928f, 0.968f, ElementProperty.alkaliMetal, MatterPhase.solid, new HeatingData(370.87f, 1156f, 2.6f, 97.7f, 1230f)),

	Magnesium("Magnesium", "Mg", 24.305f, 1.738f, ElementProperty.alkalineEarthMetal, MatterPhase.solid),
	aluminium("aluminium", "Al", 26.9815386f, 2.7f, ElementProperty.otherMetal, MatterPhase.solid),
	Silicon("Silicon", "Si", 28.0855f, 2.33f, ElementProperty.otherMetal, MatterPhase.solid),

	Phosphorus("Phosphorus", "P", 30.973762f, 1.823f, ElementProperty.nonmetal, MatterPhase.solid),
	Sulphur("Sulphur", "S", 32.065f, 1.96f, ElementProperty.nonmetal, MatterPhase.solid),
	Chlorine("Chlorine", "Cl", 35.453f, 3.214f, ElementProperty.halogen, MatterPhase.gas),

	Argon("Argon", "Ar", 39.948f, 1.784f, ElementProperty.inertGas, MatterPhase.gas),
	Potassium("Potassium", "K", 39.0983f, 0.856f, ElementProperty.alkaliMetal, MatterPhase.solid),
	Calcium("Calcium", "Ca", 40.078f, 1.55f, ElementProperty.alkalineEarthMetal, MatterPhase.solid),
	Scandium("Scandium", "Sc", 44.955912f, 2.985f, ElementProperty.transitionMetal, MatterPhase.solid),

	Titanium("Titanium", "Ti", 47.867f, 4.507f, ElementProperty.transitionMetal, MatterPhase.solid),
	Vanadium("Vanadium", "V", 50.9415f, 6.11f, ElementProperty.transitionMetal, MatterPhase.solid),
	Chromium("Chromium", "Cr", 51.9961f, 7.14f, ElementProperty.transitionMetal, MatterPhase.solid),

	Manganese("Manganese", "Mn", 54.938045f, 7.47f, ElementProperty.transitionMetal, MatterPhase.solid),
	Iron("Iron", "Fe", 55.845f, 7.874f, ElementProperty.transitionMetal, MatterPhase.solid),
	Cobalt("Cobalt", "Co", 58.933195f, 8.9f, ElementProperty.transitionMetal, MatterPhase.solid),

	Nickel("Nickel", "Ni", 58.6934f, 8.908f, ElementProperty.transitionMetal, MatterPhase.solid),
	Copper("Copper", "Cu", 63.546f, 8.92f, ElementProperty.transitionMetal, MatterPhase.solid),
	Zinc("Zinc", "Zn", 65.38f, 7.14f, ElementProperty.transitionMetal, MatterPhase.solid),

	Gallium("Gallium", "Ga", 69.723f, 5.904f, ElementProperty.otherMetal, MatterPhase.solid),
	Germanium("Germanium", "Ge", 72.64f, 5.323f, ElementProperty.semimetallic, MatterPhase.solid),
	Arsenic("Arsenic", "As", 74.9216f, 5.727f, ElementProperty.semimetallic, MatterPhase.solid),

	Selenium("Selenium", "Se", 78.96f, 4.819f, ElementProperty.nonmetal, MatterPhase.solid),
	Bromine("Bromine", "Br", 79.904f, 3.12f, ElementProperty.halogen, MatterPhase.liquid),
	Krypton("Krypton", "Kr", 83.798f, 3.75f, ElementProperty.inertGas, MatterPhase.gas),

	Rubidium("Rubidium", "Rb", 85.4678f, 1.532f, ElementProperty.alkaliMetal, MatterPhase.solid),
	Strontium("Strontium", "Sr", 87.62f, 2.63f, ElementProperty.alkalineEarthMetal, MatterPhase.solid),
	Yttrium("Yttrium", "Y", 88.90585f, 4.472f, ElementProperty.transitionMetal, MatterPhase.solid),

	Zirkonium("Zirkonium", "Zr", 91.224f, 6.511f, ElementProperty.transitionMetal, MatterPhase.solid),
	Niobium("Niobium", "Nb", 92.90638f, 8.57f, ElementProperty.transitionMetal, MatterPhase.solid),
	Molybdaenum("Molybdaenum", "Mo", 95.96f, 10.28f, ElementProperty.transitionMetal, MatterPhase.solid),

	Technetium("Technetium", "Tc", 98f, 11.5f, ElementProperty.transitionMetal, MatterPhase.solid),
	Ruthenium("Ruthenium", "Ru", 101.07f, 12.37f, ElementProperty.transitionMetal, MatterPhase.solid),
	Rhodium("Rhodium", "Rh", 102.9055f, 12.45f, ElementProperty.transitionMetal, MatterPhase.solid),

	Palladium("Palladium", "Pd", 106.42f, 12.023f, ElementProperty.transitionMetal, MatterPhase.solid),
	Silver("Silver", "Ag", 107.8682f, 10.49f, ElementProperty.transitionMetal, MatterPhase.solid),
	Cadmium("Cadmium", "Cd", 112.411f, 8.65f, ElementProperty.transitionMetal, MatterPhase.solid),

	Indium("Indium", "In", 114.818f, 7.31f, ElementProperty.otherMetal, MatterPhase.solid),
	Tin("Tin", "Sn", 118.71f, 7.31f, ElementProperty.otherMetal, MatterPhase.solid),
	Antimony("Antimony", "Sb", 121.76f, 6.697f, ElementProperty.semimetallic, MatterPhase.solid),

	Tellurium("Tellurium", "Te", 127.6f, 6.24f, ElementProperty.semimetallic, MatterPhase.solid),
	Iodine("Iodine", "I", 126.90447f, 4.94f, ElementProperty.halogen, MatterPhase.solid),
	Xenon("Xenon", "Xe", 131.293f, 5.9f, ElementProperty.inertGas, MatterPhase.gas),

	Cesium("Cesium", "Cs", 132.9054519f, 1.879f, ElementProperty.alkaliMetal, MatterPhase.solid),
	Barium("Barium", "Ba", 137.327f, 3.51f, ElementProperty.alkalineEarthMetal, MatterPhase.solid),
	Lanthanum("Lanthanum", "La", 138.90547f, 6.146f, ElementProperty.lanthanide, MatterPhase.solid),

	Cerium("Cerium", "Ce", 140.116f, 6.689f, ElementProperty.lanthanide, MatterPhase.solid),
	Praseodymium("Praseodymium", "Pr", 140.90765f, 6.64f, ElementProperty.lanthanide, MatterPhase.solid),
	Neodymium("Neodymium", "Nd", 144.242f, 7.01f, ElementProperty.lanthanide, MatterPhase.solid),

	Promethium("Promethium", "Pm", 145f, 7.264f, ElementProperty.lanthanide, MatterPhase.solid),
	Samarium("Samarium", "Sm", 150.36f, 7.353f, ElementProperty.lanthanide, MatterPhase.solid),
	Europium("Europium", "Eu", 151.964f, 5.244f, ElementProperty.lanthanide, MatterPhase.solid),

	Gadolinium("Gadolinium", "Gd", 157.25f, 7.901f, ElementProperty.lanthanide, MatterPhase.solid),
	Terbium("Terbium", "Tb", 158.92535f, 8.219f, ElementProperty.lanthanide, MatterPhase.solid),
	Dysprosium("Dysprosium", "Dy", 162.5001f, 8.551f, ElementProperty.lanthanide, MatterPhase.solid),

	Holmium("Holmium", "Ho", 164.93032f, 8.795f, ElementProperty.lanthanide, MatterPhase.solid),
	Erbium("Erbium", "Er", 167.259f, 9.066f, ElementProperty.lanthanide, MatterPhase.solid),
	Thulium("Thulium", "Tm", 168.93421f, 9.321f, ElementProperty.lanthanide, MatterPhase.solid),

	Ytterbium("Ytterbium", "Yb", 173.054f, 6.57f, ElementProperty.lanthanide, MatterPhase.solid),
	Lutetium("Lutetium", "Lu", 174.9668f, 9.841f, ElementProperty.lanthanide, MatterPhase.solid),
	Hafnium("Hafnium", "Hf", 178.49f, 13.31f, ElementProperty.lanthanide, MatterPhase.solid),

	Tantalum("Tantalum", "Ta", 180.94788f, 16.65f, ElementProperty.transitionMetal, MatterPhase.solid),
	Tungsten("Tungsten", "W", 183.84f, 19.25f, ElementProperty.transitionMetal, MatterPhase.solid),
	Rhenium("Rhenium", "Re", 186.207f, 21.02f, ElementProperty.transitionMetal, MatterPhase.solid),

	Osmium("Osmium", "Os", 190.23f, 22.59f, ElementProperty.transitionMetal, MatterPhase.solid),
	Iridium("Iridium", "Ir", 192.217f, 22.56f, ElementProperty.transitionMetal, MatterPhase.solid),
	Platinum("Platinum", "Pt", 192.084f, 21.09f, ElementProperty.transitionMetal, MatterPhase.solid),

	Gold("Gold", "Au", 196.966569f, 19.3f, ElementProperty.transitionMetal, MatterPhase.solid),
	Hydrargyrum("Hydrargyrum", "Hg", 200.59f, 13.534f, ElementProperty.transitionMetal, MatterPhase.solid),
	Thallium("Thallium", "Tl", 204.3833f, 11.85f, ElementProperty.transitionMetal, MatterPhase.solid),

	Lead("Lead", "Pb", 207.2f, 11.34f, ElementProperty.otherMetal, MatterPhase.solid),
	Bismuth("Bismuth", "Bi", 208.980401f, 9.78f, ElementProperty.otherMetal, MatterPhase.solid),
	Polonium("Polonium", "Po", 210f, 9.196f, ElementProperty.semimetallic, MatterPhase.solid),

	Astatine("Astatine", "At", 210f, 0, ElementProperty.halogen, MatterPhase.solid),
	Radon("Radon", "Rn", 220f, 9.73f, ElementProperty.inertGas, MatterPhase.gas),
	Francium("Francium", "Fr", 223f, 0f, ElementProperty.alkaliMetal, MatterPhase.solid),

	Radium("Radium", "Ra", 226f, 5f, ElementProperty.alkalineEarthMetal, MatterPhase.solid),
	Actinium("Actinium", "Ac", 227f, 10.07f, ElementProperty.actinide, MatterPhase.solid),
	Thorium("Thorium", "Th", 232.03806f, 11.724f, ElementProperty.actinide, MatterPhase.solid),

	Protactinium("Protactinium", "Pa", 231.03588f, 15.37f, ElementProperty.actinide, MatterPhase.solid),
	Uranium("Uranium", "U", 238.02891f, 19.05f, ElementProperty.actinide, MatterPhase.solid),
	Neptunium("Neptunium", "Np", 237f, 20.45f, ElementProperty.actinide, MatterPhase.solid),

	Plutonium("Plutonium", "Pu", 244f, 19.816f, ElementProperty.actinide, MatterPhase.solid),
	Americium("Americium", "Am", 243f, 13.67f, ElementProperty.actinide, MatterPhase.solid),
	Curium("Curium", "Cm", 247f, 3.51f, ElementProperty.actinide, MatterPhase.solid),

	Berkelium("Berkelium", "Bk", 247f, 14.78f, ElementProperty.actinide, MatterPhase.solid),
	Californium("Californium", "Cf", 251f, 15.1f, ElementProperty.actinide, MatterPhase.solid),
	Einsteinium("Einsteinium", "Es", 252f, 0, ElementProperty.actinide, MatterPhase.solid),

	Fermium("Fermium", "Fm", 257f, 0, ElementProperty.actinide, MatterPhase.solid),
	Mendelevium("Mendelevium", "Md", 0, 258f, ElementProperty.actinide, MatterPhase.solid),
	Nobelium("Nobelium", "No", 259f, 0, ElementProperty.actinide, MatterPhase.solid),

	Lawrencium("Lawrencium", "Lr", 262f, 0, ElementProperty.actinide, MatterPhase.solid),
	Rutherfordium("Rutherfordium", "Rf", 261f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Dubnium("Dubnium", "Db", 262f, 0, ElementProperty.transitionMetal, MatterPhase.solid),

	Seaborgium("Seaborgium", "Sg", 266f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Bohrium("Bohrium", "Bh", 264f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Hassium("Hassium", "Hs", 277f, 0, ElementProperty.transitionMetal, MatterPhase.solid),

	Meitnerium("Meitnerium", "Mt", 268f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Ununnilium("Ununnilium", "Ds", 271f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Unununium("Unununium", "Rg", 272f, 0, ElementProperty.transitionMetal, MatterPhase.solid),

	Ununbium("Ununbium", "Uub", 285f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Ununtrium("Ununtrium", "Uut", 284f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Ununquadium("Ununquadium", "Uuq", 289f, 0, ElementProperty.transitionMetal, MatterPhase.solid),

	Ununpentium("Ununpentium", "Uup", 288f, 0, ElementProperty.transitionMetal, MatterPhase.solid),
	Ununhexium("Ununhexium", "Uuh", 292f, 0, ElementProperty.transitionMetal, MatterPhase.solid);

	/**
	 * value units are (g/cm^3) aka grams per centimeter cubed
	 */
	public float density;
	/**
	 * value units are (amu) aka atomic mass unit
	 */
	public float atomicMass;

	public String elementName = "element";
	public String[] elementNames;
	public String elementSymbol = "element";

	public ElementProperty classification;

	public MatterPhase normalPhase;

	public HeatingData heatData;

	private ChemElement(String[] name, String symbol, float atomicMass, float density, ElementProperty type, MatterPhase defaultPhase) {
		this(name[0], symbol, atomicMass, density, type, defaultPhase);
		this.elementNames = name;
	}

	private ChemElement(String name, String symbol, float atomicMass, float density, ElementProperty type, MatterPhase defaultPhase) {
		this.elementName = name;
		this.elementSymbol = symbol;
		this.atomicMass = atomicMass;
		this.classification = type;
		this.normalPhase = defaultPhase;
		this.density = density;
	}

	private ChemElement(String name, String symbol, float atomicMass, float density, ElementProperty type, MatterPhase defaultPhase, HeatingData heatData) {
		this(name, symbol, atomicMass, density, type, defaultPhase);
		this.heatData = heatData;

	}

}
