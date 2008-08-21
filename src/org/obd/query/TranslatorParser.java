// $ANTLR 3.0.1 Translator.g 2008-07-07 15:00:38

	package org.obd.query;
	import org.obd.query.BooleanQueryTerm.BooleanOperator;


import org.antlr.runtime.*;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class TranslatorParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "OPEN", "CLOSE", "LINK_PARAM", "QUOTE", "BOOL_PARAM", "LINK_PREFIX", "BOOL_AND", "BOOL_OR", "PREDICATE_PREFIX", "OBJECT_PREFIX", "EQL", "LETTER", "NUMBER", "SPECIAL_CHARACTER", "ATOM_CHAR", "ATOM_TEXT"
    };
    public static final int EQL=14;
    public static final int LETTER=15;
    public static final int ATOM_CHAR=18;
    public static final int OBJECT_PREFIX=13;
    public static final int NUMBER=16;
    public static final int LINK_PARAM=6;
    public static final int BOOL_PARAM=8;
    public static final int SPECIAL_CHARACTER=17;
    public static final int QUOTE=7;
    public static final int BOOL_AND=10;
    public static final int BOOL_OR=11;
    public static final int PREDICATE_PREFIX=12;
    public static final int OPEN=4;
    public static final int EOF=-1;
    public static final int CLOSE=5;
    public static final int ATOM_TEXT=19;
    public static final int LINK_PREFIX=9;

        public TranslatorParser(TokenStream input) {
            super(input);
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "Translator.g"; }


    	List<String> atoms = new ArrayList<String>();


    public static class atom_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start atom
    // Translator.g:55:1: atom returns [String value] : QUOTE ATOM_TEXT QUOTE ;
    public final atom_return atom() throws RecognitionException {
        atom_return retval = new atom_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token QUOTE1=null;
        Token ATOM_TEXT2=null;
        Token QUOTE3=null;

        Object QUOTE1_tree=null;
        Object ATOM_TEXT2_tree=null;
        Object QUOTE3_tree=null;

        try {
            // Translator.g:55:29: ( QUOTE ATOM_TEXT QUOTE )
            // Translator.g:56:3: QUOTE ATOM_TEXT QUOTE
            {
            root_0 = (Object)adaptor.nil();

            QUOTE1=(Token)input.LT(1);
            match(input,QUOTE,FOLLOW_QUOTE_in_atom206); 
            QUOTE1_tree = (Object)adaptor.create(QUOTE1);
            adaptor.addChild(root_0, QUOTE1_tree);

            ATOM_TEXT2=(Token)input.LT(1);
            match(input,ATOM_TEXT,FOLLOW_ATOM_TEXT_in_atom208); 
            ATOM_TEXT2_tree = (Object)adaptor.create(ATOM_TEXT2);
            adaptor.addChild(root_0, ATOM_TEXT2_tree);

            QUOTE3=(Token)input.LT(1);
            match(input,QUOTE,FOLLOW_QUOTE_in_atom210); 
            QUOTE3_tree = (Object)adaptor.create(QUOTE3);
            adaptor.addChild(root_0, QUOTE3_tree);

             retval.value = ATOM_TEXT2.getText();

            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end atom

    public static class query_return extends ParserRuleReturnScope {
        public QueryTerm queryComponent;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start query
    // Translator.g:58:1: query returns [QueryTerm queryComponent] : ( booleanQuery | linkQuery );
    public final query_return query() throws RecognitionException {
        query_return retval = new query_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        booleanQuery_return booleanQuery4 = null;

        linkQuery_return linkQuery5 = null;



        try {
            // Translator.g:58:42: ( booleanQuery | linkQuery )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=BOOL_AND && LA1_0<=BOOL_OR)) ) {
                alt1=1;
            }
            else if ( (LA1_0==LINK_PREFIX) ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("58:1: query returns [QueryTerm queryComponent] : ( booleanQuery | linkQuery );", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // Translator.g:59:3: booleanQuery
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanQuery_in_query228);
                    booleanQuery4=booleanQuery();
                    _fsp--;

                    adaptor.addChild(root_0, booleanQuery4.getTree());
                    retval.queryComponent = booleanQuery4.booleanQueryTerm;

                    }
                    break;
                case 2 :
                    // Translator.g:59:70: linkQuery
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_linkQuery_in_query234);
                    linkQuery5=linkQuery();
                    _fsp--;

                    adaptor.addChild(root_0, linkQuery5.getTree());
                    retval.queryComponent = linkQuery5.linkQueryTerm;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end query

    public static class boolOp_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start boolOp
    // Translator.g:61:1: boolOp : ( BOOL_AND | BOOL_OR );
    public final boolOp_return boolOp() throws RecognitionException {
        boolOp_return retval = new boolOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set6=null;

        try {
            // Translator.g:61:9: ( BOOL_AND | BOOL_OR )
            // Translator.g:
            {
            root_0 = (Object)adaptor.nil();

            set6=(Token)input.LT(1);
            if ( (input.LA(1)>=BOOL_AND && input.LA(1)<=BOOL_OR) ) {
                input.consume();
                adaptor.addChild(root_0, adaptor.create(set6));
                errorRecovery=false;
            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_boolOp0);    throw mse;
            }


            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end boolOp

    public static class predicate_return extends ParserRuleReturnScope {
        public Object target;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start predicate
    // Translator.g:64:1: predicate returns [Object target] : LINK_PARAM PREDICATE_PREFIX EQL ( atom | query ) ;
    public final predicate_return predicate() throws RecognitionException {
        predicate_return retval = new predicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LINK_PARAM7=null;
        Token PREDICATE_PREFIX8=null;
        Token EQL9=null;
        atom_return atom10 = null;

        query_return query11 = null;


        Object LINK_PARAM7_tree=null;
        Object PREDICATE_PREFIX8_tree=null;
        Object EQL9_tree=null;

        try {
            // Translator.g:64:35: ( LINK_PARAM PREDICATE_PREFIX EQL ( atom | query ) )
            // Translator.g:65:3: LINK_PARAM PREDICATE_PREFIX EQL ( atom | query )
            {
            root_0 = (Object)adaptor.nil();

            LINK_PARAM7=(Token)input.LT(1);
            match(input,LINK_PARAM,FOLLOW_LINK_PARAM_in_predicate267); 
            LINK_PARAM7_tree = (Object)adaptor.create(LINK_PARAM7);
            adaptor.addChild(root_0, LINK_PARAM7_tree);

            PREDICATE_PREFIX8=(Token)input.LT(1);
            match(input,PREDICATE_PREFIX,FOLLOW_PREDICATE_PREFIX_in_predicate269); 
            PREDICATE_PREFIX8_tree = (Object)adaptor.create(PREDICATE_PREFIX8);
            adaptor.addChild(root_0, PREDICATE_PREFIX8_tree);

            EQL9=(Token)input.LT(1);
            match(input,EQL,FOLLOW_EQL_in_predicate271); 
            EQL9_tree = (Object)adaptor.create(EQL9);
            adaptor.addChild(root_0, EQL9_tree);

            // Translator.g:65:35: ( atom | query )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==QUOTE) ) {
                alt2=1;
            }
            else if ( ((LA2_0>=LINK_PREFIX && LA2_0<=BOOL_OR)) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("65:35: ( atom | query )", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // Translator.g:65:36: atom
                    {
                    pushFollow(FOLLOW_atom_in_predicate274);
                    atom10=atom();
                    _fsp--;

                    adaptor.addChild(root_0, atom10.getTree());
                    retval.target =atom10.value;

                    }
                    break;
                case 2 :
                    // Translator.g:65:65: query
                    {
                    pushFollow(FOLLOW_query_in_predicate279);
                    query11=query();
                    _fsp--;

                    adaptor.addChild(root_0, query11.getTree());
                    retval.target =query11.queryComponent;

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end predicate

    public static class object_return extends ParserRuleReturnScope {
        public Object target;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start object
    // Translator.g:67:1: object returns [Object target] : LINK_PARAM OBJECT_PREFIX EQL ( atom | query ) ;
    public final object_return object() throws RecognitionException {
        object_return retval = new object_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LINK_PARAM12=null;
        Token OBJECT_PREFIX13=null;
        Token EQL14=null;
        atom_return atom15 = null;

        query_return query16 = null;


        Object LINK_PARAM12_tree=null;
        Object OBJECT_PREFIX13_tree=null;
        Object EQL14_tree=null;

        try {
            // Translator.g:67:34: ( LINK_PARAM OBJECT_PREFIX EQL ( atom | query ) )
            // Translator.g:68:3: LINK_PARAM OBJECT_PREFIX EQL ( atom | query )
            {
            root_0 = (Object)adaptor.nil();

            LINK_PARAM12=(Token)input.LT(1);
            match(input,LINK_PARAM,FOLLOW_LINK_PARAM_in_object299); 
            LINK_PARAM12_tree = (Object)adaptor.create(LINK_PARAM12);
            adaptor.addChild(root_0, LINK_PARAM12_tree);

            OBJECT_PREFIX13=(Token)input.LT(1);
            match(input,OBJECT_PREFIX,FOLLOW_OBJECT_PREFIX_in_object301); 
            OBJECT_PREFIX13_tree = (Object)adaptor.create(OBJECT_PREFIX13);
            adaptor.addChild(root_0, OBJECT_PREFIX13_tree);

            EQL14=(Token)input.LT(1);
            match(input,EQL,FOLLOW_EQL_in_object303); 
            EQL14_tree = (Object)adaptor.create(EQL14);
            adaptor.addChild(root_0, EQL14_tree);

            // Translator.g:68:32: ( atom | query )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==QUOTE) ) {
                alt3=1;
            }
            else if ( ((LA3_0>=LINK_PREFIX && LA3_0<=BOOL_OR)) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("68:32: ( atom | query )", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // Translator.g:68:33: atom
                    {
                    pushFollow(FOLLOW_atom_in_object306);
                    atom15=atom();
                    _fsp--;

                    adaptor.addChild(root_0, atom15.getTree());
                    retval.target =atom15.value;

                    }
                    break;
                case 2 :
                    // Translator.g:68:62: query
                    {
                    pushFollow(FOLLOW_query_in_object311);
                    query16=query();
                    _fsp--;

                    adaptor.addChild(root_0, query16.getTree());
                    retval.target =query16.queryComponent;

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end object

    public static class linkQuery_return extends ParserRuleReturnScope {
        public LinkQueryTerm linkQueryTerm;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start linkQuery
    // Translator.g:70:1: linkQuery returns [LinkQueryTerm linkQueryTerm] : LINK_PREFIX OPEN ( predicate )? ( object )? CLOSE ;
    public final linkQuery_return linkQuery() throws RecognitionException {
        linkQuery_return retval = new linkQuery_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LINK_PREFIX17=null;
        Token OPEN18=null;
        Token CLOSE21=null;
        predicate_return predicate19 = null;

        object_return object20 = null;


        Object LINK_PREFIX17_tree=null;
        Object OPEN18_tree=null;
        Object CLOSE21_tree=null;


        			LinkQueryTerm lqt = new LinkQueryTerm();
        		
        try {
            // Translator.g:74:3: ( LINK_PREFIX OPEN ( predicate )? ( object )? CLOSE )
            // Translator.g:75:3: LINK_PREFIX OPEN ( predicate )? ( object )? CLOSE
            {
            root_0 = (Object)adaptor.nil();

            LINK_PREFIX17=(Token)input.LT(1);
            match(input,LINK_PREFIX,FOLLOW_LINK_PREFIX_in_linkQuery337); 
            LINK_PREFIX17_tree = (Object)adaptor.create(LINK_PREFIX17);
            adaptor.addChild(root_0, LINK_PREFIX17_tree);

            OPEN18=(Token)input.LT(1);
            match(input,OPEN,FOLLOW_OPEN_in_linkQuery339); 
            OPEN18_tree = (Object)adaptor.create(OPEN18);
            adaptor.addChild(root_0, OPEN18_tree);

            // Translator.g:75:21: ( predicate )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==LINK_PARAM) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==PREDICATE_PREFIX) ) {
                    alt4=1;
                }
            }
            switch (alt4) {
                case 1 :
                    // Translator.g:75:21: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_linkQuery342);
                    predicate19=predicate();
                    _fsp--;

                    adaptor.addChild(root_0, predicate19.getTree());

                    }
                    break;

            }

            // Translator.g:75:32: ( object )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==LINK_PARAM) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // Translator.g:75:32: object
                    {
                    pushFollow(FOLLOW_object_in_linkQuery345);
                    object20=object();
                    _fsp--;

                    adaptor.addChild(root_0, object20.getTree());

                    }
                    break;

            }

            CLOSE21=(Token)input.LT(1);
            match(input,CLOSE,FOLLOW_CLOSE_in_linkQuery348); 
            CLOSE21_tree = (Object)adaptor.create(CLOSE21);
            adaptor.addChild(root_0, CLOSE21_tree);


            		
            			if ((predicate19 != null) && (predicate19.target != null)){
            				Object predicate = predicate19.target;
            				if (predicate instanceof String){
            					lqt.setRelation((String)predicate);
            				} else {
            					lqt.setRelation((QueryTerm)predicate);
            				}
            			}
            			
            			if ((object20 != null) && (object20.target != null)){
            				Object target = object20.target;
            				if (target instanceof String){
            					lqt.setTarget((String)target);
            				} else {
            					lqt.setTarget((QueryTerm)target);
            				}
            			}
            			retval.linkQueryTerm = lqt;

            		

            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end linkQuery

    public static class booleanElement_return extends ParserRuleReturnScope {
        public QueryTerm booleanQueryElement;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start booleanElement
    // Translator.g:98:1: booleanElement returns [QueryTerm booleanQueryElement] : BOOL_PARAM query ;
    public final booleanElement_return booleanElement() throws RecognitionException {
        booleanElement_return retval = new booleanElement_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token BOOL_PARAM22=null;
        query_return query23 = null;


        Object BOOL_PARAM22_tree=null;

        try {
            // Translator.g:98:56: ( BOOL_PARAM query )
            // Translator.g:99:3: BOOL_PARAM query
            {
            root_0 = (Object)adaptor.nil();

            BOOL_PARAM22=(Token)input.LT(1);
            match(input,BOOL_PARAM,FOLLOW_BOOL_PARAM_in_booleanElement365); 
            BOOL_PARAM22_tree = (Object)adaptor.create(BOOL_PARAM22);
            adaptor.addChild(root_0, BOOL_PARAM22_tree);

            pushFollow(FOLLOW_query_in_booleanElement367);
            query23=query();
            _fsp--;

            adaptor.addChild(root_0, query23.getTree());
             retval.booleanQueryElement = (QueryTerm)query23.queryComponent;

            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end booleanElement

    public static class booleanQuery_return extends ParserRuleReturnScope {
        public QueryTerm booleanQueryTerm;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start booleanQuery
    // Translator.g:101:1: booleanQuery returns [QueryTerm booleanQueryTerm] : boolOp OPEN ( booleanElement )+ CLOSE ;
    public final booleanQuery_return booleanQuery() throws RecognitionException {
        booleanQuery_return retval = new booleanQuery_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OPEN25=null;
        Token CLOSE27=null;
        boolOp_return boolOp24 = null;

        booleanElement_return booleanElement26 = null;


        Object OPEN25_tree=null;
        Object CLOSE27_tree=null;


        	   		List	<QueryTerm> queryElements = new ArrayList<QueryTerm>();
        	   		BooleanQueryTerm bqt = new BooleanQueryTerm();
           		
        try {
            // Translator.g:106:3: ( boolOp OPEN ( booleanElement )+ CLOSE )
            // Translator.g:107:3: boolOp OPEN ( booleanElement )+ CLOSE
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_boolOp_in_booleanQuery395);
            boolOp24=boolOp();
            _fsp--;

            adaptor.addChild(root_0, boolOp24.getTree());
            OPEN25=(Token)input.LT(1);
            match(input,OPEN,FOLLOW_OPEN_in_booleanQuery397); 
            OPEN25_tree = (Object)adaptor.create(OPEN25);
            adaptor.addChild(root_0, OPEN25_tree);

            // Translator.g:107:15: ( booleanElement )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==BOOL_PARAM) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // Translator.g:107:16: booleanElement
            	    {
            	    pushFollow(FOLLOW_booleanElement_in_booleanQuery400);
            	    booleanElement26=booleanElement();
            	    _fsp--;

            	    adaptor.addChild(root_0, booleanElement26.getTree());
            	     queryElements.add(booleanElement26.booleanQueryElement); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

            CLOSE27=(Token)input.LT(1);
            match(input,CLOSE,FOLLOW_CLOSE_in_booleanQuery406); 
            CLOSE27_tree = (Object)adaptor.create(CLOSE27);
            adaptor.addChild(root_0, CLOSE27_tree);


            			for (QueryTerm qt : queryElements) {
            				bqt.addQueryTerm(qt);
            			}
            			if (input.toString(boolOp24.start,boolOp24.stop).equals("and")){
            				bqt.setOperator(BooleanOperator.AND);
            			} else if (input.toString(boolOp24.start,boolOp24.stop).equals("or")){
            				bqt.setOperator(BooleanOperator.OR);			
            			} else {
            				System.err.println("Unknown Boolean Operation");
            				System.exit(-1);
            			}
            			retval.booleanQueryTerm = bqt;
            		

            }

            retval.stop = input.LT(-1);

                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end booleanQuery


 

    public static final BitSet FOLLOW_QUOTE_in_atom206 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_ATOM_TEXT_in_atom208 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_QUOTE_in_atom210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanQuery_in_query228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_linkQuery_in_query234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_boolOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LINK_PARAM_in_predicate267 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_PREDICATE_PREFIX_in_predicate269 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_EQL_in_predicate271 = new BitSet(new long[]{0x0000000000000E80L});
    public static final BitSet FOLLOW_atom_in_predicate274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_in_predicate279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LINK_PARAM_in_object299 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_OBJECT_PREFIX_in_object301 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_EQL_in_object303 = new BitSet(new long[]{0x0000000000000E80L});
    public static final BitSet FOLLOW_atom_in_object306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_in_object311 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LINK_PREFIX_in_linkQuery337 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_OPEN_in_linkQuery339 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_predicate_in_linkQuery342 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_object_in_linkQuery345 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_CLOSE_in_linkQuery348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_PARAM_in_booleanElement365 = new BitSet(new long[]{0x0000000000000E00L});
    public static final BitSet FOLLOW_query_in_booleanElement367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolOp_in_booleanQuery395 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_OPEN_in_booleanQuery397 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_booleanElement_in_booleanQuery400 = new BitSet(new long[]{0x0000000000000120L});
    public static final BitSet FOLLOW_CLOSE_in_booleanQuery406 = new BitSet(new long[]{0x0000000000000002L});

}