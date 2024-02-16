// Deque class definition
public class Deque<T> {
	// Constructor for the Deque class
	public Deque() {
		// Initialise the queue linked list
		this.queue = new List<T>();
	}
	
	// Method for pushing data at the head of the list
	public void PushAtHead(T data) {
		this.queue.addFirst(data);
	}
	
	// Method for pushing the data at the tail of the list
	public void PushAtTail(T data) {
		this.queue.addLast(data);
	}
	
	// Method for removing the data node from the head
	public T RemoveAtHead() {
		return this.queue.removeFirst();
	}
	
	// Method for removing the data node from the tail
	public T RemoveAtTail() {
		return this.queue.removeLast();
	}
	
	// Method for getting the size of the list
	public int Size() {
		return this.queue.size();
	}
	
	// Method to check if the list is empty
	public boolean IsEmpty() {
		return this.queue.size() <= 0;
	}
	
	// Method to get the head node data
	public T HeadData() {
		return this.queue.head().data;
	}
	
	// Method to get the tail node data
	public T TailData() {
		return this.queue.tail().data;
	}
	
	// Queue as a Singly List Data structure
	private List<T> queue;
}
