package org.obd.query.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.bbop.dataadapter.DataAdapterException;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.OBOSession;

/**
 * A OBOSessionShard that supports specific loading operations
 * @author cjm
 *
 */
public class MutableOBOSessionShard extends OBOSessionShard {

	Collection pathsLoaded = new LinkedList<String>();
	public MutableOBOSessionShard(String f) throws DataAdapterException {
		super();
		loadFile(f);
	}
	public MutableOBOSessionShard() {
		super();
	}
	public int loadLocal(String ontName) throws DataAdapterException {
		String path = "/users/cjm/obo-all/"+ontName+"/"+ontName+".obo";
		return loadFile(path);
	}
	public int load(String ontName) throws DataAdapterException {
		String path = "http://purl.org/obo/obo-all/"+ontName+"/"+ontName+".obo";
		return loadFile(path);
	}
	public int loadFile(String path) throws DataAdapterException {
		OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
		config.getReadPaths().add(path);

		config.setAllowDangling(true);
		config.setBasicSave(false);
		config.setFailFast(false);
		OBOSession newSession = adapter.doOperation(OBOAdapter.READ_ONTOLOGY, config,
				null);
		if (session == null)
			session = newSession;
		else
			session.importSession(newSession, true);
		pathsLoaded.add(path);
		return newSession.getObjects().size();
	}
	public Collection getPathsLoaded() {
		return pathsLoaded;
	}
	

}
