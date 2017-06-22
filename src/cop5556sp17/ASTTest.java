package cop5556sp17;

import static cop5556sp17.Scanner.Kind.PLUS;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.AST.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;

public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}



	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

	@Test
	public void testBinaryExpr1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc+true";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(BooleanLitExpression.class, be.getE1().getClass());
		be=(BinaryExpression) be.getE0();
		assertEquals(IdentExpression.class, be.getE1().getClass());
        assertEquals(IntLitExpression.class, be.getE0().getClass());
	}
    @Test
    public void testBlock0() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{integer a while(1){}}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode ast = parser.block();
        assertEquals(Block.class, ast.getClass());
        Block b=(Block)ast;
        assertEquals("a",b.getDecs().get(0).getIdent().getText());
        assertEquals("while",b.getStatements().get(0).getFirstToken().getText());
    }
    @Test
    public void testProgram() throws IllegalCharException, IllegalNumberException, SyntaxException {
     String input = "aa boolean abc { }";
     Scanner scanner = new Scanner(input);
     scanner.scan();
     Parser parser = new Parser(scanner);
     ASTNode ast = parser.program();
     assertEquals(Program.class, ast.getClass());
     Program be = (Program) ast;
     assertEquals(ArrayList.class, be.getParams().getClass());
     assertEquals(Block.class, be.getB().getClass());
    }
    
    @Test
    public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
     String input = "abc -> blur (q) |-> efg";
     Scanner scanner = new Scanner(input);
     scanner.scan();
     Parser parser = new Parser(scanner);
     ASTNode ast = parser.chain();
     assertEquals(BinaryChain.class, ast.getClass());
     BinaryChain bc = (BinaryChain) ast;
     assertEquals(BinaryChain.class, bc.getE0().getClass());
     assertEquals(IdentChain.class, bc.getE1().getClass());
    }
    @Test
    public void testComplexProgram() throws IllegalCharException, IllegalNumberException, SyntaxException{
        String input = "main integer a, boolean b {\n" +
                "boolean c\n" +
                "image output\n" +
                "integer i\n" +
                "c <- true & true == b | false & false;\n" +
                "if (c == true) {\n" +
                "   output -> gray(1+2);\n" +
                "   output -> show;\n" +
                "}\n" +
                "while (i > 0) {\n" +
                "   output -> scale(1/2, (5+1)*4);\n" +
                "}\n" +
                "hide -> output |-> b;\n" +
                "sleep 1;\n" +
                "}\n";
        Parser parser = new Parser(new Scanner(input).scan());
        ASTNode ast = parser.parse();
        assertEquals(Program.class, ast.getClass());

        // Check program node
        Program prog = (Program) ast;
        assertEquals("main", prog.getName());
        assertEquals(Block.class, prog.getB().getClass());
        List<ParamDec> paramDecList = prog.getParams();
        assertEquals(Kind.KW_INTEGER, paramDecList.get(0).getType().kind);
        assertEquals("a", paramDecList.get(0).getIdent().getText());
        assertEquals(Kind.KW_BOOLEAN, paramDecList.get(1).getType().kind);
        assertEquals("b", paramDecList.get(1).getIdent().getText());

        // Check program block node
        Block progB = prog.getB();
        List<Dec> decList = progB.getDecs();
        List<Statement> statementList = progB.getStatements();
        // Decs
        assertEquals(Kind.KW_BOOLEAN, decList.get(0).getType().kind);
        assertEquals("c", decList.get(0).getIdent().getText());
        assertEquals(Kind.KW_IMAGE, decList.get(1).getType().kind);
        assertEquals("output", decList.get(1).getIdent().getText());
        assertEquals(Kind.KW_INTEGER, decList.get(2).getType().kind);
        assertEquals("i", decList.get(2).getIdent().getText());

        // Statements
        // Assignment
        // "c <- true & true == b | false & false;\n" +
        assertEquals(AssignmentStatement.class, statementList.get(0).getClass());
        AssignmentStatement assignmentStatement = (AssignmentStatement) statementList.get(0);
        assertEquals(IdentLValue.class, assignmentStatement.getVar().getClass());
        assertEquals(Kind.IDENT, assignmentStatement.getVar().getFirstToken().kind);
        // ==
        assertEquals(BinaryExpression.class, assignmentStatement.getE().getClass());
        BinaryExpression tempBE1 = (BinaryExpression)assignmentStatement.getE();
        assertEquals(Kind.EQUAL, tempBE1.getOp().kind);
        BinaryExpression tempBE2 = (BinaryExpression)tempBE1.getE0();
        assertEquals(Kind.AND, tempBE2.getOp().kind);
        tempBE2 = (BinaryExpression)tempBE1.getE1();
        assertEquals(Kind.OR, tempBE2.getOp().kind);
        tempBE2 = (BinaryExpression)tempBE2.getE1();
        assertEquals(Kind.AND, tempBE2.getOp().kind);

        // If
        assertEquals(IfStatement.class, statementList.get(1).getClass());
        IfStatement ifStatement = (IfStatement) statementList.get(1);
        assertEquals(BinaryExpression.class, ifStatement.getE().getClass());
        assertEquals(Kind.IDENT, ((BinaryExpression)ifStatement.getE()).getE0().firstToken.kind);
        assertEquals(Kind.EQUAL, ((BinaryExpression)ifStatement.getE()).getOp().kind);
        assertEquals(Kind.KW_TRUE, ((BinaryExpression)ifStatement.getE()).getE1().firstToken.kind);

        Block ifB = ifStatement.getB();
        assertEquals(0, ifB.getDecs().size());
        // output -> gray(1+2);
        assertEquals(BinaryChain.class, ifB.getStatements().get(0).getClass());
        BinaryChain tempChain = (BinaryChain)ifB.getStatements().get(0);
        assertEquals(IdentChain.class, tempChain.getE0().getClass());
        assertEquals(FilterOpChain.class, tempChain.getE1().getClass());
        assertEquals("gray", tempChain.getE1().getFirstToken().getText());
        Tuple tempTuple = ((FilterOpChain)tempChain.getE1()).getArg();
        assertEquals(1, tempTuple.getExprList().size());
        assertEquals(BinaryExpression.class, tempTuple.getExprList().get(0).getClass());
        assertEquals(Kind.PLUS, ((BinaryExpression)tempTuple.getExprList().get(0)).getOp().kind);
        // output -> show;
        assertEquals(BinaryChain.class, ifB.getStatements().get(1).getClass());
        tempChain = (BinaryChain)ifB.getStatements().get(1);
        assertEquals(IdentChain.class, tempChain.getE0().getClass());
        assertEquals(FrameOpChain.class, tempChain.getE1().getClass());
        assertEquals("show", tempChain.getE1().getFirstToken().getText());
        tempTuple = ((FrameOpChain)tempChain.getE1()).getArg();
        assertEquals(0, tempTuple.getExprList().size());

        // While
        assertEquals(WhileStatement.class, statementList.get(2).getClass());
        WhileStatement whileStatement = (WhileStatement) statementList.get(2);
        assertEquals(BinaryExpression.class, whileStatement.getE().getClass());
        assertEquals(Kind.IDENT, ((BinaryExpression)whileStatement.getE()).getE0().firstToken.kind);
        assertEquals(Kind.GT, ((BinaryExpression)whileStatement.getE()).getOp().kind);
        assertEquals(Kind.INT_LIT, ((BinaryExpression)whileStatement.getE()).getE1().firstToken.kind);

        Block whileB = whileStatement.getB();
        assertEquals(0, whileB.getDecs().size());
        // output -> scale(1/2, (5+1)*4);
        assertEquals(BinaryChain.class, whileB.getStatements().get(0).getClass());
        tempChain = (BinaryChain)whileB.getStatements().get(0);
        assertEquals(IdentChain.class, tempChain.getE0().getClass());
        assertEquals(ImageOpChain.class, tempChain.getE1().getClass());
        assertEquals("scale", tempChain.getE1().getFirstToken().getText());
        tempTuple = ((ImageOpChain)tempChain.getE1()).getArg();
        assertEquals(2, tempTuple.getExprList().size());
        assertEquals(BinaryExpression.class, tempTuple.getExprList().get(0).getClass());
        assertEquals(Kind.DIV, ((BinaryExpression)tempTuple.getExprList().get(0)).getOp().kind);
        assertEquals(BinaryExpression.class, tempTuple.getExprList().get(1).getClass());
        tempBE1 = (BinaryExpression)tempTuple.getExprList().get(1);
        assertEquals(Kind.TIMES, tempBE1.getOp().kind);
        assertEquals("4", tempBE1.getE1().firstToken.getText());
        tempBE1 = (BinaryExpression)tempBE1.getE0();
        assertEquals(Kind.PLUS, tempBE1.getOp().kind);
        assertEquals("5", tempBE1.getE0().firstToken.getText());
        assertEquals("1", tempBE1.getE1().firstToken.getText());

        // Chain
        // hide -> output |-> b;
        assertEquals(BinaryChain.class, statementList.get(3).getClass());
        BinaryChain chain = (BinaryChain) statementList.get(3);
        assertEquals("b", chain.getE1().firstToken.getText());
        assertEquals(Kind.BARARROW, chain.getArrow().kind);
        assertEquals(BinaryChain.class, chain.getE0().getClass());
        chain = (BinaryChain)chain.getE0();
        assertEquals(FrameOpChain.class, chain.getE0().getClass());
        assertEquals("output", chain.getE1().firstToken.getText());
        assertEquals(Kind.ARROW, chain.getArrow().kind);

        // Sleep
        // sleep 1;
        assertEquals(SleepStatement.class, statementList.get(4).getClass());
        assertEquals(Kind.OP_SLEEP, statementList.get(4).firstToken.kind);
        assertEquals(Kind.INT_LIT, ((SleepStatement)statementList.get(4)).getE().firstToken.kind);

    }

}
