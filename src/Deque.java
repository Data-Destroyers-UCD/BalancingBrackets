
public class Deque<T> {
	private List<T> queue;
	
	public void PushAtHead(T data) {
		this.queue.addFirst(data);
	}
	
	public void PushAtTail(T data) {
		this.queue.addLast(data);
	}
	
	public T RemoveAtHead() {
		return this.queue.removeFirst();
	}
	
	public T RemoveAtTail() {
		return this.queue.removeLast();
	}
	
	public int Size() {
		return this.queue.size();
	}
	
	public boolean IsEmpty() {
		return this.queue.size() <= 0;
	}
	
	
}
