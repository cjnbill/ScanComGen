package cop5556sp17;

import static cop5556sp17.Scanner.Kind.SEMI;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ScannerTest {

	@Rule
    public ExpectedException thrown = ExpectedException.none();


	
	@Test
	public void testEmpty() throws IllegalCharException, IllegalNumberException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	@Test
	public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
		//input string
		String input = ";;;";
		//create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//get the first token and check its kind, position, and contents
		Scanner.Token token = scanner.nextToken();
		assertEquals(SEMI, token.kind);
		assertEquals(0, token.pos);
		String text = SEMI.getText();
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		//get the next token and check its kind, position, and contents
		Scanner.Token token1 = scanner.nextToken();
		assertEquals(SEMI, token1.kind);
		assertEquals(1, token1.pos);
		assertEquals(text.length(), token1.length);
		assertEquals(text, token1.getText());
		Scanner.Token token2 = scanner.nextToken();
		assertEquals(SEMI, token2.kind);
		assertEquals(2, token2.pos);
		assertEquals(text.length(), token2.length);
		assertEquals(text, token2.getText());
		//check that the scanner has inserted an EOF token at the end
		Scanner.Token token3 = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF,token3.kind);
	}
	
	
	/**
	 * This test illustrates how to check that the Scanner detects errors properly. 
	 * In this test, the input contains an int literal with a value that exceeds the range of an int.
	 * The scanner should detect this and throw and IllegalNumberException.
	 * 
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	@Test
	public void testIntOverflowError() throws IllegalCharException, IllegalNumberException{
		String input = "99999999999999999";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalNumberException.class);
		scanner.scan();	
		
	}
	@Test
	public void mytest() throws IllegalCharException, IllegalNumberException{
		String input = "/*...*/a/***/\nbc!/ /*/ /**/ !\nd/*.**/";
		Scanner scanner = new Scanner(input);
		//thrown.expect(IllegalCharException.class);

		scanner.scan();
		Scanner.Token token = scanner.nextToken();
		//assertEquals(IDENT, token.kind);
		System.out.println(token.getText());
		System.out.println(token.kind);
		System.out.println(token.getLinePos().line);
		System.out.println(token.getLinePos().posInLine);
		
		Scanner.Token token2 = scanner.nextToken();
		System.out.println(token2.getText());
		System.out.println(token2.kind);
		System.out.println(token2.getLinePos().line);
		System.out.println(token2.getLinePos().posInLine);
		
		Scanner.Token token3 = scanner.nextToken();
		System.out.println(token3.getText());
		System.out.println(token3.kind);
		System.out.println(token3.getLinePos().line);
		System.out.println(token3.getLinePos().posInLine);
		
		Scanner.Token token4 = scanner.nextToken();
		System.out.println(token4.getText());
		System.out.println(token4.kind);
		System.out.println(token4.getLinePos().line);
		System.out.println(token4.getLinePos().posInLine);
		//assertEquals(text, token.getText());
		Scanner.Token token5 = scanner.nextToken();
		token5 = scanner.nextToken();
		System.out.println(token5.getText());
		System.out.println(token5.kind);
		System.out.println(token5.getLinePos().line);
		System.out.println(token5.getLinePos().posInLine);
	}


//TODO  more tests
	
}
