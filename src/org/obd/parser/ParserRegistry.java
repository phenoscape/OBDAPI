package org.obd.parser;

public class ParserRegistry {

	protected Class[] parsers = 
	{
			GeneAssociationParser.class,
			GZIPParser.class,
			HomologeneParser.class,
			MGIGenotypePhenotypeParser.class,
			OBOFormatParser.class,
			ZFINPhenotypeEnvironmentParser.class,
			ZFINGenotypeFeatureParser.class,
			ZFINGenotypePhenotypeOBOParser.class
	};
	
	public Class[] getParsers() {
		return parsers;
	}

}
