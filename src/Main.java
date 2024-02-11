import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		//String programPath = "src/BalancedBrackets.java";
		String programPath = "src/Brackets.txt";
		// Pass a directory to check for Files
		BalancedBrackets bb = new BalancedBrackets();
		System.out.println(bb.isBalanced(programPath)); 
	}

}
