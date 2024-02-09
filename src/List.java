// Singly Linked List Implementation
public class List<T> {
	private ListNode head;
	private ListNode tail;
	private int length;
	
	public List() {
		this.head = null;
		this.tail = null;
		this.length = 0;
	}
	
	public class ListNode {
		public T data;
		public ListNode next;
		
		public ListNode(T data) {
			this.data = data;
			this.next = null;
		}
	}
	
	// O(1)
	public void addFirst(T data) {
		if(this.head == null || this.tail == null) {
			this.createHead(data);
		} else {
			ListNode newHead = new ListNode(data);
			newHead.next = this.head;
			this.head = newHead;
		}
		this.length++;
	}
	
	// O(1)
	public void addLast(T data) {
		if(this.head == null || this.tail == null) {
			this.createHead(data);
		} else {
			ListNode newTail = new ListNode(data);
			this.tail.next = newTail;
			this.tail = newTail;
		}
		this.length++;
	}
	
	// O(1)
	public T removeFirst() {
		if(this.head == null || this.tail == null) {
			return null;
		} else {
			T data = this.head.data;
			this.head = this.head.next;
			if(this.head == null) {
				this.tail = null;
			}
			this.length--;
			return data;
		}
	}
	
	// O(n)
	public T removeLast() {
		if(this.head == null && this.tail == null) {
			return null;
		} else {
			T data = this.tail.data;
			ListNode temp = this.head;
			while(temp.next != this.tail) temp = temp.next;
			this.tail = temp;
			temp.next = null;
			this.length--;
			return data;
		}
	}
	
	public int size() {
		return this.length;
	}
	
	private void createHead(T data) {
		this.head = new ListNode(data);
		this.tail = this.head;
	}
}
