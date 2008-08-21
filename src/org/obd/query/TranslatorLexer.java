// $ANTLR 3.0.1 Translator.g 2008-07-07 15:00:38

package org.obd.query;

import org.antlr.runtime.*;


public class TranslatorLexer extends Lexer {
    public static final int LETTER=15;
    public static final int EQL=14;
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
    public static final int Tokens=20;
    public static final int CLOSE=5;
    public static final int ATOM_TEXT=19;
    public static final int LINK_PREFIX=9;
    public TranslatorLexer() {;} 
    public TranslatorLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "Translator.g"; }

    // $ANTLR start OPEN
    public final void mOPEN() throws RecognitionException {
        try {
            int _type = OPEN;
            // Translator.g:20:7: ( '[' )
            // Translator.g:20:10: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OPEN

    // $ANTLR start CLOSE
    public final void mCLOSE() throws RecognitionException {
        try {
            int _type = CLOSE;
            // Translator.g:21:8: ( ']' )
            // Translator.g:21:11: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CLOSE

    // $ANTLR start LINK_PARAM
    public final void mLINK_PARAM() throws RecognitionException {
        try {
            int _type = LINK_PARAM;
            // Translator.g:23:2: ( '?' )
            // Translator.g:23:5: '?'
            {
            match('?'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LINK_PARAM

    // $ANTLR start QUOTE
    public final void mQUOTE() throws RecognitionException {
        try {
            int _type = QUOTE;
            // Translator.g:24:8: ( '\"' )
            // Translator.g:24:11: '\"'
            {
            match('\"'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end QUOTE

    // $ANTLR start BOOL_PARAM
    public final void mBOOL_PARAM() throws RecognitionException {
        try {
            int _type = BOOL_PARAM;
            // Translator.g:26:2: ( '+' )
            // Translator.g:26:5: '+'
            {
            match('+'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BOOL_PARAM

    // $ANTLR start LINK_PREFIX
    public final void mLINK_PREFIX() throws RecognitionException {
        try {
            int _type = LINK_PREFIX;
            // Translator.g:28:2: ( 'link' )
            // Translator.g:28:4: 'link'
            {
            match("link"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LINK_PREFIX

    // $ANTLR start BOOL_AND
    public final void mBOOL_AND() throws RecognitionException {
        try {
            int _type = BOOL_AND;
            // Translator.g:29:9: ( 'and' )
            // Translator.g:29:11: 'and'
            {
            match("and"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BOOL_AND

    // $ANTLR start BOOL_OR
    public final void mBOOL_OR() throws RecognitionException {
        try {
            int _type = BOOL_OR;
            // Translator.g:30:9: ( 'or' )
            // Translator.g:30:11: 'or'
            {
            match("or"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BOOL_OR

    // $ANTLR start PREDICATE_PREFIX
    public final void mPREDICATE_PREFIX() throws RecognitionException {
        try {
            int _type = PREDICATE_PREFIX;
            // Translator.g:32:2: ( 'predicate' )
            // Translator.g:32:4: 'predicate'
            {
            match("predicate"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PREDICATE_PREFIX

    // $ANTLR start OBJECT_PREFIX
    public final void mOBJECT_PREFIX() throws RecognitionException {
        try {
            int _type = OBJECT_PREFIX;
            // Translator.g:34:2: ( 'object' )
            // Translator.g:34:4: 'object'
            {
            match("object"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OBJECT_PREFIX

    // $ANTLR start EQL
    public final void mEQL() throws RecognitionException {
        try {
            int _type = EQL;
            // Translator.g:35:5: ( '=' )
            // Translator.g:35:8: '='
            {
            match('='); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EQL

    // $ANTLR start ATOM_CHAR
    public final void mATOM_CHAR() throws RecognitionException {
        try {
            int _type = ATOM_CHAR;
            // Translator.g:40:2: ( LETTER | NUMBER | SPECIAL_CHARACTER )
            // Translator.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<=':')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ATOM_CHAR

    // $ANTLR start ATOM_TEXT
    public final void mATOM_TEXT() throws RecognitionException {
        try {
            int _type = ATOM_TEXT;
            // Translator.g:43:2: ( ( ATOM_CHAR )+ )
            // Translator.g:43:4: ( ATOM_CHAR )+
            {
            // Translator.g:43:4: ( ATOM_CHAR )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<=':')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // Translator.g:43:4: ATOM_CHAR
            	    {
            	    mATOM_CHAR(); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ATOM_TEXT

    // $ANTLR start LETTER
    public final void mLETTER() throws RecognitionException {
        try {
            // Translator.g:45:2: ( 'a' .. 'z' | 'A' .. 'Z' )
            // Translator.g:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end LETTER

    // $ANTLR start NUMBER
    public final void mNUMBER() throws RecognitionException {
        try {
            // Translator.g:48:2: ( '0' .. '9' )
            // Translator.g:48:4: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end NUMBER

    // $ANTLR start SPECIAL_CHARACTER
    public final void mSPECIAL_CHARACTER() throws RecognitionException {
        try {
            // Translator.g:51:2: ( ':' | '_' )
            // Translator.g:
            {
            if ( input.LA(1)==':'||input.LA(1)=='_' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end SPECIAL_CHARACTER

    public void mTokens() throws RecognitionException {
        // Translator.g:1:8: ( OPEN | CLOSE | LINK_PARAM | QUOTE | BOOL_PARAM | LINK_PREFIX | BOOL_AND | BOOL_OR | PREDICATE_PREFIX | OBJECT_PREFIX | EQL | ATOM_CHAR | ATOM_TEXT )
        int alt2=13;
        switch ( input.LA(1) ) {
        case '[':
            {
            alt2=1;
            }
            break;
        case ']':
            {
            alt2=2;
            }
            break;
        case '?':
            {
            alt2=3;
            }
            break;
        case '\"':
            {
            alt2=4;
            }
            break;
        case '+':
            {
            alt2=5;
            }
            break;
        case 'l':
            {
            switch ( input.LA(2) ) {
            case 'i':
                {
                int LA2_12 = input.LA(3);

                if ( (LA2_12=='n') ) {
                    int LA2_19 = input.LA(4);

                    if ( (LA2_19=='k') ) {
                        int LA2_24 = input.LA(5);

                        if ( ((LA2_24>='0' && LA2_24<=':')||(LA2_24>='A' && LA2_24<='Z')||LA2_24=='_'||(LA2_24>='a' && LA2_24<='z')) ) {
                            alt2=13;
                        }
                        else {
                            alt2=6;}
                    }
                    else {
                        alt2=13;}
                }
                else {
                    alt2=13;}
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt2=13;
                }
                break;
            default:
                alt2=12;}

            }
            break;
        case 'a':
            {
            switch ( input.LA(2) ) {
            case 'n':
                {
                int LA2_15 = input.LA(3);

                if ( (LA2_15=='d') ) {
                    int LA2_20 = input.LA(4);

                    if ( ((LA2_20>='0' && LA2_20<=':')||(LA2_20>='A' && LA2_20<='Z')||LA2_20=='_'||(LA2_20>='a' && LA2_20<='z')) ) {
                        alt2=13;
                    }
                    else {
                        alt2=7;}
                }
                else {
                    alt2=13;}
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt2=13;
                }
                break;
            default:
                alt2=12;}

            }
            break;
        case 'o':
            {
            switch ( input.LA(2) ) {
            case 'b':
                {
                int LA2_16 = input.LA(3);

                if ( (LA2_16=='j') ) {
                    int LA2_21 = input.LA(4);

                    if ( (LA2_21=='e') ) {
                        int LA2_26 = input.LA(5);

                        if ( (LA2_26=='c') ) {
                            int LA2_29 = input.LA(6);

                            if ( (LA2_29=='t') ) {
                                int LA2_31 = input.LA(7);

                                if ( ((LA2_31>='0' && LA2_31<=':')||(LA2_31>='A' && LA2_31<='Z')||LA2_31=='_'||(LA2_31>='a' && LA2_31<='z')) ) {
                                    alt2=13;
                                }
                                else {
                                    alt2=10;}
                            }
                            else {
                                alt2=13;}
                        }
                        else {
                            alt2=13;}
                    }
                    else {
                        alt2=13;}
                }
                else {
                    alt2=13;}
                }
                break;
            case 'r':
                {
                int LA2_17 = input.LA(3);

                if ( ((LA2_17>='0' && LA2_17<=':')||(LA2_17>='A' && LA2_17<='Z')||LA2_17=='_'||(LA2_17>='a' && LA2_17<='z')) ) {
                    alt2=13;
                }
                else {
                    alt2=8;}
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
            case 'a':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt2=13;
                }
                break;
            default:
                alt2=12;}

            }
            break;
        case 'p':
            {
            switch ( input.LA(2) ) {
            case 'r':
                {
                int LA2_18 = input.LA(3);

                if ( (LA2_18=='e') ) {
                    int LA2_23 = input.LA(4);

                    if ( (LA2_23=='d') ) {
                        int LA2_27 = input.LA(5);

                        if ( (LA2_27=='i') ) {
                            int LA2_30 = input.LA(6);

                            if ( (LA2_30=='c') ) {
                                int LA2_32 = input.LA(7);

                                if ( (LA2_32=='a') ) {
                                    int LA2_34 = input.LA(8);

                                    if ( (LA2_34=='t') ) {
                                        int LA2_35 = input.LA(9);

                                        if ( (LA2_35=='e') ) {
                                            int LA2_36 = input.LA(10);

                                            if ( ((LA2_36>='0' && LA2_36<=':')||(LA2_36>='A' && LA2_36<='Z')||LA2_36=='_'||(LA2_36>='a' && LA2_36<='z')) ) {
                                                alt2=13;
                                            }
                                            else {
                                                alt2=9;}
                                        }
                                        else {
                                            alt2=13;}
                                    }
                                    else {
                                        alt2=13;}
                                }
                                else {
                                    alt2=13;}
                            }
                            else {
                                alt2=13;}
                        }
                        else {
                            alt2=13;}
                    }
                    else {
                        alt2=13;}
                }
                else {
                    alt2=13;}
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt2=13;
                }
                break;
            default:
                alt2=12;}

            }
            break;
        case '=':
            {
            alt2=11;
            }
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case ':':
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '_':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'm':
        case 'n':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            int LA2_11 = input.LA(2);

            if ( ((LA2_11>='0' && LA2_11<=':')||(LA2_11>='A' && LA2_11<='Z')||LA2_11=='_'||(LA2_11>='a' && LA2_11<='z')) ) {
                alt2=13;
            }
            else {
                alt2=12;}
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( OPEN | CLOSE | LINK_PARAM | QUOTE | BOOL_PARAM | LINK_PREFIX | BOOL_AND | BOOL_OR | PREDICATE_PREFIX | OBJECT_PREFIX | EQL | ATOM_CHAR | ATOM_TEXT );", 2, 0, input);

            throw nvae;
        }

        switch (alt2) {
            case 1 :
                // Translator.g:1:10: OPEN
                {
                mOPEN(); 

                }
                break;
            case 2 :
                // Translator.g:1:15: CLOSE
                {
                mCLOSE(); 

                }
                break;
            case 3 :
                // Translator.g:1:21: LINK_PARAM
                {
                mLINK_PARAM(); 

                }
                break;
            case 4 :
                // Translator.g:1:32: QUOTE
                {
                mQUOTE(); 

                }
                break;
            case 5 :
                // Translator.g:1:38: BOOL_PARAM
                {
                mBOOL_PARAM(); 

                }
                break;
            case 6 :
                // Translator.g:1:49: LINK_PREFIX
                {
                mLINK_PREFIX(); 

                }
                break;
            case 7 :
                // Translator.g:1:61: BOOL_AND
                {
                mBOOL_AND(); 

                }
                break;
            case 8 :
                // Translator.g:1:70: BOOL_OR
                {
                mBOOL_OR(); 

                }
                break;
            case 9 :
                // Translator.g:1:78: PREDICATE_PREFIX
                {
                mPREDICATE_PREFIX(); 

                }
                break;
            case 10 :
                // Translator.g:1:95: OBJECT_PREFIX
                {
                mOBJECT_PREFIX(); 

                }
                break;
            case 11 :
                // Translator.g:1:109: EQL
                {
                mEQL(); 

                }
                break;
            case 12 :
                // Translator.g:1:113: ATOM_CHAR
                {
                mATOM_CHAR(); 

                }
                break;
            case 13 :
                // Translator.g:1:123: ATOM_TEXT
                {
                mATOM_TEXT(); 

                }
                break;

        }

    }


 

}