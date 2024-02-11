
public class Stack<T> {
	private Node<T> top;
	private int size; 

	public Stack() {
        top = null;
        size = 0;
    }

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }


    public void push(T item) {
        Node<T> newNode = new Node<>(item);
        newNode.next = top;
        top = newNode;
        size++;
    }


    public T pop() {
        T item = top.data;
        top = top.next;
        size--;
        return item;
    }


    public T peek() {
        return top.data;
    }


    public boolean isEmpty() {
        return top == null;
    }


    public int size() {
        return size;
    }


    public void clear() {
        top = null;
        size = 0;
    }
}

