// Imports for File exceptions
import java.io.IOException;

// Definition for the main class
public class Main {
	// main function that serves as the execution point
	public static void main(String[] args) throws IOException {
		// Get the path of the program
		//String programPath = "src/BalancedBrackets.java";
		String programPath = "src/Brackets.txt";
		// Create a balanced brackets object
		BalancedBrackets bb = new BalancedBrackets();
		// Print if the brackets are balanced
		System.out.println(bb.isBalanced(programPath)); 
	}

}
