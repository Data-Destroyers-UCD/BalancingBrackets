// Imports for File handling
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// Class for checking Balanced Brackets
public class BalancedBrackets {
	
	// Constructor
	public BalancedBrackets() {
		// Check which data structure to utilise
		if(BalancedBrackets.shouldUseStack) {
			this.stack = new Stack<Character>();
		} else {
			this.deque = new Deque<Character>();
		}
	}
	
	// A balance check method that takes in a java file
	public boolean isBalanced(String filePath) throws IOException {
		// Store the characters as array of string
		String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		
		// skip comments, strings-> "" or '' or /**/ or //
		text = this.removeComments(text);
		text = this.removeStringsAndChars(text);
				
		// Loop through each character in the text
	    for (char c : text.toCharArray()) {
	        if (this.isOpeningBracket(c)) {
	            // If the character is an opening bracket, push it onto the stack/deque
	            this.push(c);
	        } else if (this.isClosingBracket(c)) {
	        	Character b = this.pop();
	            // If the character is a closing bracket
	            if (!this.isMatchingBracket(b, c)) {
	                // If there is no corresponding opening bracket or it doesn't match, return false
	                return false;
	            }
	        }
	    }

		// If after looping through every character in the
		//		file, the brackets are balanced if the stack is empty
		return this.isEmpty();
	}
	
	// Variable to check if we want to use stack
	private static boolean shouldUseStack = true;
	// Store the starting bracket types
	private static char[] openingParanthesis = { '{', '[', '(' };
	// Store the ending brackets types
	private static char[] closingParanthesis = { '}', ']', ')'};
	// Variable for deque data structure
	private Deque<Character> deque;
	// Variable for stack data structure
	private Stack<Character> stack;
	
	// Method to push into the stack or deque
	private void push(Character value) {
		// Push into stack if the variable is set
		if(BalancedBrackets.shouldUseStack) {
			// Push at the top
			this.stack.push(value);
			return;
		} 
		// Push at the head in the deque
		this.deque.PushAtHead(value);
	}
	
	// Method to pop from stack or deque
	private Character pop() {
		// Return null termination character if the stack or deque is empty
		if(this.isEmpty()) return '\0';
		
		// If we want to use the stack
		if(BalancedBrackets.shouldUseStack) {
			// Pop the top of the stack and return it's contents
			return this.stack.pop();
		} 
		// Otherwise pop from the head of the queue and return the value
		return this.deque.RemoveAtHead();	
	}
	
	// Method to check if the stack or deque is empty
	private boolean isEmpty() {
		// If we want to use the stack
		if(BalancedBrackets.shouldUseStack) {
			// Return if the stack is empty
			return this.stack.isEmpty();
		} 
		// Otherwise check if the deque is empty
		return this.deque.IsEmpty();
	}
		
	// Method to remove comments from the program
	private String removeComments(String data) {
		// Remove multi line comments with '/**/'
		String temp = data.replaceAll("/\\*[^*]*\\*+(?:[^*/][^*]*\\*+)*/", "");
		// Remove single line comments with '//'
		temp = temp.replaceAll("//.*", "");
		return temp;
	}
	
	// Method to remove the characters in the string double quotes ""
	private String removeStringsAndChars(String data) {
		// Remove escape sequence \" as well as double quotes
		String temp = data.replaceAll("\"(?:\\\\.|[^\"])*\"", "");
		// Remove char between '' single quotes
		temp = temp.replaceAll("\'.*?\'", "");
		return temp;
	}
	
	// Method to check if the character is the opening bracket
	private boolean isOpeningBracket(Character value) {
		// For every character in the static array of opening paranthesis
		for(Character c : BalancedBrackets.openingParanthesis) {
			// Check if the value matches the character
			if(value == c) {
				return true;
			}
		}
		// Other wise not an opening bracket
		return false;
	}
	
	// Method to check if the character is a closing bracket
	private boolean isClosingBracket(Character value) {
		// For every character in the static array of opening paranthesis
		for(Character c : BalancedBrackets.closingParanthesis) {
			// Check if the value matches the character
			if(value == c) {
				return true;
			}
		}
		// Other wise not a closing bracket
		return false;
	}

	// Method to check if the opening and closing brackets are matching
	private boolean isMatchingBracket(Character opening, Character closing) {
			// Loop through the length of the brackets array
			for (int i = 0; i < BalancedBrackets.openingParanthesis.length; i++) {
				// Check if the opening bracket matches the opening character and the closing bracket matches the closing character
	            if (opening == openingParanthesis[i] && closing == closingParanthesis[i]) {
	                return true;
	            }
	        }
			// Otherwise return false
	        return false;
	}
	
}
