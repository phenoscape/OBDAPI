/**
 * Web Service layer, both REST and SOAP based access
 * <p>
 * Note: SOAP access (via Axis/2) is now deprecated.
 * <p>
 * The REST layer uses the RESTlet framework. 
 * {@link org.obd.ws.OBDRestApplication} defines the core application and sets up
 * the REST URL patterns. Each pattern maps to some subclass of {@link org.obd.ws.OBDResource}
 * <p>
 * <img src="doc-files/obd-architecture-overview.png"/>
 * <p>
 * Order of events:
 * <ul>
 *  <li>
 *   Server is initiated (via tomcat or neolios) - a {@link Shard} is created, pointing at some annotation source(s)
 *  </li>
 *  <li>
 *  </li>
 *  <li>browser or other client requests resource via HTTP. 
 *   For example <a href="http://spade.lbl.gov:8182/obdxml/nodes/MP:0002877/statements/annotations">/obdxml/nodes/MP:0002877/statements/annotations</a>
 *  
 *  </li>
 *  <li>Request is intercepted by server (e.g. Tomcat or Neolios) and passed to RESTLET Application: {@link org.obd.ws.OBDRestApplication}
 *  </li>
 *  <li>
 *    Pattern/route is checked against attached routes and handed off to appropriate RESTLET resource: e.g. {@link org.obd.ws.StatementsResource}
 *  </li>
 *  <li>
 *    Resource class makes appropriate calls to shard; e.g. {@link org.obd.query.Shard#getAnnotationStatementsForNode()}
 *  </li>
 *  <li>
 *   Shard implementation makes appropriate calls 
 *    ( annotation files via {@link org.obd.query.impl.OBOSessionShard} or a database via {@link org.obd.query.impl.OBDSQLShard} )
 *  </li>
 *  <li>
 *   Objects such as {@link org.obd.model.Graph}s, {@link org.obd.model.Node}s and {@link org.obd.model.Statement}s are returned
 *  </li>
 *  <li>
 *   Resource transforms objects to form for transmission across the wire. json/obo/owl/obdxml handled in {@link org.obd.model.bridge}.
 *    Otherwise the object is passed to a FreeMarker template
 *  </li>
 *  <li>
 *   Client receives payload and does something with it. E.g. render, parse
 *  </li>
 *  <li>
 *  </li>
 *  <li>
 *  </li>
 * </ul>
 */
package org.obd.ws;
import org.obd.query.*;
import org.obd.query.impl.*;
import org.obd.model.*;