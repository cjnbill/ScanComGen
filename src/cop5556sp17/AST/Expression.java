package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public abstract class Expression extends ASTNode {

	Type.TypeName typename;
	public void setTypename(Type.TypeName name){
	    typename=name;
    }
    public Type.TypeName getTypename(){
	    return typename;
    }
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
