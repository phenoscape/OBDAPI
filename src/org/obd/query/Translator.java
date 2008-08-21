package org.obd.query;

import java.io.ByteArrayInputStream;
import org.antlr.runtime.*;
import org.obd.model.Node;
import org.obd.query.BooleanQueryTerm.BooleanOperator;
import org.obd.query.impl.OBDSQLShard;

public class Translator{
	
	private TranslatorLexer lexer;
	private TranslatorParser parser;
	CommonTokenStream tokens;
	
	
	public static void main(String[] args) throws Exception{
		
		// This is all testing stuff....
		
		String jdbcPath = "jdbc:postgresql://spade.lbl.gov:5432/obd_phenotype_200805";
		
		Shard shard = new OBDSQLShard();
		((OBDSQLShard)shard).connect(jdbcPath,"cjm",null);
		
		Translator t = new Translator();
		QueryTerm qt = t.parse("or[+link[?predicate=\"is_a\"?object=\"ZFA:0000114\"]+link[?predicate=\"part_of\"?object=\"ZFA:0000114\"]]");
		
		BooleanQueryTerm bqt = new BooleanQueryTerm();
		LinkQueryTerm lqt1 = new LinkQueryTerm();
		LinkQueryTerm lqt2 = new LinkQueryTerm();
		
		lqt1.setRelation("is_a");
		lqt1.setTarget("ZFA:0000114");
		
		lqt2.setRelation("part_of");
		lqt2.setTarget("ZFA:0000114");
		
		bqt.addQueryTerm(lqt1);
		bqt.addQueryTerm(lqt2);
		bqt.setOperator(BooleanOperator.OR);
		
		
		System.out.println(qt.toString());
		System.out.println(bqt.toString());
		
		for (Node n : shard.getNodesByQuery(qt)){
			System.out.println(n.getId() + "\t" + n.getLabel());
		}
		 
	}
	
	public QueryTerm parse(String input) throws Exception{
		
		ANTLRInputStream is = new ANTLRInputStream(new ByteArrayInputStream(input.getBytes()));
		this.lexer = new TranslatorLexer(is);
		this.tokens = new CommonTokenStream(this.lexer);
		this.parser = new TranslatorParser(this.tokens);
		TranslatorParser.query_return qr = this.parser.query();
		return qr.queryComponent;

	}
		
}