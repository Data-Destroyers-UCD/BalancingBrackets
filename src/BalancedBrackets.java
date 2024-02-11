
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BalancedBrackets {
	
	public BalancedBrackets() {
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

	            	System.out.print(b);
	            	System.out.print(c);
	                // If there is no corresponding opening bracket or it doesn't match, return false
	                return false;
	            }
	        }
	    }
			
		//System.out.println("Text with {} () []");
		//System.out.println(text);

		return this.isEmpty();
	}
	
	private static boolean shouldUseStack = true;
	private static char[] openingParanthesis = { '{', '[', '(' };
	private static char[] closingParanthesis = { '}', ']', ')'};
	
	private Deque<Character> deque;
	private Stack<Character> stack;
	
	private void push(Character value) {
		if(BalancedBrackets.shouldUseStack) {
			this.stack.push(value);
			return;
		} 
		
		this.deque.PushAtHead(value);
	}
	
	private Character pop() {
		if(this.isEmpty()) return '\0';
		
		if(BalancedBrackets.shouldUseStack) {
			return this.stack.pop();
		} 
			
		return this.deque.RemoveAtHead();	
	}
	
	private boolean isEmpty() {
		if(BalancedBrackets.shouldUseStack) {
			return this.stack.isEmpty();
		} 
		return this.deque.IsEmpty();
	}
		
	private String removeComments(String data) {
		// Remove multi line comments with '/**/'
		String temp = data.replaceAll("/\\*[^*]*\\*+(?:[^*/][^*]*\\*+)*/", "");
		// Remove single line comments with '//'
		temp = temp.replaceAll("//.*", "");
		return temp;
	}
	
	private String removeStringsAndChars(String data) {
		// Remove escape sequence \" as well as double quotes
		String temp = data.replaceAll("\"(?:\\\\.|[^\"])*\"", "");
		// Remove char between '' single quotes
		temp = temp.replaceAll("\'.*?\'", "");

		return temp;
	}
	
	private boolean isOpeningBracket(Character value) {
		for(Character c : BalancedBrackets.openingParanthesis) {
			if(value == c) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isClosingBracket(Character value) {
		for(Character c : BalancedBrackets.closingParanthesis) {
			if(value == c) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isMatchingBracket(Character opening, Character closing) {
		 for (int i = 0; i < BalancedBrackets.openingParanthesis.length; i++) {
	            if (opening == openingParanthesis[i] && closing == closingParanthesis[i]) {
	                return true;
	            }
	        }
	        return false;
	}
	
	
}
