package org.obd.model.bridge;

import java.util.Arrays;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatistic;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.CountStatistic;
import org.obd.model.stats.ScoredNode;


public class OBDJSONBridge {


	
	public static JSONObject toJSON(Collection<Statement> coll) {
		JSONObject jsonObj = new JSONObject();
		try {
			JSONArray a = new JSONArray();
			for (Statement s : coll) {
				a.put(toJSON(s));
			}
			jsonObj.put("statements", a);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return jsonObj;
	}
	
	public static JSONObject nodesToJSON(Collection<Node> coll) {
		JSONObject jsonObj = new JSONObject();
		try {
			JSONArray a = new JSONArray();
			for (Node d : coll) {
				a.put(toJSON(d));
			}
			jsonObj.put("nodes", a);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return jsonObj;
	}
	
	public static JSONObject scoredNodesToJSON(Collection<ScoredNode> coll){
		JSONObject jsonObj = new JSONObject();
		try {
			JSONArray a = new JSONArray();
			for (ScoredNode d : coll) {
				a.put(toJSON(d));
			}
			jsonObj.put("nodes", a);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return jsonObj;
	}

	
	private static Object toJSON(ScoredNode node) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("id",  node.getNodeId());
			jsonObj.put("score", node.getScore());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObj;
	}

	public static JSONObject toJSON(Graph g) {
		JSONObject jsonObj = toJSON(Arrays.asList(g.getStatements()));
		JSONArray a = new JSONArray();
		for (Node n : g.getNodes()) {
			a.put(toJSON(n));		
		}
		try {
			jsonObj.put("nodes", a);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}
	public static JSONObject toJSON(Node node) {
		JSONObject jsonObj = new JSONObject();
		if (node instanceof LinkStatement) {
			LinkStatement link = (LinkStatement)node;
			includeStatementInfo(link, jsonObj);
			try {
				jsonObj.put("appliesToAllInstancesOf", new Boolean(link.isAppliesToAllInstancesOf()));

				} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if (node instanceof CompositionalDescription) {
			CompositionalDescription desc = (CompositionalDescription)node;
			
			try {
				jsonObj.put("predicate", desc.getPredicate().toString());
				jsonObj.put("node", desc.getNodeId());
				jsonObj.put("relation", desc.getRelationId());
				JSONArray a = new JSONArray();
				if (desc.getArguments() != null) {
					for (CompositionalDescription arg : desc.getArguments()) {
						if (arg.isAtomic())
							a.put(arg.getNodeId());
						else
							a.put(toJSON(arg));
					}
					jsonObj.put("arguments", a);
				}

				} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else {
			try {
				jsonObj.put("id",  node.getId());
				jsonObj.put("label", node.getLabel());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObj;
	}
	

	public static Object toJSON(AggregateStatisticCollection stats) {
		JSONObject o = new JSONObject();
		for (AggregateStatistic s : stats.getStats() ) {
			if (s instanceof CountStatistic) {
				CountStatistic c = (CountStatistic)s;
				try {
					o.put(s.getMeasuredEntity().toString(), 
							new Integer(c.getCount()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		return o;
	}


	
	public static void includeStatementInfo(Statement s, JSONObject jsonObj) {
		try {
			jsonObj.put("node", s.getNodeId());
			jsonObj.put("relation", s.getRelationId());
			jsonObj.put("target", s.getTargetId());
			jsonObj.put("source", s.getSourceId());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
