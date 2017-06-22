package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.AST.Type.TypeName;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Chain c=binaryChain.getE0();
        Token op=binaryChain.getArrow();
        ChainElem ce=binaryChain.getE1();
        c.visit(this,null);
        ce.visit(this,null);
        if(c.getTypename()==URL&&op.isKind(ARROW)&&ce.getTypename()==IMAGE){
            binaryChain.setTypename(IMAGE);
        }
        else if(c.getTypename()==FILE&&op.isKind(ARROW)&&ce.getTypename()==IMAGE){
            binaryChain.setTypename(IMAGE);
        }
        else if(c.getTypename()==FRAME&&op.isKind(ARROW)&&FrameOpChain.class.isInstance(ce)
                &&(ce.getFirstToken().isKind(KW_XLOC)||ce.getFirstToken().isKind(KW_YLOC))){
            binaryChain.setTypename(INTEGER);
        }
        else if (c.getTypename()==FRAME&&op.isKind(ARROW)&&FrameOpChain.class.isInstance(ce)
                &&(ce.getFirstToken().isKind(KW_SHOW)
                        ||ce.getFirstToken().isKind(KW_HIDE)
                        ||ce.getFirstToken().isKind(KW_MOVE))){
            binaryChain.setTypename(FRAME);
        }
        else if (c.getTypename()==IMAGE&&op.isKind(ARROW)&&ImageOpChain.class.isInstance(ce)
                &&(ce.getFirstToken().isKind(OP_WIDTH)
                        ||ce.getFirstToken().isKind(OP_HEIGHT))){
            binaryChain.setTypename(INTEGER);
        }
        else if(c.getTypename()==IMAGE&&op.isKind(ARROW)&&ce.getTypename()==FRAME){
            binaryChain.setTypename(FRAME);
        }
        else if(c.getTypename()==IMAGE&&op.isKind(ARROW)&&ce.getTypename()==FILE){
            binaryChain.setTypename(NONE);
        }
        else if (c.getTypename()==IMAGE
                &&(op.isKind(ARROW)||op.isKind(BARARROW))
                &&FilterOpChain.class.isInstance(ce)
                &&(ce.getFirstToken().isKind(OP_GRAY)
                ||ce.getFirstToken().isKind(OP_BLUR)
                ||ce.getFirstToken().isKind(OP_CONVOLVE))){
            binaryChain.setTypename(IMAGE);
        }
        else if (c.getTypename()==IMAGE &&op.isKind(ARROW) &&ImageOpChain.class.isInstance(ce)
                &&ce.getFirstToken().isKind(KW_SCALE)){
            binaryChain.setTypename(IMAGE);
        }
        else if (c.getTypename()==IMAGE &&op.isKind(ARROW) &&IdentChain.class.isInstance(ce)
                &&ce.getTypename()==IMAGE){
            binaryChain.setTypename(IMAGE);
        }
        else if(c.getTypename()==INTEGER&&op.isKind(ARROW)&&IdentChain.class.isInstance(ce)
                &&ce.getTypename()==INTEGER){
            binaryChain.setTypename(INTEGER);
        }
        else throw new TypeCheckException(binaryChain.getE1().getFirstToken().getText());
        return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Expression e0=binaryExpression.getE0();
        Expression e1=binaryExpression.getE1();
        Token op=binaryExpression.getOp();
        e0.visit(this,null);
        e1.visit(this,null);
        if(e0.getTypename()==INTEGER
                &&(op.isKind(PLUS)||op.isKind(MINUS))
                &&e1.getTypename()==INTEGER){
            binaryExpression.setTypename(INTEGER);
        }
        else if(e0.getTypename()==IMAGE
                &&(op.isKind(PLUS)||op.isKind(MINUS))
                &&e1.getTypename()==IMAGE){
            binaryExpression.setTypename(IMAGE);
        }
        else if(e0.getTypename()==INTEGER
                &&(op.isKind(TIMES)||op.isKind(DIV)||op.isKind(MOD))
                &&e1.getTypename()==INTEGER){
            binaryExpression.setTypename(INTEGER);
        }
        else if(e0.getTypename()==INTEGER
                &&op.isKind(TIMES)
                &&e1.getTypename()==IMAGE){
            binaryExpression.setTypename(IMAGE);
        }
        else if(e0.getTypename()==IMAGE
                &&op.isKind(TIMES)
                &&e1.getTypename()==INTEGER){
            binaryExpression.setTypename(IMAGE);
        }
        else if(e0.getTypename()==INTEGER
                &&(op.isKind(LT)||op.isKind(GT)||op.isKind(LE)||op.isKind(GE))
                &&e1.getTypename()==INTEGER){
            binaryExpression.setTypename(BOOLEAN);
        }
        else if(e0.getTypename()==BOOLEAN
                &&(op.isKind(LT)||op.isKind(GT)||op.isKind(LE)||op.isKind(GE)||op.isKind(AND)||op.isKind(OR))
                &&e1.getTypename()==BOOLEAN){
            binaryExpression.setTypename(BOOLEAN);
        }
        else if(op.isKind(EQUAL)||op.isKind(NOTEQUAL)){
            if(e0.getTypename()!=e1.getTypename())
                throw new TypeCheckException(binaryExpression.getFirstToken().getText());
            binaryExpression.setTypename(BOOLEAN);
        }
        else if((op.isKind(MOD)||op.isKind(DIV))&&e0.getTypename()==IMAGE&&e1.getTypename()==INTEGER){
            binaryExpression.setTypename(IMAGE);
        }
        else throw new TypeCheckException("BinaryExpression");
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
        symtab.enterScope();
        for(Dec d:block.getDecs()){
            d.visit(this,null);
        }
        for(Statement s:block.getStatements()){
            s.visit(this,null);
        }
        symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
        booleanLitExpression.setTypename(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
        filterOpChain.getArg().visit(this,null);
        if(!filterOpChain.getArg().getExprList().isEmpty())
            throw new TypeCheckException(filterOpChain.getFirstToken().getText());
        filterOpChain.setTypename(IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Token op=frameOpChain.getFirstToken();
        Tuple tp=frameOpChain.getArg();
        frameOpChain.getArg().visit(this,null);
        if (op.isKind(KW_SHOW)||op.isKind(KW_HIDE)) {
            if(!tp.getExprList().isEmpty())
                throw new TypeCheckException(frameOpChain.getFirstToken().getText());
            frameOpChain.setTypename(NONE);
        }
        else if (op.isKind(KW_XLOC)||op.isKind(KW_YLOC)){
            if(!tp.getExprList().isEmpty())
                throw new TypeCheckException(frameOpChain.getFirstToken().getText());
            frameOpChain.setTypename(INTEGER);
        }
        else if(op.isKind(KW_MOVE)){
            if(tp.getExprList().size()!=2)
                throw new TypeCheckException(frameOpChain.getFirstToken().getText());
            frameOpChain.setTypename(NONE);
        }
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Dec dec=symtab.lookup(identChain.getFirstToken().getText());
        if(dec==null)
            throw new TypeCheckException(identChain.getFirstToken().getText());
        identChain.setTypename(dec.getTypename());
        identChain.setDec(symtab.lookup(identChain.getFirstToken().getText()));
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Dec dec=symtab.lookup(identExpression.getFirstToken().getText());
        if(dec==null)
            throw new TypeCheckException(identExpression.getFirstToken().getText());
        identExpression.setTypename(dec.getTypename());
        identExpression.setDec(symtab.lookup(identExpression.getFirstToken().getText()));
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
        ifStatement.getE().visit(this,null);
        ifStatement.getB().visit(this,null);
        if(ifStatement.getE().getTypename()!=BOOLEAN)
            throw new TypeCheckException(ifStatement.getFirstToken().getText());
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
        intLitExpression.setTypename(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Expression e=sleepStatement.getE();
        e.visit(this,null);
        if(e.getTypename()!=INTEGER)
              throw new TypeCheckException(sleepStatement.getFirstToken().getText());
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
        whileStatement.getE().visit(this,null);
        whileStatement.getB().visit(this,null);
        if(whileStatement.getE().getTypename()!=BOOLEAN)
            throw new TypeCheckException(whileStatement.getFirstToken().getText());
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
        declaration.setTypename(Type.getTypeName(declaration.getType()));
        if(!symtab.insert(declaration.getIdent().getText(),declaration))
            throw new TypeCheckException(declaration.getFirstToken().getLinePos().toString());
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
        for(ParamDec pd:program.getParams()) {
            pd.visit(this,null);
        }
        program.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
        IdentLValue iv=assignStatement.getVar();
        Expression e=assignStatement.getE();
        iv.visit(this,null);
        e.visit(this,null);
        if(iv.getDec().getTypename()!=e.getTypename())
            throw new TypeCheckException(assignStatement.getFirstToken().getText());
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Dec dec=symtab.lookup(identX.getFirstToken().getText());
        if(dec==null)
            throw new TypeCheckException(identX.getText());
        identX.setDec(dec);
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
        paramDec.setTypename(Type.getTypeName(paramDec.getType()));
        if(!symtab.insert(paramDec.getIdent().getText(),paramDec))
            throw new TypeCheckException(paramDec.getFirstToken().getText());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
        constantExpression.setTypename(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
        Token op=imageOpChain.getFirstToken();
        Tuple tp=imageOpChain.getArg();
        if (op.isKind(OP_WIDTH)||op.isKind(OP_HEIGHT)){
            if(!tp.getExprList().isEmpty())
                throw new TypeCheckException(imageOpChain.getFirstToken().getText());
            imageOpChain.setTypename(INTEGER);
        }
        else if (op.isKind(KW_SCALE)){
            if(tp.getExprList().size()!=1)
                throw new TypeCheckException(imageOpChain.getFirstToken().getText());
            imageOpChain.setTypename(IMAGE);
        }
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
        for(Expression e:tuple.getExprList()){
            e.visit(this,null);
            if(e.getTypename()!=INTEGER)
                throw new TypeCheckException(tuple.getFirstToken().getText());
        }
		return null;
	}


}
