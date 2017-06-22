package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
    Type.TypeName typename;
    public void setTypename(Type.TypeName name){
        typename=name;
    }
    public Type.TypeName getTypename(){
        return typename;
    }
}
