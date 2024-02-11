
public class Stack<T> {
	private List<T> stack;
	 
	public Stack() {
        this.stack = new List<T>();
    }

    public void push(T item) {
        this.stack.addFirst(item);
    }


    public T pop() {   
        return this.stack.removeFirst();
    }


    public T peek() {
        return this.stack.head().data;
    }


    public boolean isEmpty() {
        return this.stack.size() <= 0;
    }


    public int size() {
        return this.stack.size();
    }


    // public void clear() {
    //     top = null;
    //     size = 0;
    // }
}

