/**
 * Core OBD API and Query Objects
 * <p>
 * The core of the java API is a {@link org.obd.query.Shard}. This provides methods
 * for accessing an OBD Shard (i.e. repository/instance). There can be multiple
 * implementations of a Shard. The two primary implementations are:
 * <ul>
 *  <li> {@link org.obd.query.impl.OBDSQLShard} </li> -- database access
 *  <li> {@link org.obd.query.impl.OBOSessionShard} </li> -- in-memory access
 *  <li> {@link org.obd.query.impl.MultiShard} </li> -- mediator access to >1 shard
 * </ul>
 * <p>
 * The shard object access provides means of accessing a shard through either
 * simple convenience methods, or via queries. Queries are constructed using
 * {@link org.obd.query.QueryTerm} objects
 * <p>
 * The following diagram gives an overview of how access to different kinds of repositories
 * is implemented. The diagram omits certain Abstract classes for brevity. Note that also
 * the access method for OWL may change to a direct OWLAPI wrapper
 * <p>
 * <img src="doc-files/obd-shard-uml.png">
 * <p>
 * The OBD API is generic and free of domain-specific knowledge or constructs. However, it is obviously geared towards annotation-centric use cases.
<p>
<code>
  getAnnotationStatementsForNode(ClassId)
  </code>
<p>
This returns all statements of the form R(E,X) where R’(X,ClassId) holds. More advanced query calls are possible for situations where the API user needs more control over relations, filtering, etc.
<p>
As well as canned API calls, the API user can create and manipulate query objects, and pass these to the shard/repository. These query objects are highly expressive and allow API user to ask for things such as:
  Annotations from source X with evidence E that are to size phenotypes affecting the heart or kidneys, or parts of these organs
(note that of course neither the query model nor object model are cognizant of evidence, phenotypes and so on: this is expressed using classes and relations from ontologies)
<p>
As well as a java API, there is also a REST level API, developed using the RESTLET framework.
<p>
Another option is to access OBD via a SPARQL endpoint. The d2rq tool is used to map to RDF directly from the OBDSQL schema (see below).
<p>
<h2>Implementations</h2>
The OBD API can be implemented over a number of difference persistence layers. The current main implementation is an OBDSQLShard, which wraps an OBDSQL Schema database. This database schema is generic and closely corresponds to the OBD Model. It is geared towards both genericity and efficiency. Relations (in the relational model sense) can be materialized for speed.
<p>
There is also an OBOSession layer, which wraps org.obo objects – this is the same object model used by Phenote, and was originally developed for oboedit, and then extended to handle annotations. There is a bridge between this model and the Manchester OWLAPI (which will probably form the core of Protege4). This is how OBD handles import and export of annotations to OWL (required, for example, by the BIRN group).
<p>
Note that a single OBD shard/repository can wrap multiple persistence sources in a transparent fashion; the mediation strategy is still quite simple, so some query answers are incomplete when using this multishard strategy)
<p>
The persistence strategy has been designed such that it is simple to wrap existing bio-database schemas with the API. In addition, we anticipate wrapping RDF triplestores – the OBDAPI and query objects give a higher lever way to access annotations, SPARQL is too low level for this.
<p>
<h2>Deductive query answering</h2>
A critical requirement of OBD is the ability to answer queries such as: what things are annotated with the class “lymphocyte”, where lymphocyte is a class in an ontology. This means that query answering must rely on deductive strategies (simple example: annotations to subtypes of lymphocyte, such as T cell, must also be included).
More complex deductions are required in the cases of annotations to post-coordinated multi-ontology classes, especially where there is a mix of pre-coordinated classes with class definitions and post-coordinated class expressions.
<p>
Standard reasoners can be used to pre-compute the deductive closure. Currently OBD uses its own simple forward chaining reasoner and this scales better with the kind of low expressivity (sub EL++) high volume annotations in OBD.
<p>

 */

package org.obd.query;