package org.obd.ws;

import java.sql.SQLException;

import org.bbop.dataadapter.DataAdapterException;
import org.obd.query.Shard;
import org.obd.query.impl.ChadoSQLShard;
import org.obd.query.impl.MultiShard;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obd.query.impl.OBDSQLShard;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;



/**
 * Main OBD RESTLET application
 * @author cjm
 *
 */
public class OBDRestApplication extends Application {

	static String defaultJdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
	protected Shard shard;
	
	public static class Config {
		protected Shard shard;
		protected int port;

		public Config() {
			super();

		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public Shard getShard() {
			return shard;
		}

		public void setShard(Shard shard) {
			this.shard = shard;
		}


	}

	public static void main(String[] args) throws Exception {
		startServer(args);
	}

	public static Config parseMainArguments(String[] args) throws ClassNotFoundException, DataAdapterException {

		int port = 8182;
		String jdbcPath = defaultJdbcPath;

		// TODO: configurable
		// for now we hardcode a multishard wrapping an obosession
		// and a SQL shard
		MultiShard multiShard = new MultiShard();

		for (int i = 0; i < args.length; i++)
			System.err.println("args[" + i + "] = |" + args[i] + "|");

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-d")) {
				i++;
				String schema="obd";
				if (args[i].equals("-schema")) {
					i++;
					schema = args[i];
					i++;
				}
				jdbcPath = args[i];
				try {		
					OBDSQLShard obd;
					if (schema.equals("chado"))
						obd = new ChadoSQLShard();
					else
						obd = new OBDSQLShard();
					obd.connect(jdbcPath);
					multiShard.addShard(obd);
				} catch (SQLException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			else if (args[i].equals("-i")) {
				i++;
				String filePath = args[i];
				MutableOBOSessionShard moss = new MutableOBOSessionShard();
				moss.loadFile(args[i]);
				multiShard.addShard(moss);
			}
			else if (args[i].equals("-p")) {
				i++;
				port = Integer.parseInt(args[i]);
			}
			else {
				System.err.println("Unknown option: "+args[i]);
				System.exit(1);
			}
		}

		if (multiShard.getShards().size() == 0) {
			try {		
				OBDSQLShard obd = new OBDSQLShard();
				obd.connect(jdbcPath);
				multiShard.addShard(obd);
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		Config config = new Config();
		config.setShard(multiShard);
		config.setPort(port);
		return config;

	}

	public static void startServer(String[] args) throws Exception {
		// Create a component
		Component component = new Component();
		component.getClients().add(Protocol.FILE);
		component.getClients().add(Protocol.JAR);
		component.getClients().add(Protocol.CLAP);
		OBDRestApplication application = new OBDRestApplication(
				component.getContext());


		/*
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.loadLocal("cell");
		multiShard.addShard(moss);
		 */

		Config config = parseMainArguments(args);
		component.getServers().add(Protocol.HTTP, config.getPort());

		application.setShard(config.getShard());

		// Attach the application to the component and start it
		component.getDefaultHost().attach("", application);

		component.start();
	}



	public OBDRestApplication() {
		super();
	}

	public OBDRestApplication(Context context) {
		super(context);
	}

	/**
	 * @return local location of pages (css, js, ftl, html)
	 * 
	 * This is typically a relative location within the source tree.
	 * This can be accessed at runtime via CLAP - either directly
	 * in the source tree, or via the jar. See RESTlet docs for details on CLAP
	 */
	public String getPagesDirectoryRelativePath() {
		return "/org/obd/ws/pages";
	}



	/**
	 * pages (js,css,html,ftl,etc) are all managed locally within this project.
	 * This call sets up the corresponding directories from these within the
	 * RESTlet server:
	 * 
	 *   /css/
	 *   /js/
	 *   /images/
	 *   
	 * Any URL starting with this pattern will be mapped to the relevant
	 * portion of  getPagesDirectoryRelativePath()
	 * 
	 * @param router
	 */
	public void setupDirectories(Router router) {
		String base = getPagesDirectoryRelativePath();
		Directory cssDirectory = 
			new Directory(getContext(), 
					LocalReference.createClapReference(LocalReference.CLAP_CLASS, 
							base+"/css/"));
		router.attach("/css/", cssDirectory);

		Directory imagesDirectory = new Directory(getContext(), 
				LocalReference.createClapReference(LocalReference.CLAP_CLASS, 
						base+"/images/"));
		router.attach("/images/", imagesDirectory);

		Directory jsDirectory = new Directory(getContext(), 
				LocalReference.createClapReference(LocalReference.CLAP_CLASS, 
						base+"/js/"));
		router.attach("/js/", jsDirectory);	
	}

	/**
	 * TODO: smart way of overriding this on application specific basis
	 */
	@Override
	public Restlet createRoot() {
		Router router = new Router(getContext());
		attachRoutes(router);
		attachApplicationSpecificRoutes(router);
		return router;
	}
	
	public void attachApplicationSpecificRoutes(Router router) {
		// Add a route for the top-level resource
		router.attach("/", HomeResource.class).
		getTemplate().setMatchingMode(org.restlet.util.Template.MODE_EQUALS);
		router.attach("", HomeResource.class);
		
	}
	public void attachRoutes(Router router) {

		router.attach("/help", HomeResource.class);
		router.attach("/help/", HomeResource.class);
		router.attach("/help/{id}", HomeResource.class);

		router.attach("/{format}/help/", HomeResource.class);

		router.attach("/{format}/metadata/", ShardMetadataResource.class);
		router.attach("/pages/{page}.html", PageResource.class);
		router.attach("/usecases/{usecase}.html", PageResource.class);

		setupDirectories(router);

		// Add a route for node resources
		router.attach("/{format}/nodes/{id}", NodeResource.class);

		// Add routes for resources accessed via a node
		// should it be possible to append all statements URLs with /graph?
		router.attach("/{format}/nodes/{id}/statements", StatementsResource.class);
		router.attach("/{format}/nodes/{id}/statements/{aspect}", StatementsResource.class);
		// is this a politically correct way of doing a filter in REST?
		router.attach("/{format}/nodes/{id}/statements/{aspect}/{relation}", StatementsResource.class);
		// class-expression composite description
		// (here we use the term "description" in the description logic sense)
		router.attach("/{format}/nodes/{id}/description", DescriptionResource.class);

		router.attach("/{format}/nodes/{id}/blast", ScoredNodesResource.class);

		// Add a route for graph-by-node resources
		router.attach("/{format}/nodes/{id}/graph", GraphResource.class);
		router.attach("/{format}/nodes/{id}/graph/{aspect}", GraphResource.class);

		router.attach("/{format}/descriptions", DescriptionsResource.class);

		// Add a route for source node resources
		router.attach("/{format}/sources", SourcesResource.class);
		router.attach("/{format}/sources/{id}", SourceResource.class);
		router.attach("/{format}/sources/{id}/nodes", SourceResource.class);
		router.attach("/{format}/sources/{id}/statements", SourceResource.class);
		router.attach("/{format}/sources/{id}/graph", SourceResource.class);
		router.attach("/{format}/sources/{id}/annotations", SourceResource.class);

		// Add a route for node resources
		router.attach("/{format}/search/{operator}/{term}", NodesBySearchResource.class);
		router.attach("/{format}/search/{operator}/{term}/statements", StatementsBySearchResource.class);
		router.attach("/{format}/search/{operator}/{term}/statements/{aspect}", StatementsBySearchResource.class);

		router.attach("/{format}/hset/{id}", NestedAnnotationResource.class);
	}



	public Shard getShard() {
		return shard;
	}

	public void setShard(Shard shard) {
		this.shard = shard;
	}

}

