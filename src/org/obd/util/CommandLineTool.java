package org.obd.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.Shard;
import org.purl.obo.vocab.RelationVocabulary;

public class CommandLineTool {

	protected Shard shard;
	static String defaultJdbcPath = "jdbc:postgresql://localhost:9999/obd_phenotype_200805";
	String dbUsername = "cjm";
	String dbPassword = "";
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();
	protected TermVocabulary termVocabulary = new TermVocabulary();

	final String ABNORMAL = "PATO:0000460";
	final String QUALITY = "PATO:0000001";
	final String GENE = "SO:0000704";
	final String IN_ORGANISM = "OBO_REL:in_organism";

	public void initLogger(Level level) {
		Handler[] handlers = Logger.getLogger( "" ).getHandlers();
		for ( int index = 0; index < handlers.length; index++ ) {
			handlers[index].setLevel( level );
		}
		Logger.getLogger( "org.bbop.rdbms").setLevel(level);
		Logger.getLogger( "org.obd").setLevel(level);
		
	}

}
