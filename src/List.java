// Generic Singly Linked List class definition
public class List<T> {

	// Constructor for the singly linked list
	public List() {
		// Initialise the head
		this.head = null;
		// Initialise the tail
		this.tail = null;
		// Initialise the length to zero
		this.length = 0;
	}
	
	// Nested class List Node
	public class ListNode {
		// Generic Data
		public T data;
		// Reference for the next node
		public ListNode next;
		// Constructor for the List Node
		public ListNode(T data) {
			// Intialise the data
			this.data = data;
			// Initialise the next pointer to null
			this.next = null;
		}
	}
	
	// Add data to the list from the head
	// Time complexity: O(1)
	public void addFirst(T data) {
		// Check if the head or tail is null
		if(this.head == null || this.tail == null) {
			// Create a new head (along with the tail)
			this.createHead(data);
		} else { // Otherwise
			// Create a newNode for head
			ListNode newHead = new ListNode(data);
			// Set the new node's next pointer to the previous head
			newHead.next = this.head;
			// Set the head pointer to the new head
			this.head = newHead;
		}
		// Increase the length of the list
		this.length++;
	}
	
	// Add data to the list from the tail
	// Time complexity: O(1)
	public void addLast(T data) {
		// Check if the head or tail is null
		if(this.head == null || this.tail == null) {
			// Create a new head (along with the tail)
			this.createHead(data);
		} else { // Otherwise
			// Create a newNode for tail
			ListNode newTail = new ListNode(data);
			// Set the previous tail's next pointer to the new node
			this.tail.next = newTail;
			// Set the tail to the new node
			this.tail = newTail;
		}
		// Increase the size of the list
		this.length++;
	}
	
	// Method to Remove the head 
	// Time complexity: O(1)
	public T removeFirst() {
		// Check if the head or tail is null
		if(this.head == null || this.tail == null) {
			return null;
		} else {
			// Get the head's data
			T data = this.head.data;
			// Set the head to the head's next node
			this.head = this.head.next;
			// If the head is null, ie. the list is empty now
			if(this.head == null) {
				// Set the tail to null
				this.tail = null;
			}
			// Reduce the size of the list
			this.length--;
			// Return the data from the head
			return data;
		}
	}
	
	// Method to Remove the tail 
	// Time complexity: O(n)
	public T removeLast() {
		// Check if the head or tail is null
		if(this.head == null && this.tail == null) {
			return null;
		} else {
			// Get the tail's data
			T data = this.tail.data;
			// Get the head reference of the list
			ListNode temp = this.head;
			// Check if the list has more than 1 node
			if(temp.next != null) {
				// Visit the second last node in the list
				while(temp.next != this.tail) temp = temp.next;
				// Set the tail as the second last node
				this.tail = temp;
				// Set the next of the second last node to null
				temp.next = null;
			} else { // Otherwise
				// Set the head and the tail to null
				this.head = null;
				this.tail = null;
			}
			// Reduce the size of the list
			this.length--;
			// Return the data from the tail
			return data;
		}
	}
	
	// Method to get the length of the linked list
	public int size() {
		return this.length;
	}
	
	// Method to get the node reference of the head of the list
	public ListNode head() {
		return this.head;
	}
	
	// Method to get the node reference of the tail of the list
	public ListNode tail() {
		return this.tail;
	}
	
	// Linked List head pointer
	private ListNode head;
	// Linked List tail pointer
	private ListNode tail;
	// Linked List length
	private int length;
	
	// Method to create a new head if it doesn't exist
	private void createHead(T data) {
		// Create a new List Node with the data and set it as head
		this.head = new ListNode(data);
		// Set the tail as the head
		this.tail = this.head;
	}
	
}
