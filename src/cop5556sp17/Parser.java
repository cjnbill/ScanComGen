package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;
import java.util.ArrayList;


public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		Program p=program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {
		//expression::=term(relOp term)*
        Token first=t;
		Expression e0;
		Expression e1;
		e0=term();
		while(t.isKind(LT)||t.isKind(LE)||t.isKind(GT)||t.isKind(GE)||t.isKind(EQUAL)||t.isKind(NOTEQUAL)){
			Token op=t;
			consume();
			e1=term();
			e0 = new BinaryExpression(first,e0,op,e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		//term::= elem ( weakOp elem)*
        Token first=t;
		Expression e0;
		Expression e1;
		e0=elem();
		while(t.isKind(PLUS)||t.isKind(MINUS)||t.isKind(OR)){
			Token op=t;
			consume();
			e1 = elem();
			e0=new BinaryExpression(first,e0,op,e1);
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		//elem::= factor ( strongOp factor)*
        Token first=t;
        Expression e0;
        Expression e1;
		e0=factor();
		while(t.isKind(TIMES)||t.isKind(DIV)||t.isKind(AND)||t.isKind(MOD)){
		    Token op=t;
			consume();
			e1 = factor();
			e0=new BinaryExpression(first,e0,op,e1);
		}
		return e0;
	}

	Expression factor() throws SyntaxException {
	    Expression e;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
		    e = new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: {
		    e=new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
		    e=new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
		    e=new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e= expression();
			match(RPAREN);
		}
			break;
		default:
			throw new SyntaxException("illegal factor");
		}
		return e;
	}

	Block block() throws SyntaxException {
		//block::=  {  ( dec | statement) *  }
        ArrayList<Dec> decs=new ArrayList<>();
        ArrayList<Statement> statements=new ArrayList<>();
        Token first=t;
		if (t.isKind(LBRACE)) {
			consume();
			while(true){
			    //dec
				if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)){
				    decs.add(dec());
				}
				//end
				else if(t.isKind(RBRACE)){
					consume();
					break;
				}
				//statement
				else{
				    statements.add(statement());
                }
			}
		}
		else throw new SyntaxException("illegal factor");
		return new Block(first,decs,statements);
	}

	Program program() throws SyntaxException {
		//program::=  IDENT  block  
		//program::=  IDENT  param_dec ( , param_dec )* block
        ArrayList<ParamDec> paramDecs=new ArrayList<>();
        Block b;
        Program p;
        Token first=t;
		if (t.isKind(IDENT)) { 
			consume();
			if(t.isKind(LBRACE)){
			    b=block();
			}
			else if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)){
				paramDecs.add(paramDec());
				while(t.isKind(COMMA)){
					consume();
					paramDecs.add(paramDec());
				}
				b=block();
			}
			else throw new SyntaxException("illegal factor");
		} 
		else throw new SyntaxException("illegal factor");
		p=new Program(first,paramDecs,b);
		return p;
	}

	ParamDec paramDec() throws SyntaxException {
		//param_dec ::= (  KW_URL  |  KW_FILE  |  KW_INTEGER  |  KW_BOOEAN)   IDENT
        ParamDec p;
        Token first=t;
        Token ident;
		if (t.isKind(KW_URL)) {
			consume();
		}
		else if(t.isKind(KW_FILE)) { 
			consume();
		}
		else if(t.isKind(KW_INTEGER)) { 
			consume();
		}
		else if(t.isKind(KW_BOOLEAN)) { 
			consume();
		}
		else throw new SyntaxException("illegal factor");
		ident=t;
		match(IDENT);
		p=new ParamDec(first,ident);
		return p;
	}

	Dec dec() throws SyntaxException {
		//dec ::= (  KW_INTEGER  |  KW_BOOLEAN  |  KW_IMAGE  |  KW_FRAME)    IDENT
        Dec d;
        Token first=t;
        Token ident;
		if (t.isKind(KW_INTEGER)) { 
			consume();
		}
		else if(t.isKind(KW_BOOLEAN)) { 
			consume();
		}
		else if(t.isKind(KW_IMAGE)) { 
			consume();
		}
		else if(t.isKind(KW_FRAME)) { 
			consume();
		}
		else throw new SyntaxException("illegal factor");
		ident=t;
		match(IDENT);
		d=new Dec(first,ident);
		return d;
	}

	Statement statement() throws SyntaxException {
		//statement ::=  OP_SLEEP  expression ;|whileStatement|ifStatement|chain;|assign;
        Statement s=null;
        Token first=t;
		if (t.isKind(OP_SLEEP)) { 
			consume();
			s=new SleepStatement(first,expression());
			match(SEMI);
		}
		else if(t.isKind(KW_WHILE)) { 
			consume();
			if(t.isKind(LPAREN)){
				consume();
				Expression e=expression();
				match(RPAREN);
				Block b=block();
				s=new WhileStatement(first,e,b);
			}
		}
		else if(t.isKind(KW_IF)) { 
			consume();
			if(t.isKind(LPAREN)){
				consume();
				Expression e=expression();
				match(RPAREN);
				Block b=block();
				s=new IfStatement(first,e,b);
			}
		}
		//assign chain begin with IDENT
		else if(t.isKind(IDENT)) { 
			if(scanner.peek().isKind(ASSIGN)){
			    IdentLValue iv =new IdentLValue(t);
				consume();
				consume();
				Expression e=expression();
				s=new AssignmentStatement(first,iv,e);
				match(SEMI);
			}
			else{
				s=chain();match(SEMI);
			}
		}
		else {s=chain();match(SEMI);}
		return s;
	}

	Chain chain() throws SyntaxException {
		//chain ::= chainElem arrowOp chainElem ( arrowOp chainElem)*
        Token first=t;
        Chain c0;
        ChainElem c1;
        Token op;

        c0=chainElem();
	    
		if (t.isKind(ARROW)||t.isKind(BARARROW)) {
		    op=t;
			consume(); 
		}
		else throw new SyntaxException("illegal factor");
		c1=chainElem();
		c0=new BinaryChain(first,c0,op,c1);

		while(t.isKind(ARROW)||t.isKind(BARARROW)){
		    op=t;
			consume();
			c1=chainElem();
			c0=new BinaryChain(first,c0,op,c1);
		}
		return c0;
	}

	ChainElem chainElem() throws SyntaxException {
		//chainElem ::=  IDENT  | filterOp arg | frameOp arg | imageOp arg
        ChainElem ce;
		if (t.isKind(IDENT)) {
		    ce=new IdentChain(t);
			consume();
		}
		else if(t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)) {
		    Token op=t;
			consume();
			ce=new FilterOpChain(op,arg());
		}
		else if(t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)) {
		    Token op=t;
			consume();
			ce=new FrameOpChain(op,arg());
		}
		else if(t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE)) {
		    Token op=t;
			consume();
			ce=new ImageOpChain(op,arg());
		}
		else throw new SyntaxException("illegal factor");
		return ce;
	}

	Tuple arg() throws SyntaxException {
		//arg ::= eps|  (expression (  , expression)* ) )
        Token first=t;
        ArrayList<Expression> exprs=new ArrayList<>();
		if (t.isKind(LPAREN)) {
			consume();
			exprs.add(expression());
			while(t.isKind(COMMA)){
				consume();
				exprs.add(expression());
			}
			match(RPAREN);
		}
		Tuple tp=new Tuple(first,exprs);
		return tp;
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
