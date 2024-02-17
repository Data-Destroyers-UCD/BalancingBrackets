// Imports for File exceptions
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

// Definition for the main class
public class Main {
	// main function that serves as the execution point
	public static void main(String[] args) throws IOException {
		// Get the path of the program
		//String programPath = "src/BalancedBrackets.java";
		String programPath = "src/Brackets.txt";
		// Create a balanced brackets object
		BalancedBrackets bb = new BalancedBrackets(true);
		// Print if the brackets are balanced
		System.out.println(bb.isBalanced(programPath));
		BalancedBrackets bb2 = new BalancedBrackets(false);
		System.out.println(bb2.isBalanced(programPath));

		// List of all file paths
		List<String> filePaths = Arrays.asList(
				"src/inputs/AccessControlException.java",
				"src/inputs/AlgorithmConstraints.java",
				"src/inputs/Ref.java",
				"src/inputs/SignedMutableBigInteger.java",
				"src/inputs/Policy.java",
				"src/inputs/AccessController.java",
				"src/inputs/AccessControlContext.java",
				"src/inputs/Security.java",
				"src/inputs/combination.java",
				"src/inputs/combination2.java",
				"src/inputs/combination3.java"

				// ... more file paths
		);

		// Header for the table
		System.out.printf("%-30s %-15s %-15s %-15s%n", "Program", "Lines of Code", "Stack Time", "Deque Time");

		// Test each file
		for (String filePath : filePaths) {
			long lines = Files.lines(Paths.get(filePath)).count();

			// Stack time test
			long startStackTime = System.currentTimeMillis();
			new BalancedBrackets(true).isBalanced(filePath);
			long stackTime = System.currentTimeMillis() - startStackTime;

			// Deque time test
			long startDequeTime = System.currentTimeMillis();
			new BalancedBrackets(false).isBalanced(filePath);
			long dequeTime = System.currentTimeMillis() - startDequeTime;

			// Output the formatted result for this file
			System.out.printf("%-30s %-15d %-15s %-15s%n", new File(filePath).getName(), lines, stackTime+"ms", dequeTime+"ms");
		}




	}

}
