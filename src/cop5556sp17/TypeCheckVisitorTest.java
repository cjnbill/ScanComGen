/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 *
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

import java.util.ArrayList;

public class TypeCheckVisitorTest {


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAssignmentBoolLit0() throws Exception{
		String input = "p {\nboolean y \ny <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testAssignmentBoolLitError0() throws Exception{
		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	// My own test cases
	// by Ke Jin

	@Test
	public void testComplexProgram() throws Exception {
		String input = "main integer a, boolean b, file f {\n" +
				"boolean c\n" +
				"image output\n" +
				"integer i\n" +
				"c <- (a >= 5 == b) <= (true != false);\n" +
				"output <- output - output;\n" +
				"b <- output * 2 == 2 * output;\n" +
				"if (c == true) {\n" +
				"   image b" +
				"   f -> output;" +
				"   output -> b;\n" +
				"   output -> gray;\n" +
				"   output -> f;\n" +
				"   frame frm\n" +
				"   output -> frm -> move(1*2/2,2+3*5) -> xloc;" +
				"}\n" +
				"output -> width;\n" +
				"i <- 100/10;\n" +
				"while (i > 0) {\n" +
				"   output -> scale(screenwidth/2);\n" +
				"   i <- i-1;\n" +
				"}\n" +
				"f -> output |-> convolve;\n" +
				"frame rtn\n" +
				"rtn -> show;\n" +
				"sleep 10;\n" +
				"}\n";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}


	@Test
	public void testIllegalChainCombination1() throws Exception {
		String input = "p {integer a\n a |-> a;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalBiExpressionCombination() throws Exception {
		String input = "p {image a\n a <- a / a;}";

		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalFilterOpChain() throws Exception {
		String input = "p {image a\n a -> gray(1);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalFrameOpChain() throws Exception {
		String input = "p {frame a\n a -> move(1,2,3);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalImageOpChain() throws Exception {
		String input = "p {image a\n a -> scale(1,2);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIdentChain1() throws Exception {
		String input = "p {image a\n" +
				"if (true) {integer b}\n" +
				"a -> b;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIdentChain2() throws Exception {
		String input = "p {image a\n" +
				"a -> b;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIdentExpression1() throws Exception {
		String input = "p {integer a\n" +
				"if (true) {integer b}\n" +
				"a <- b;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIdentExpression2() throws Exception {
		String input = "p {integer a\n" +
				"a <- b;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIfStatement() throws Exception {
		String input = "p {if (2) {integer b}}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalWhileStatement() throws Exception {
		String input = "p {while (2) {integer b}}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalSleepStatement() throws Exception {
		String input = "p {sleep true;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalDec() throws Exception {
		String input = "p {integer a\ninteger a}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalParamDec() throws Exception {
		String input = "p integer a, file a {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIdentLValue1() throws Exception {
		String input = "p \nurl y {\n  image i\n  y|->i;\n}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalIdentLValue2() throws Exception {
		String input = "p {if (true) {integer b}\n" +
				"b <- 2;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}

	@Test
	public void testIllegalTuple() throws Exception {
		String input = "p {image a\n" +
				"a -> move(1,true);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckException.class);
		program.visit(v, null);
	}
}
