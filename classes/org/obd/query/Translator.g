grammar Translator;

options {
	output=AST;
}

@header {
	package org.obd.query;
	import org.obd.query.BooleanQueryTerm.BooleanOperator;
}

@members{
	List<String> atoms = new ArrayList<String>();
}





OPEN 	:	 '[';
CLOSE 	:	 ']';
LINK_PARAM 
	:	 '?';
QUOTE 	:	 '"';
BOOL_PARAM 
	:	 '+';
LINK_PREFIX
	:	'link';
BOOL_AND:	'and';
BOOL_OR	:	'or';
PREDICATE_PREFIX
	:	'predicate';
OBJECT_PREFIX
	:	'object';
EQL	:	 '=';



ATOM_CHAR
	:	LETTER|NUMBER|SPECIAL_CHARACTER;

ATOM_TEXT
	:	ATOM_CHAR+;
fragment LETTER
	:	'a'..'z'|'A'..'Z';

fragment NUMBER
	:	'0'..'9';
	
fragment SPECIAL_CHARACTER
	:	':'|'_';



atom		returns [String value]:	
		QUOTE ATOM_TEXT QUOTE { $value = $ATOM_TEXT.text;};

query		returns [QueryTerm queryComponent]:  
		booleanQuery {$queryComponent = $booleanQuery.booleanQueryTerm;} | linkQuery {$queryComponent = $linkQuery.linkQueryTerm;};

boolOp		:	
		BOOL_AND | BOOL_OR;

predicate	returns [Object target]	:	
		LINK_PARAM PREDICATE_PREFIX EQL (atom {$target=$atom.value;}| query {$target=$query.queryComponent;});

object 		returns [Object target]	:	
		LINK_PARAM OBJECT_PREFIX EQL (atom {$target=$atom.value;}| query {$target=$query.queryComponent;});

linkQuery	returns [LinkQueryTerm linkQueryTerm]
		@init {
			LinkQueryTerm lqt = new LinkQueryTerm();
		}
		:
		LINK_PREFIX OPEN  predicate? object? CLOSE {
		
			if ($predicate.target != null){
				Object predicate = $predicate.target;
				if (predicate instanceof String){
					lqt.setRelation((String)predicate);
				} else {
					lqt.setRelation((QueryTerm)predicate);
				}
			}
			
			if ($object.target != null){
				Object target = $object.target;
				if (target instanceof String){
					lqt.setTarget((String)target);
				} else {
					lqt.setTarget((QueryTerm)target);
				}
			}
			$linkQueryTerm = lqt;

		};

booleanElement	returns [QueryTerm booleanQueryElement] :	
		BOOL_PARAM query { $booleanQueryElement= (QueryTerm)$query.queryComponent;};
	
booleanQuery 	returns [QueryTerm booleanQueryTerm]
		@init {
	   		List	<QueryTerm> queryElements = new ArrayList<QueryTerm>();
	   		BooleanQueryTerm bqt = new BooleanQueryTerm();
   		}
		:	
		boolOp OPEN (booleanElement { queryElements.add($booleanElement.booleanQueryElement); })+ CLOSE {
			for (QueryTerm qt : queryElements) {
				bqt.addQueryTerm(qt);
			}
			if ($boolOp.text.equals("and")){
				bqt.setOperator(BooleanOperator.AND);
			} else if ($boolOp.text.equals("or")){
				bqt.setOperator(BooleanOperator.OR);			
			} else {
				System.err.println("Unknown Boolean Operation");
				System.exit(-1);
			}
			$booleanQueryTerm = bqt;
		};
	
	
	
