// Imports for File exceptions
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// Definition for the main class
public class Main {
	// main function that serves as the execution point
	public static void main(String[] args) throws IOException {
	
		// Only specify the folder source
		// Note: the java files in the inputs folder might display errors in eclipse IDE as they are manipulated for the sake of this project.
		File dir = new File("src/inputs");
		// Filter the java files
		FilenameFilter filter = (f, name) -> name.endsWith(".java");
		// Get the file names array
		String[] fileNames = dir.list(filter);

		// Create an empty list for file info
		List<FileInfo> fileInfoList = new ArrayList<>();
		// Loop through each file in the array of file names
		for(String fileName : fileNames) {
			// Create a file name relative to the source of the program
			String filePath = "src/inputs/" + fileName;
			// Get the line count
			long lines = Files.lines(Paths.get(filePath)).count();

			// Stack time test
			long startStackTime = System.currentTimeMillis(); // Start the timer
			new BalancedBrackets(true).isBalanced(filePath); // Check bracket balance
			long stackTime = System.currentTimeMillis() - startStackTime; // calculate the time

			// Deque time test
			long startDequeTime = System.currentTimeMillis(); // Start the timer
			new BalancedBrackets(false).isBalanced(filePath); // Check bracket balance
			long dequeTime = System.currentTimeMillis() - startDequeTime; // calculate the time
			
			// Add the file information to the list
			fileInfoList.add(new FileInfo(fileName, lines, stackTime, dequeTime));
		}

		// Sort by line count
		fileInfoList.sort(Comparator.comparingLong(f -> f.lineCount));

		// Header for the table
		System.out.printf("%-30s %-15s %-15s %-15s%n", "Program", "Lines of Code", "Stack Time", "Deque Time");

		// Output the sorted result in table form
		for (FileInfo fileInfo : fileInfoList) {
			System.out.printf("%-30s %-15d %-15s %-15s%n", fileInfo.fileName, fileInfo.lineCount, fileInfo.stackTime + "ms", fileInfo.dequeTime + "ms");
		}



	}

}
