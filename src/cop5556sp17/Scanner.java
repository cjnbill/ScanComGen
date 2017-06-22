package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;

import cop5556sp17.Scanner.Kind;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	public static enum State{
		START,IN_DIGIT,IN_IDENT,AFTER_EQ,AFTER_NOT,
		AFTER_OR,AFTER_LT,AFTER_GT,AFTER_MINUS,AFTER_ORMINUS,
		AFTER_TIMES,AFTER_DIV,BEGIN_COM,END_COM
		}
	public static HashMap<String, Kind> reserved = new HashMap<>();

/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			if(kind==Kind.EOF)return "eof";
			String s=chars.substring(pos, pos+length);
			return s;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			return Scanner.this.getLinePos(this);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
    		return Integer.parseInt(chars.substring(pos,pos+length));
		}

		public boolean isKind(Kind k) {
			// TODO Auto-generated method stub
			return kind==k;
		}
		 @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		for(Kind kind:Kind.values()){  
	        reserved.put(kind.getText(), kind);  
	    }
		lines=new ArrayList<Integer>();
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		//tokens.add(new Token(Kind.EOF,pos,0));
		int length=chars.length();
	    State state = State.START;
	    int startPos = 0;
	    int ch;
	    while (pos <= length) {
	        ch = pos < length ? chars.charAt(pos) : -1;
	        switch (state) {
	            case START: {
	                pos = skipWhiteSpace(pos);
	                ch = pos < length ? chars.charAt(pos) : -1;
	                startPos = pos;
	                switch (ch) {
	                    //end of line
	                    case '\n':{lines.add(pos);pos++;}break;
	                    //end of file
	                    case -1: {lines.add(pos);tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
	                    //operator
	                    case '+': {tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;} break;
	                    //case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
	                    //case '/': {tokens.add(new Token(Kind.DIV, startPos, 1));pos++;} break;
	                    case '&': {tokens.add(new Token(Kind.AND, startPos, 1));pos++;} break;
	                    case '%': {tokens.add(new Token(Kind.MOD, startPos, 1));pos++;} break;
	                    case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
	                    case '/': {state = State.AFTER_DIV;pos++;}break;
	                    case '=': {state = State.AFTER_EQ;pos++;}break;
	                    case '!': {state = State.AFTER_NOT;pos++;}break;
	                    case '<': {state = State.AFTER_LT;pos++;}break;
	                    case '>': {state = State.AFTER_GT;pos++;}break;
	                    case '-': {state = State.AFTER_MINUS;pos++;}break;
	                    case '|': {state = State.AFTER_OR;pos++;}break;
	                    //separator
	                    case ';': {tokens.add(new Token(Kind.SEMI, startPos, 1));pos++;} break;
	                    case ',': {tokens.add(new Token(Kind.COMMA, startPos, 1));pos++;} break;
	                    case '(': {tokens.add(new Token(Kind.LPAREN, startPos, 1));pos++;} break;
	                    case ')': {tokens.add(new Token(Kind.RPAREN, startPos, 1));pos++;} break;
	                    case '{': {tokens.add(new Token(Kind.LBRACE, startPos, 1));pos++;} break;
	                    case '}': {tokens.add(new Token(Kind.RBRACE, startPos, 1));pos++;} break;
	                    //0
	                    case '0': {tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;}break;
	                    default: {
	                    	//digit
	                        if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;}
	                        //id
	                        else if (Character.isJavaIdentifierStart(ch)) {
	                             state = State.IN_IDENT;pos++;
	                         }
	                        //illegal
	                         else {throw new IllegalCharException(
	                                    "illegal char " +ch+" at pos "+pos);
	                         }
	                      }
	                } // switch (ch)

	            }  break;
	            case IN_DIGIT: {
	            	boolean success=true;
	            	if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;}
	            	else {
	            		try {
	            			Integer.parseInt(chars.substring(startPos,pos));
                        } catch (NumberFormatException nfe) {
                        	success = false;
                        	throw new IllegalNumberException(
                                    "illegal number " +ch+" at pos "+pos);
                        }
	            		if(success)tokens.add(new Token(Kind.INT_LIT,startPos, pos-startPos));
	            		state = State.START;
	            	}
	            }break;
	            case IN_IDENT: {
	            	if (Character.isJavaIdentifierPart(ch)) {
                        state = State.IN_IDENT;pos++;
                    }
	            	else{
	            		//TODO
	            		String ident=chars.substring(startPos,pos);
	            		//System.out.println(ident);
	            		if(reserved.containsKey(ident)){
	            			tokens.add(new Token(reserved.get(ident),startPos, pos-startPos));
	            		}
	            		else 
	            			tokens.add(new Token(Kind.IDENT,startPos, pos-startPos));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_EQ: {
	            	if(ch=='='){
	            		tokens.add(new Token(Kind.EQUAL,startPos, 2));
	            		state = State.START;
	            		pos++;
	            	}
	            	else {
	            		throw new IllegalCharException(
                            "illegal char " +ch+" at pos "+pos);
                    }
	            }  break;
	            case AFTER_NOT:{
	            	if(ch=='='){
	            		tokens.add(new Token(Kind.NOTEQUAL,startPos, 2));
	            		state = State.START;
	            		pos++;
	            	}
	            	else {
	            		tokens.add(new Token(Kind.NOT,startPos, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_OR:{
	            	if(ch=='-'){
	            		state = State.AFTER_ORMINUS;
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.OR,startPos, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_LT:{
	            	if(ch=='='){
	            		tokens.add(new Token(Kind.LE,startPos, 2));
	            		state = State.START;
	            		pos++;
	            	}
	            	else if(ch=='-'){
	            		tokens.add(new Token(Kind.ASSIGN,startPos, 2));
	            		state = State.START;
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.LT,startPos, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_GT:{
	            	if(ch=='='){
	            		tokens.add(new Token(Kind.GE,startPos, 2));
	            		state = State.START;
	            		pos++;
	            	}
	            	else {
	            		tokens.add(new Token(Kind.GT,startPos, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_MINUS:{
	            	if(ch=='>'){
	            		tokens.add(new Token(Kind.ARROW,startPos, 2));
	            		state = State.START;
	            		pos++;
	            	}
	            	else {
	            		tokens.add(new Token(Kind.MINUS,startPos, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_ORMINUS:{
	            	if(ch=='>'){
	            		tokens.add(new Token(Kind.BARARROW,startPos, 3));
	            		state = State.START;
	            		pos++;
	            	}
	            	else {
	            		tokens.add(new Token(Kind.OR,startPos, 1));
	            		tokens.add(new Token(Kind.MINUS,startPos+1, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case AFTER_DIV:{
	            	if(ch=='*'){
	            		state = State.BEGIN_COM;
	            		pos++;
	            	}
	            	else {
	            		tokens.add(new Token(Kind.DIV,startPos, 1));
	            		state = State.START;
	            	}
	            }  break;
	            case BEGIN_COM:{
	            	if(ch=='*'){
	            		state = State.END_COM;
	            		pos++;
	            	}
	            	else {
	            		if(ch=='\n'){
	            			lines.add(pos);
	            		}
	            		else if(ch==-1){
	            			lines.add(pos);tokens.add(new Token(Kind.EOF, pos, 0)); 
	            		}
	            		pos++;
	            	}
	            }  break;
	            case END_COM:{
	            	if(ch=='/'){
	            		state = State.START;
	            		pos++;
	            	}
	            	else if(ch=='*'){
	            		state = State.END_COM;
	            		pos++;
	            	}
	            	else {
	            		state = State.BEGIN_COM;
	            		if(ch=='\n'){
	            			lines.add(pos);
	            		}
	            		else if(ch==-1){
	            			lines.add(pos);tokens.add(new Token(Kind.EOF, pos, 0)); 
	            		}
	            		pos++;
	            	}
	            }  break;
	            default:  assert false;
	        }// switch(state)
	    } // while

		return this;  
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	final ArrayList<Integer> lines;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		if(lines.size()==0)return new LinePos(0,0);
		int posInLine=0;
		int line=0;
		int preline=0;
		boolean b=false;
		for (int i = 0; i < lines.size(); i++) {
			if(i==0&&t.pos < lines.get(i)){
				posInLine=t.pos-preline;
				b=true;
				break;
			}
			else if(t.pos < lines.get(i)){
				posInLine=t.pos-preline-1;
				b=true;
				break;
			}
			preline=lines.get(i);
			line++;
		}
		if(!b){line++;posInLine=t.pos-lines.get(lines.size()-1)-1;}
		return new LinePos(line,posInLine);
	}
	public int skipWhiteSpace(int pos){
		int i;
		boolean b=false;
		for(i = pos; i < chars.length(); i++) {
			if(chars.charAt(i)!=' '&&chars.charAt(i)!='\t'&&chars.charAt(i)!='\r'){
				b=true;
				break;
			}
        }
		return b?i:i+1;
	}


}
