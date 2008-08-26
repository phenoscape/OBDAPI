package org.obd.parser;

import java.util.Collection;

import org.obd.model.Node;
import org.obd.model.Node.Metatype;
import org.obd.query.ComparisonQueryTerm.Operator;


/**
 * parses ZFIN-produced files for linking genotype to gene
 * <p>
 * @see http://zfin.org/data_transfer/Downloads/phenotype_environment.txt
 * @author cjm
 *
 */
public abstract class ZFINTabularParser extends TabularInfoParser {

	protected String src = "ZFIN";
	protected String taxId = ncbitaxId("7955");
	
	public ZFINTabularParser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ZFINTabularParser(String path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String createId(String local) {
		// some have IDs like ZDB:1223
		// change these to ZFIN:...
		if (local.contains(":")) {
			String[] toks = local.split(":");
			local = toks[1];
		}
		return zfinId(local);
	}


	
}
