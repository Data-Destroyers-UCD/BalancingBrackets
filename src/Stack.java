// Stack class definition
public class Stack<T> {
	// Constructor for the Stack class
	public Stack() {
        // Initialise the stack as a singly linked list
        this.stack = new List<T>();
    }

    // Method to push data item to the stack (List)
    public void push(T item) {
        // Insert data item as the header
        this.stack.addFirst(item);
    }

    // Method to pop data from the stack (List)
    public T pop() {   
        // Remove the list header and return the data
        return this.stack.removeFirst();
    }

    // Method to get the data at the top of stack
    public T peek() {
        // Return the data contained in the list header
        return this.stack.head().data;
    }

    // Method to check if the list is empty
    public boolean isEmpty() {
        // Check if the list size is zero
        return this.size() == 0;
    }

    // Method to get the size of the array
    public int size() {
        // Get the size of the list
        return this.stack.size();
    }

    // Variable to create the stack as a list
    private List<T> stack;
}

