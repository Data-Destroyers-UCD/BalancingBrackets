/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import sun.misc.SharedSecrets;

/**
 * This class consists exclusively of static methods that operate on or return
 * collections.  It contains polymorphic algorithms that operate on
 * collections, "wrappers", which return a new collection backed by a
 * specified collection, and a few other odds and ends.
 *
 * <p>The methods of this class all throw a <tt>NullPointerException</tt>
 * if the collections or class objects provided to them are null.
 *
 * <p>The documentation for the polymorphic algorithms contained in this class
 * generally includes a brief description of the <i>implementation</i>.  Such
 * descriptions should be regarded as <i>implementation notes</i>, rather than
 * parts of the <i>specification</i>.  Implementors should feel free to
 * substitute other algorithms, so long as the specification itself is adhered
 * to.  (For example, the algorithm used by <tt>sort</tt> does not have to be
 * a mergesort, but it does have to be <i>stable</i>.)
 *
 * <p>The "destructive" algorithms contained in this class, that is, the
 * algorithms that modify the collection on which they operate, are specified
 * to throw <tt>UnsupportedOperationException</tt> if the collection does not
 * support the appropriate mutation primitive(s), such as the <tt>set</tt>
 * method.  These algorithms may, but are not required to, throw this
 * exception if an invocation would have no effect on the collection.  For
 * example, invoking the <tt>sort</tt> method on an unmodifiable list that is
 * already sorted may or may not throw <tt>UnsupportedOperationException</tt>.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     Set
 * @see     List
 * @see     Map
 * @since   1.2
 */

public class Collections {
    // Suppresses default constructor, ensuring non-instantiability.
    private Collections() {
    }

    // Algorithms

    /*
     * Tuning parameters for algorithms - Many of the List algorithms have
     * two implementations, one of which is appropriate for RandomAccess
     * lists, the other for "sequential."  Often, the random access variant
     * yields better performance on small sequential access lists.  The
     * tuning parameters below determine the cutoff point for what constitutes
     * a "small" sequential access list for each algorithm.  The values below
     * were empirically determined to work well for LinkedList. Hopefully
     * they should be reasonable for other sequential access List
     * implementations.  Those doing performance work on this code would
     * do well to validate the values of these parameters from time to time.
     * (The first word of each tuning parameter name is the algorithm to which
     * it applies.)
     */
    private static final int BINARYSEARCH_THRESHOLD   = 5000;
    private static final int REVERSE_THRESHOLD        =   18;
    private static final int SHUFFLE_THRESHOLD        =    5;
    private static final int FILL_THRESHOLD           =   25;
    private static final int ROTATE_THRESHOLD         =  100;
    private static final int COPY_THRESHOLD           =   10;
    private static final int REPLACEALL_THRESHOLD     =   11;
    private static final int INDEXOFSUBLIST_THRESHOLD =   35;

    /**
     * Sorts the specified list into ascending order, according to the
     * {@linkplain Comparable natural ordering} of its elements.
     * All elements in the list must implement the {@link Comparable}
     * interface.  Furthermore, all elements in the list must be
     * <i>mutually comparable</i> (that is, {@code e1.compareTo(e2)}
     * must not throw a {@code ClassCastException} for any elements
     * {@code e1} and {@code e2} in the list).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * <p>The specified list must be modifiable, but need not be resizable.
     *
     * @implNote
     * This implementation defers to the {@link List#sort(Comparator)}
     * method using the specified list and a {@code null} comparator.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list to be sorted.
     * @throws ClassCastException if the list contains elements that are not
     *         <i>mutually comparable</i> (for example, strings and integers).
     * @throws UnsupportedOperationException if the specified list's
     *         list-iterator does not support the {@code set} operation.
     * @throws IllegalArgumentException (optional) if the implementation
     *         detects that the natural ordering of the list elements is
     *         found to violate the {@link Comparable} contract
     * @see List#sort(Comparator)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        list.sort(null);
    }

    /**
     * Sorts the specified list according to the order induced by the
     * specified comparator.  All elements in the list must be <i>mutually
     * comparable</i> using the specified comparator (that is,
     * {@code c.compare(e1, e2)} must not throw a {@code ClassCastException}
     * for any elements {@code e1} and {@code e2} in the list).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * <p>The specified list must be modifiable, but need not be resizable.
     *
     * @implNote
     * This implementation defers to the {@link List#sort(Comparator)}
     * method using the specified list and comparator.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list to be sorted.
     * @param  c the comparator to determine the order of the list.  A
     *        {@code null} value indicates that the elements' <i>natural
     *        ordering</i> should be used.
     * @throws ClassCastException if the list contains elements that are not
     *         <i>mutually comparable</i> using the specified comparator.
     * @throws UnsupportedOperationException if the specified list's
     *         list-iterator does not support the {@code set} operation.
     * @throws IllegalArgumentException (optional) if the comparator is
     *         found to violate the {@link Comparator} contract
     * @see List#sort(Comparator)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void sort(List<T> list, Comparator<? super T> c) {
        list.sort(c);
    }


    /**
     * Searches the specified list for the specified object using the binary
     * search algorithm.  The list must be sorted into ascending order
     * according to the {@linkplain Comparable natural ordering} of its
     * elements (as by the {@link #sort(List)} method) prior to making this
     * call.  If it is not sorted, the results are undefined.  If the list
     * contains multiple elements equal to the specified object, there is no
     * guarantee which one will be found.
     *
     * <p>This method runs in log(n) time for a "random access" list (which
     * provides near-constant-time positional access).  If the specified list
     * does not implement the {@link RandomAccess} interface and is large,
     * this method will do an iterator-based binary search that performs
     * O(n) link traversals and O(log n) element comparisons.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list to be searched.
     * @param  key the key to be searched for.
     * @return the index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt> if all
     *         elements in the list are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ClassCastException if the list contains elements that are not
     *         <i>mutually comparable</i> (for example, strings and
     *         integers), or the search key is not mutually comparable
     *         with the elements of the list.
     */
    public static <T>
    int binarySearch(List<? extends Comparable<? super T>> list, T key) {
        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return Collections.indexedBinarySearch(list, key);
        else
            return Collections.iteratorBinarySearch(list, key);
    }

    private static <T>
    int indexedBinarySearch(List<? extends Comparable<? super T>> list, T key) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = list.get(mid);
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    private static <T>
    int iteratorBinarySearch(List<? extends Comparable<? super T>> list, T key)
    {
        int low = 0;
        int high = list.size()-1;
        ListIterator<? extends Comparable<? super T>> i = list.listIterator();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = get(i, mid);
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    /**
     * Gets the ith element from the given list by repositioning the specified
     * list listIterator.
     */
    private static <T> T get(ListIterator<? extends T> i, int index) {
        T obj = null;
        int pos = i.nextIndex();
        if (pos <= index) {
            do {
                obj = i.next();
            } while (pos++ < index);
        } else {
            do {
                obj = i.previous();
            } while (--pos > index);
        }
        return obj;
    }

    /**
     * Searches the specified list for the specified object using the binary
     * search algorithm.  The list must be sorted into ascending order
     * according to the specified comparator (as by the
     * {@link #sort(List, Comparator) sort(List, Comparator)}
     * method), prior to making this call.  If it is
     * not sorted, the results are undefined.  If the list contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     *
     * <p>This method runs in log(n) time for a "random access" list (which
     * provides near-constant-time positional access).  If the specified list
     * does not implement the {@link RandomAccess} interface and is large,
     * this method will do an iterator-based binary search that performs
     * O(n) link traversals and O(log n) element comparisons.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list to be searched.
     * @param  key the key to be searched for.
     * @param  c the comparator by which the list is ordered.
     *         A <tt>null</tt> value indicates that the elements'
     *         {@linkplain Comparable natural ordering} should be used.
     * @return the index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt> if all
     *         elements in the list are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ClassCastException if the list contains elements that are not
     *         <i>mutually comparable</i> using the specified comparator,
     *         or the search key is not mutually comparable with the
     *         elements of the list using this comparator.
     */
    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c) {
        if (c==null)
            return binarySearch((List<? extends Comparable<? super T>>) list, key);

        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return Collections.indexedBinarySearch(list, key, c);
        else
            return Collections.iteratorBinarySearch(list, key, c);
    }

    private static <T> int indexedBinarySearch(List<? extends T> l, T key, Comparator<? super T> c) {
        int low = 0;
        int high = l.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = l.get(mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    private static <T> int iteratorBinarySearch(List<? extends T> l, T key, Comparator<? super T> c) {
        int low = 0;
        int high = l.size()-1;
        ListIterator<? extends T> i = l.listIterator();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = get(i, mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    /**
     * Reverses the order of the elements in the specified list.<p>
     *
     * This method runs in linear time.
     *
     * @param  list the list whose elements are to be reversed.
     * @throws UnsupportedOperationException if the specified list or
     *         its list-iterator does not support the <tt>set</tt> operation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void reverse(List<?> list) {
        int size = list.size();
        if (size < REVERSE_THRESHOLD || list instanceof RandomAccess) {
            for (int i=0, mid=size>>1, j=size-1; i<mid; i++, j--)
                swap(list, i, j);
        } else {
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator fwd = list.listIterator();
            ListIterator rev = list.listIterator(size);
            for (int i=0, mid=list.size()>>1; i<mid; i++) {
                Object tmp = fwd.next();
                fwd.set(rev.previous());
                rev.set(tmp);
            }
        }
    }

    /**
     * Randomly permutes the specified list using a default source of
     * randomness.  All permutations occur with approximately equal
     * likelihood.
     *
     * <p>The hedge "approximately" is used in the foregoing description because
     * default source of randomness is only approximately an unbiased source
     * of independently chosen bits. If it were a perfect source of randomly
     * chosen bits, then the algorithm would choose permutations with perfect
     * uniformity.
     *
     * <p>This implementation traverses the list backwards, from the last
     * element up to the second, repeatedly swapping a randomly selected element
     * into the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.
     *
     * <p>This method runs in linear time.  If the specified list does not
     * implement the {@link RandomAccess} interface and is large, this
     * implementation dumps the specified list into an array before shuffling
     * it, and dumps the shuffled array back into the list.  This avoids the
     * quadratic behavior that would result from shuffling a "sequential
     * access" list in place.
     *
     * @param  list the list to be shuffled.
     * @throws UnsupportedOperationException if the specified list or
     *         its list-iterator does not support the <tt>set</tt> operation.
     */
    public static void shuffle(List<?> list) {
        Random rnd = r;
        if (rnd == null)
            r = rnd = new Random(); // harmless race.
        shuffle(list, rnd);
    }

    private static Random r;

    /**
     * Randomly permute the specified list using the specified source of
     * randomness.  All permutations occur with equal likelihood
     * assuming that the source of randomness is fair.<p>
     *
     * This implementation traverses the list backwards, from the last element
     * up to the second, repeatedly swapping a randomly selected element into
     * the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.<p>
     *
     * This method runs in linear time.  If the specified list does not
     * implement the {@link RandomAccess} interface and is large, this
     * implementation dumps the specified list into an array before shuffling
     * it, and dumps the shuffled array back into the list.  This avoids the
     * quadratic behavior that would result from shuffling a "sequential
     * access" list in place.
     *
     * @param  list the list to be shuffled.
     * @param  rnd the source of randomness to use to shuffle the list.
     * @throws UnsupportedOperationException if the specified list or its
     *         list-iterator does not support the <tt>set</tt> operation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void shuffle(List<?> list, Random rnd) {
        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
            for (int i=size; i>1; i--)
                swap(list, i-1, rnd.nextInt(i));
        } else {
            Object[] arr = list.toArray();

            // Shuffle array
            for (int i=size; i>1; i--)
                swap(arr, i-1, rnd.nextInt(i));

            // Dump array back into list
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator it = list.listIterator();
            for (int i=0; i<arr.length; i++) {
                it.next();
                it.set(arr[i]);
            }
        }
    }

    /**
     * Swaps the elements at the specified positions in the specified list.
     * (If the specified positions are equal, invoking this method leaves
     * the list unchanged.)
     *
     * @param list The list in which to swap elements.
     * @param i the index of one element to be swapped.
     * @param j the index of the other element to be swapped.
     * @throws IndexOutOfBoundsException if either <tt>i</tt> or <tt>j</tt>
     *         is out of range (i &lt; 0 || i &gt;= list.size()
     *         || j &lt; 0 || j &gt;= list.size()).
     * @since 1.4
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void swap(List<?> list, int i, int j) {
        // instead of using a raw type here, it's possible to capture
        // the wildcard but it will require a call to a supplementary
        // private method
        final List l = list;
        l.set(i, l.set(j, l.get(i)));
    }

    /**
     * Swaps the two specified elements in the specified array.
     */
    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * Replaces all of the elements of the specified list with the specified
     * element. <p>
     *
     * This method runs in linear time.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list to be filled with the specified element.
     * @param  obj The element with which to fill the specified list.
     * @throws UnsupportedOperationException if the specified list or its
     *         list-iterator does not support the <tt>set</tt> operation.
     */
    public static <T> void fill(List<? super T> list, T obj) {
        int size = list.size();

        if (size < FILL_THRESHOLD || list instanceof RandomAccess) {
            for (int i=0; i<size; i++)
                list.set(i, obj);
        } else {
            ListIterator<? super T> itr = list.listIterator();
            for (int i=0; i<size; i++) {
                itr.next();
                itr.set(obj);
            }
        }
    }

    /**
     * Copies all of the elements from one list into another.  After the
     * operation, the index of each copied element in the destination list
     * will be identical to its index in the source list.  The destination
     * list must be at least as long as the source list.  If it is longer, the
     * remaining elements in the destination list are unaffected. <p>
     *
     * This method runs in linear time.
     *
     * @param  <T> the class of the objects in the lists
     * @param  dest The destination list.
     * @param  src The source list.
     * @throws IndexOutOfBoundsException if the destination list is too small
     *         to contain the entire source List.
     * @throws UnsupportedOperationException if the destination list's
     *         list-iterator does not support the <tt>set</tt> operation.
     */
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        int srcSize = src.size();
        if (srcSize > dest.size())
            throw new IndexOutOfBoundsException("Source does not fit in dest");

        if (srcSize < COPY_THRESHOLD ||
            (src instanceof RandomAccess && dest instanceof RandomAccess)) {
            for (int i=0; i<srcSize; i++)
                dest.set(i, src.get(i));
        } else {
            ListIterator<? super T> di=dest.listIterator();
            ListIterator<? extends T> si=src.listIterator();
            for (int i=0; i<srcSize; i++) {
                di.next();
                di.set(si.next());
            }
        }
    }

    /**
     * Returns the minimum element of the given collection, according to the
     * <i>natural ordering</i> of its elements.  All elements in the
     * collection must implement the <tt>Comparable</tt> interface.
     * Furthermore, all elements in the collection must be <i>mutually
     * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  <T> the class of the objects in the collection
     * @param  coll the collection whose minimum element is to be determined.
     * @return the minimum element of the given collection, according
     *         to the <i>natural ordering</i> of its elements.
     * @throws ClassCastException if the collection contains elements that are
     *         not <i>mutually comparable</i> (for example, strings and
     *         integers).
     * @throws NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    public static <T extends Object & Comparable<? super T>> T min(Collection<? extends T> coll) {
        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (next.compareTo(candidate) < 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * Returns the minimum element of the given collection, according to the
     * order induced by the specified comparator.  All elements in the
     * collection must be <i>mutually comparable</i> by the specified
     * comparator (that is, <tt>comp.compare(e1, e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  <T> the class of the objects in the collection
     * @param  coll the collection whose minimum element is to be determined.
     * @param  comp the comparator with which to determine the minimum element.
     *         A <tt>null</tt> value indicates that the elements' <i>natural
     *         ordering</i> should be used.
     * @return the minimum element of the given collection, according
     *         to the specified comparator.
     * @throws ClassCastException if the collection contains elements that are
     *         not <i>mutually comparable</i> using the specified comparator.
     * @throws NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T min(Collection<? extends T> coll, Comparator<? super T> comp) {
        if (comp==null)
            return (T)min((Collection) coll);

        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (comp.compare(next, candidate) < 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * Returns the maximum element of the given collection, according to the
     * <i>natural ordering</i> of its elements.  All elements in the
     * collection must implement the <tt>Comparable</tt> interface.
     * Furthermore, all elements in the collection must be <i>mutually
     * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  <T> the class of the objects in the collection
     * @param  coll the collection whose maximum element is to be determined.
     * @return the maximum element of the given collection, according
     *         to the <i>natural ordering</i> of its elements.
     * @throws ClassCastException if the collection contains elements that are
     *         not <i>mutually comparable</i> (for example, strings and
     *         integers).
     * @throws NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    public static <T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll) {
        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (next.compareTo(candidate) > 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * Returns the maximum element of the given collection, according to the
     * order induced by the specified comparator.  All elements in the
     * collection must be <i>mutually comparable</i> by the specified
     * comparator (that is, <tt>comp.compare(e1, e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  <T> the class of the objects in the collection
     * @param  coll the collection whose maximum element is to be determined.
     * @param  comp the comparator with which to determine the maximum element.
     *         A <tt>null</tt> value indicates that the elements' <i>natural
     *        ordering</i> should be used.
     * @return the maximum element of the given collection, according
     *         to the specified comparator.
     * @throws ClassCastException if the collection contains elements that are
     *         not <i>mutually comparable</i> using the specified comparator.
     * @throws NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T max(Collection<? extends T> coll, Comparator<? super T> comp) {
        if (comp==null)
            return (T)max((Collection) coll);

        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (comp.compare(next, candidate) > 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * Rotates the elements in the specified list by the specified distance.
     * After calling this method, the element at index <tt>i</tt> will be
     * the element previously at index <tt>(i - distance)</tt> mod
     * <tt>list.size()</tt>, for all values of <tt>i</tt> between <tt>0</tt>
     * and <tt>list.size()-1</tt>, inclusive.  (This method has no effect on
     * the size of the list.)
     *
     * <p>For example, suppose <tt>list</tt> comprises<tt> [t, a, n, k, s]</tt>.
     * After invoking <tt>Collections.rotate(list, 1)</tt> (or
     * <tt>Collections.rotate(list, -4)</tt>), <tt>list</tt> will comprise
     * <tt>[s, t, a, n, k]</tt>.
     *
     * <p>Note that this method can usefully be applied to sublists to
     * move one or more elements within a list while preserving the
     * order of the remaining elements.  For example, the following idiom
     * moves the element at index <tt>j</tt> forward to position
     * <tt>k</tt> (which must be greater than or equal to <tt>j</tt>):
     * <pre>
     *     Collections.rotate(list.subList(j, k+1), -1);
     * </pre>
     * To make this concrete, suppose <tt>list</tt> comprises
     * <tt>[a, b, c, d, e]</tt>.  To move the element at index <tt>1</tt>
     * (<tt>b</tt>) forward two positions, perform the following invocation:
     * <pre>
     *     Collections.rotate(l.subList(1, 4), -1);
     * </pre>
     * The resulting list is <tt>[a, c, d, b, e]</tt>.
     *
     * <p>To move more than one element forward, increase the absolute value
     * of the rotation distance.  To move elements backward, use a positive
     * shift distance.
     *
     * <p>If the specified list is small or implements the {@link
     * RandomAccess} interface, this implementation exchanges the first
     * element into the location it should go, and then repeatedly exchanges
     * the displaced element into the location it should go until a displaced
     * element is swapped into the first element.  If necessary, the process
     * is repeated on the second and successive elements, until the rotation
     * is complete.  If the specified list is large and doesn't implement the
     * <tt>RandomAccess</tt> interface, this implementation breaks the
     * list into two sublist views around index <tt>-distance mod size</tt>.
     * Then the {@link #reverse(List)} method is invoked on each sublist view,
     * and finally it is invoked on the entire list.  For a more complete
     * description of both algorithms, see Section 2.3 of Jon Bentley's
     * <i>Programming Pearls</i> (Addison-Wesley, 1986).
     *
     * @param list the list to be rotated.
     * @param distance the distance to rotate the list.  There are no
     *        constraints on this value; it may be zero, negative, or
     *        greater than <tt>list.size()</tt>.
     * @throws UnsupportedOperationException if the specified list or
     *         its list-iterator does not support the <tt>set</tt> operation.
     * @since 1.4
     */
    public static void rotate(List<?> list, int distance) {
        if (list instanceof RandomAccess || list.size() < ROTATE_THRESHOLD)
            rotate1(list, distance);
        else
            rotate2(list, distance);
    }

    private static <T> void rotate1(List<T> list, int distance) {
        int size = list.size();
        if (size == 0)
            return;
        distance = distance % size;
        if (distance < 0)
            distance += size;
        if (distance == 0)
            return;

        for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
            T displaced = list.get(cycleStart);
            int i = cycleStart;
            do {
                i += distance;
                if (i >= size)
                    i -= size;
                displaced = list.set(i, displaced);
                nMoved ++;
            } while (i != cycleStart);
        }
    }

    private static void rotate2(List<?> list, int distance) {
        int size = list.size();
        if (size == 0)
            return;
        int mid =  -distance % size;
        if (mid < 0)
            mid += size;
        if (mid == 0)
            return;

        reverse(list.subList(0, mid));
        reverse(list.subList(mid, size));
        reverse(list);
    }

    /**
     * Replaces all occurrences of one specified value in a list with another.
     * More formally, replaces with <tt>newVal</tt> each element <tt>e</tt>
     * in <tt>list</tt> such that
     * <tt>(oldVal==null ? e==null : oldVal.equals(e))</tt>.
     * (This method has no effect on the size of the list.)
     *
     * @param  <T> the class of the objects in the list
     * @param list the list in which replacement is to occur.
     * @param oldVal the old value to be replaced.
     * @param newVal the new value with which <tt>oldVal</tt> is to be
     *        replaced.
     * @return <tt>true</tt> if <tt>list</tt> contained one or more elements
     *         <tt>e</tt> such that
     *         <tt>(oldVal==null ?  e==null : oldVal.equals(e))</tt>.
     * @throws UnsupportedOperationException if the specified list or
     *         its list-iterator does not support the <tt>set</tt> operation.
     * @since  1.4
     */
    public static <T> boolean replaceAll(List<T> list, T oldVal, T newVal) {
        boolean result = false;
        int size = list.size();
        if (size < REPLACEALL_THRESHOLD || list instanceof RandomAccess) {
            if (oldVal==null) {
                for (int i=0; i<size; i++) {
                    if (list.get(i)==null) {
                        list.set(i, newVal);
                        result = true;
                    }
                }
            } else {
                for (int i=0; i<size; i++) {
                    if (oldVal.equals(list.get(i))) {
                        list.set(i, newVal);
                        result = true;
                    }
                }
            }
        } else {
            ListIterator<T> itr=list.listIterator();
            if (oldVal==null) {
                for (int i=0; i<size; i++) {
                    if (itr.next()==null) {
                        itr.set(newVal);
                        result = true;
                    }
                }
            } else {
                for (int i=0; i<size; i++) {
                    if (oldVal.equals(itr.next())) {
                        itr.set(newVal);
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the starting position of the first occurrence of the specified
     * target list within the specified source list, or -1 if there is no
     * such occurrence.  More formally, returns the lowest index <tt>i</tt>
     * such that {@code source.subList(i, i+target.size()).equals(target)},
     * or -1 if there is no such index.  (Returns -1 if
     * {@code target.size() > source.size()})
     *
     * <p>This implementation uses the "brute force" technique of scanning
     * over the source list, looking for a match with the target at each
     * location in turn.
     *
     * @param source the list in which to search for the first occurrence
     *        of <tt>target</tt>.
     * @param target the list to search for as a subList of <tt>source</tt>.
     * @return the starting position of the first occurrence of the specified
     *         target list within the specified source list, or -1 if there
     *         is no such occurrence.
     * @since  1.4
     */
    public static int indexOfSubList(List<?> source, List<?> target) {
        int sourceSize = source.size();
        int targetSize = target.size();
        int maxCandidate = sourceSize - targetSize;

        if (sourceSize < INDEXOFSUBLIST_THRESHOLD ||
            (source instanceof RandomAccess&&target instanceof RandomAccess)) {
        nextCand:
            for (int candidate = 0; candidate <= maxCandidate; candidate++) {
                for (int i=0, j=candidate; i<targetSize; i++, j++)
                    if (!eq(target.get(i), source.get(j)))
                        continue nextCand;  // Element mismatch, try next cand
                return candidate;  // All elements of candidate matched target
            }
        } else {  // Iterator version of above algorithm
            ListIterator<?> si = source.listIterator();
        nextCand:
            for (int candidate = 0; candidate <= maxCandidate; candidate++) {
                ListIterator<?> ti = target.listIterator();
                for (int i=0; i<targetSize; i++) {
                    if (!eq(ti.next(), si.next())) {
                        // Back up source iterator to next candidate
                        for (int j=0; j<i; j++)
                            si.previous();
                        continue nextCand;
                    }
                }
                return candidate;
            }
        }
        return -1;  // No candidate matched the target
    }

    /**
     * Returns the starting position of the last occurrence of the specified
     * target list within the specified source list, or -1 if there is no such
     * occurrence.  More formally, returns the highest index <tt>i</tt>
     * such that {@code source.subList(i, i+target.size()).equals(target)},
     * or -1 if there is no such index.  (Returns -1 if
     * {@code target.size() > source.size()})
     *
     * <p>This implementation uses the "brute force" technique of iterating
     * over the source list, looking for a match with the target at each
     * location in turn.
     *
     * @param source the list in which to search for the last occurrence
     *        of <tt>target</tt>.
     * @param target the list to search for as a subList of <tt>source</tt>.
     * @return the starting position of the last occurrence of the specified
     *         target list within the specified source list, or -1 if there
     *         is no such occurrence.
     * @since  1.4
     */
    public static int lastIndexOfSubList(List<?> source, List<?> target) {
        int sourceSize = source.size();
        int targetSize = target.size();
        int maxCandidate = sourceSize - targetSize;

        if (sourceSize < INDEXOFSUBLIST_THRESHOLD ||
            source instanceof RandomAccess) {   // Index access version
        nextCand:
            for (int candidate = maxCandidate; candidate >= 0; candidate--) {
                for (int i=0, j=candidate; i<targetSize; i++, j++)
                    if (!eq(target.get(i), source.get(j)))
                        continue nextCand;  // Element mismatch, try next cand
                return candidate;  // All elements of candidate matched target
            }
        } else {  // Iterator version of above algorithm
            if (maxCandidate < 0)
                return -1;
            ListIterator<?> si = source.listIterator(maxCandidate);
        nextCand:
            for (int candidate = maxCandidate; candidate >= 0; candidate--) {
                ListIterator<?> ti = target.listIterator();
                for (int i=0; i<targetSize; i++) {
                    if (!eq(ti.next(), si.next())) {
                        if (candidate != 0) {
                            // Back up source iterator to next candidate
                            for (int j=0; j<=i+1; j++)
                                si.previous();
                        }
                        continue nextCand;
                    }
                }
                return candidate;
            }
        }
        return -1;  // No candidate matched the target
    }


    // Unmodifiable Wrappers

    /**
     * Returns an unmodifiable view of the specified collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  <T> the class of the objects in the collection
     * @param  c the collection for which an unmodifiable view is to be
     *         returned.
     * @return an unmodifiable view of the specified collection.
     */
    public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> c) {
        return new UnmodifiableCollection<>(c);
    }

    /**
     * @serial include
     */
    static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 1820017752578914078L;

        final Collection<? extends E> c;

        UnmodifiableCollection(Collection<? extends E> c) {
            if (c==null)
                throw new NullPointerException();
            this.c = c;
        }

        public int size()                   {return c.size();}
        public boolean isEmpty()            {return c.isEmpty();}
        public boolean contains(Object o)   {return c.contains(o);}
        public Object[] toArray()           {return c.toArray();}
        public <T> T[] toArray(T[] a)       {return c.toArray(a);}
        public String toString()            {return c.toString();}

        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private final Iterator<? extends E> i = c.iterator();

                public boolean hasNext() {return i.hasNext();}
                public E next()          {return i.next();}
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    // Use backing collection version
                    i.forEachRemaining(action);
                }
            };
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> coll) {
            return c.containsAll(coll);
        }
        public boolean addAll(Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }
        public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
        public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            c.forEach(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Spliterator<E> spliterator() {
            return (Spliterator<E>)c.spliterator();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> stream() {
            return (Stream<E>)c.stream();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> parallelStream() {
            return (Stream<E>)c.parallelStream();
        }
    }

    /**
     * Returns an unmodifiable view of the specified set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified set.
     */
    public static <T> Set<T> unmodifiableSet(Set<? extends T> s) {
        return new UnmodifiableSet<>(s);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSet<E> extends UnmodifiableCollection<E>
                                 implements Set<E>, Serializable {
        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet(Set<? extends E> s)     {super(s);}
        public boolean equals(Object o) {return o == this || c.equals(o);}
        public int hashCode()           {return c.hashCode();}
    }

    /**
     * Returns an unmodifiable view of the specified sorted set.  This method
     * allows modules to provide users with "read-only" access to internal
     * sorted sets.  Query operations on the returned sorted set "read
     * through" to the specified sorted set.  Attempts to modify the returned
     * sorted set, whether direct, via its iterator, or via its
     * <tt>subSet</tt>, <tt>headSet</tt>, or <tt>tailSet</tt> views, result in
     * an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned sorted set will be serializable if the specified sorted set
     * is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param s the sorted set for which an unmodifiable view is to be
     *        returned.
     * @return an unmodifiable view of the specified sorted set.
     */
    public static <T> SortedSet<T> unmodifiableSortedSet(SortedSet<T> s) {
        return new UnmodifiableSortedSet<>(s);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSortedSet<E>
                             extends UnmodifiableSet<E>
                             implements SortedSet<E>, Serializable {
        private static final long serialVersionUID = -4929149591599911165L;
        private final SortedSet<E> ss;

        UnmodifiableSortedSet(SortedSet<E> s) {super(s); ss = s;}

        public Comparator<? super E> comparator() {return ss.comparator();}

        public SortedSet<E> subSet(E fromElement, E toElement) {
            return new UnmodifiableSortedSet<>(ss.subSet(fromElement,toElement));
        }
        public SortedSet<E> headSet(E toElement) {
            return new UnmodifiableSortedSet<>(ss.headSet(toElement));
        }
        public SortedSet<E> tailSet(E fromElement) {
            return new UnmodifiableSortedSet<>(ss.tailSet(fromElement));
        }

        public E first()                   {return ss.first();}
        public E last()                    {return ss.last();}
    }

    /**
     * Returns an unmodifiable view of the specified navigable set.  This method
     * allows modules to provide users with "read-only" access to internal
     * navigable sets.  Query operations on the returned navigable set "read
     * through" to the specified navigable set.  Attempts to modify the returned
     * navigable set, whether direct, via its iterator, or via its
     * {@code subSet}, {@code headSet}, or {@code tailSet} views, result in
     * an {@code UnsupportedOperationException}.<p>
     *
     * The returned navigable set will be serializable if the specified
     * navigable set is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param s the navigable set for which an unmodifiable view is to be
     *        returned
     * @return an unmodifiable view of the specified navigable set
     * @since 1.8
     */
    public static <T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s) {
        return new UnmodifiableNavigableSet<>(s);
    }

    /**
     * Wraps a navigable set and disables all of the mutative operations.
     *
     * @param <E> type of elements
     * @serial include
     */
    static class UnmodifiableNavigableSet<E>
                             extends UnmodifiableSortedSet<E>
                             implements NavigableSet<E>, Serializable {

        private static final long serialVersionUID = -6027448201786391929L;

        /**
         * A singleton empty unmodifiable navigable set used for
         * {@link #emptyNavigableSet()}.
         *
         * @param <E> type of elements, if there were any, and bounds
         */
        private static class EmptyNavigableSet<E> extends UnmodifiableNavigableSet<E>
            implements Serializable {
            private static final long serialVersionUID = -6291252904449939134L;

            public EmptyNavigableSet() {
                super(new TreeSet<E>());
            }

            private Object readResolve()        { return EMPTY_NAVIGABLE_SET; }
        }

        @SuppressWarnings("rawtypes")
        private static final NavigableSet<?> EMPTY_NAVIGABLE_SET =
                new EmptyNavigableSet<>();

        /**
         * The instance we are protecting.
         */
        private final NavigableSet<E> ns;

        UnmodifiableNavigableSet(NavigableSet<E> s)         {super(s); ns = s;}

        public E lower(E e)                             { return ns.lower(e); }
        public E floor(E e)                             { return ns.floor(e); }
        public E ceiling(E e)                         { return ns.ceiling(e); }
        public E higher(E e)                           { return ns.higher(e); }
        public E pollFirst()     { throw new UnsupportedOperationException(); }
        public E pollLast()      { throw new UnsupportedOperationException(); }
        public NavigableSet<E> descendingSet()
                 { return new UnmodifiableNavigableSet<>(ns.descendingSet()); }
        public Iterator<E> descendingIterator()
                                         { return descendingSet().iterator(); }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.headSet(toElement, inclusive));
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.tailSet(fromElement, inclusive));
        }
    }

    /**
     * Returns an unmodifiable view of the specified list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static <T> List<T> unmodifiableList(List<? extends T> list) {
        return (list instanceof RandomAccess ?
                new UnmodifiableRandomAccessList<>(list) :
                new UnmodifiableList<>(list));
    }

    /**
     * @serial include
     */
    static class UnmodifiableList<E> extends UnmodifiableCollection<E>
                                  implements List<E> {
        private static final long serialVersionUID = -283967356065247728L;

        final List<? extends E> list;

        UnmodifiableList(List<? extends E> list) {
            super(list);
            this.list = list;
        }

        public boolean equals(Object o) {return o == this || list.equals(o);}
        public int hashCode()           {return list.hashCode();}

        public E get(int index) {return list.get(index);}
        public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }
        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }
        public E remove(int index) {
            throw new UnsupportedOperationException();
        }
        public int indexOf(Object o)            {return list.indexOf(o);}
        public int lastIndexOf(Object o)        {return list.lastIndexOf(o);}
        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void sort(Comparator<? super E> c) {
            throw new UnsupportedOperationException();
        }

        public ListIterator<E> listIterator()   {return listIterator(0);}

        public ListIterator<E> listIterator(final int index) {
            return new ListIterator<E>() {
                private final ListIterator<? extends E> i
                    = list.listIterator(index);

                public boolean hasNext()     {return i.hasNext();}
                public E next()              {return i.next();}
                public boolean hasPrevious() {return i.hasPrevious();}
                public E previous()          {return i.previous();}
                public int nextIndex()       {return i.nextIndex();}
                public int previousIndex()   {return i.previousIndex();}

                public void remove() {
                    throw new UnsupportedOperationException();
                }
                public void set(E e) {
                    throw new UnsupportedOperationException();
                }
                public void add(E e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    i.forEachRemaining(action);
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new UnmodifiableList<>(list.subList(fromIndex, toIndex));
        }

        /**
         * UnmodifiableRandomAccessList instances are serialized as
         * UnmodifiableList instances to allow them to be deserialized
         * in pre-1.4 JREs (which do not have UnmodifiableRandomAccessList).
         * This method inverts the transformation.  As a beneficial
         * side-effect, it also grafts the RandomAccess marker onto
         * UnmodifiableList instances that were serialized in pre-1.4 JREs.
         *
         * Note: Unfortunately, UnmodifiableRandomAccessList instances
         * serialized in 1.4.1 and deserialized in 1.4 will become
         * UnmodifiableList instances, as this method was missing in 1.4.
         */
        private Object readResolve() {
            return (list instanceof RandomAccess
                    ? new UnmodifiableRandomAccessList<>(list)
                    : this);
        }
    }

    /**
     * @serial include
     */
    static class UnmodifiableRandomAccessList<E> extends UnmodifiableList<E>
                                              implements RandomAccess
    {
        UnmodifiableRandomAccessList(List<? extends E> list) {
            super(list);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new UnmodifiableRandomAccessList<>(
                list.subList(fromIndex, toIndex));
        }

        private static final long serialVersionUID = -2542308836966382001L;

        /**
         * Allows instances to be deserialized in pre-1.4 JREs (which do
         * not have UnmodifiableRandomAccessList).  UnmodifiableList has
         * a readResolve method that inverts this transformation upon
         * deserialization.
         */
        private Object writeReplace() {
            return new UnmodifiableList<>(list);
        }
    }

    /**
     * Returns an unmodifiable view of the specified map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified map.
     */
    public static <K,V> Map<K,V> unmodifiableMap(Map<? extends K, ? extends V> m) {
        return new UnmodifiableMap<>(m);
    }

    /**
     * @serial include
     */
    private static class UnmodifiableMap<K,V> implements Map<K,V>, Serializable {
        private static final long serialVersionUID = -1034234728574286014L;

        private final Map<? extends K, ? extends V> m;

        UnmodifiableMap(Map<? extends K, ? extends V> m) {
            if (m==null)
                throw new NullPointerException();
            this.m = m;
        }

        public int size()                        {return m.size();}
        public boolean isEmpty()                 {return m.isEmpty();}
        public boolean containsKey(Object key)   {return m.containsKey(key);}
        public boolean containsValue(Object val) {return m.containsValue(val);}
        public V get(Object key)                 {return m.get(key);}

        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }

        private transient Set<K> keySet;
        private transient Set<Map.Entry<K,V>> entrySet;
        private transient Collection<V> values;

        public Set<K> keySet() {
            if (keySet==null)
                keySet = unmodifiableSet(m.keySet());
            return keySet;
        }

        public Set<Map.Entry<K,V>> entrySet() {
            if (entrySet==null)
                entrySet = new UnmodifiableEntrySet<>(m.entrySet());
            return entrySet;
        }

        public Collection<V> values() {
            if (values==null)
                values = unmodifiableCollection(m.values());
            return values;
        }

        public boolean equals(Object o) {return o == this || m.equals(o);}
        public int hashCode()           {return m.hashCode();}
        public String toString()        {return m.toString();}

        // Override default methods in Map
        @Override
        @SuppressWarnings("unchecked")
        public V getOrDefault(Object k, V defaultValue) {
            // Safe cast as we don't change the value
            return ((Map<K, V>)m).getOrDefault(k, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            m.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        /**
         * We need this class in addition to UnmodifiableSet as
         * Map.Entries themselves permit modification of the backing Map
         * via their setValue operation.  This class is subtle: there are
         * many possible attacks that must be thwarted.
         *
         * @serial include
         */
        static class UnmodifiableEntrySet<K,V>
            extends UnmodifiableSet<Map.Entry<K,V>> {
            private static final long serialVersionUID = 7854390611657943733L;

            @SuppressWarnings({"unchecked", "rawtypes"})
            UnmodifiableEntrySet(Set<? extends Map.Entry<? extends K, ? extends V>> s) {
                // Need to cast to raw in order to work around a limitation in the type system
                super((Set)s);
            }

            static <K, V> Consumer<Map.Entry<K, V>> entryConsumer(Consumer<? super Entry<K, V>> action) {
                return e -> action.accept(new UnmodifiableEntry<>(e));
            }

            public void forEach(Consumer<? super Entry<K, V>> action) {
                Objects.requireNonNull(action);
                c.forEach(entryConsumer(action));
            }

            static final class UnmodifiableEntrySetSpliterator<K, V>
                    implements Spliterator<Entry<K,V>> {
                final Spliterator<Map.Entry<K, V>> s;

                UnmodifiableEntrySetSpliterator(Spliterator<Entry<K, V>> s) {
                    this.s = s;
                }

                @Override
                public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
                    Objects.requireNonNull(action);
                    return s.tryAdvance(entryConsumer(action));
                }

                @Override
                public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                    Objects.requireNonNull(action);
                    s.forEachRemaining(entryConsumer(action));
                }

                @Override
                public Spliterator<Entry<K, V>> trySplit() {
                    Spliterator<Entry<K, V>> split = s.trySplit();
                    return split == null
                           ? null
                           : new UnmodifiableEntrySetSpliterator<>(split);
                }

                @Override
                public long estimateSize() {
                    return s.estimateSize();
                }

                @Override
                public long getExactSizeIfKnown() {
                    return s.getExactSizeIfKnown();
                }

                @Override
                public int characteristics() {
                    return s.characteristics();
                }

                @Override
                public boolean hasCharacteristics(int characteristics) {
                    return s.hasCharacteristics(characteristics);
                }

                @Override
                public Comparator<? super Entry<K, V>> getComparator() {
                    return s.getComparator();
                }
            }

            @SuppressWarnings("unchecked")
            public Spliterator<Entry<K,V>> spliterator() {
                return new UnmodifiableEntrySetSpliterator<>(
                        (Spliterator<Map.Entry<K, V>>) c.spliterator());
            }

            @Override
            public Stream<Entry<K,V>> stream() {
                return StreamSupport.stream(spliterator(), false);
            }

            @Override
            public Stream<Entry<K,V>> parallelStream() {
                return StreamSupport.stream(spliterator(), true);
            }

            public Iterator<Map.Entry<K,V>> iterator() {
                return new Iterator<Map.Entry<K,V>>() {
                    private final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = c.iterator();

                    public boolean hasNext() {
                        return i.hasNext();
                    }
                    public Map.Entry<K,V> next() {
                        return new UnmodifiableEntry<>(i.next());
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @SuppressWarnings("unchecked")
            public Object[] toArray() {
                Object[] a = c.toArray();
                for (int i=0; i<a.length; i++)
                    a[i] = new UnmodifiableEntry<>((Map.Entry<? extends K, ? extends V>)a[i]);
                return a;
            }

            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] a) {
                // We don't pass a to c.toArray, to avoid window of
                // vulnerability wherein an unscrupulous multithreaded client
                // could get his hands on raw (unwrapped) Entries from c.
                Object[] arr = c.toArray(a.length==0 ? a : Arrays.copyOf(a, 0));

                for (int i=0; i<arr.length; i++)
                    arr[i] = new UnmodifiableEntry<>((Map.Entry<? extends K, ? extends V>)arr[i]);

                if (arr.length > a.length)
                    return (T[])arr;

                System.arraycopy(arr, 0, a, 0, arr.length);
                if (a.length > arr.length)
                    a[arr.length] = null;
                return a;
            }

            /**
             * This method is overridden to protect the backing set against
             * an object with a nefarious equals function that senses
             * that the equality-candidate is Map.Entry and calls its
             * setValue method.
             */
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                return c.contains(
                    new UnmodifiableEntry<>((Map.Entry<?,?>) o));
            }

            /**
             * The next two methods are overridden to protect against
             * an unscrupulous List whose contains(Object o) method senses
             * when o is a Map.Entry, and calls o.setValue.
             */
            public boolean containsAll(Collection<?> coll) {
                for (Object e : coll) {
                    if (!contains(e)) // Invokes safe contains() above
                        return false;
                }
                return true;
            }
            public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;
                Set<?> s = (Set<?>) o;
                if (s.size() != c.size())
                    return false;
                return containsAll(s); // Invokes safe containsAll() above
            }

            /**
             * This "wrapper class" serves two purposes: it prevents
             * the client from modifying the backing Map, by short-circuiting
             * the setValue method, and it protects the backing Map against
             * an ill-behaved Map.Entry that attempts to modify another
             * Map Entry when asked to perform an equality check.
             */
            private static class UnmodifiableEntry<K,V> implements Map.Entry<K,V> {
                private Map.Entry<? extends K, ? extends V> e;

                UnmodifiableEntry(Map.Entry<? extends K, ? extends V> e)
                        {this.e = Objects.requireNonNull(e);}

                public K getKey()        {return e.getKey();}
                public V getValue()      {return e.getValue();}
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
                public int hashCode()    {return e.hashCode();}
                public boolean equals(Object o) {
                    if (this == o)
                        return true;
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry<?,?> t = (Map.Entry<?,?>)o;
                    return eq(e.getKey(),   t.getKey()) &&
                           eq(e.getValue(), t.getValue());
                }
                public String toString() {return e.toString();}
            }
        }
    }

    /**
     * Returns an unmodifiable view of the specified sorted map.  This method
     * allows modules to provide users with "read-only" access to internal
     * sorted maps.  Query operations on the returned sorted map "read through"
     * to the specified sorted map.  Attempts to modify the returned
     * sorted map, whether direct, via its collection views, or via its
     * <tt>subMap</tt>, <tt>headMap</tt>, or <tt>tailMap</tt> views, result in
     * an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned sorted map will be serializable if the specified sorted map
     * is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param m the sorted map for which an unmodifiable view is to be
     *        returned.
     * @return an unmodifiable view of the specified sorted map.
     */
    public static <K,V> SortedMap<K,V> unmodifiableSortedMap(SortedMap<K, ? extends V> m) {
        return new UnmodifiableSortedMap<>(m);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSortedMap<K,V>
          extends UnmodifiableMap<K,V>
          implements SortedMap<K,V>, Serializable {
        private static final long serialVersionUID = -8806743815996713206L;

        private final SortedMap<K, ? extends V> sm;

        UnmodifiableSortedMap(SortedMap<K, ? extends V> m) {super(m); sm = m; }
        public Comparator<? super K> comparator()   { return sm.comparator(); }
        public SortedMap<K,V> subMap(K fromKey, K toKey)
             { return new UnmodifiableSortedMap<>(sm.subMap(fromKey, toKey)); }
        public SortedMap<K,V> headMap(K toKey)
                     { return new UnmodifiableSortedMap<>(sm.headMap(toKey)); }
        public SortedMap<K,V> tailMap(K fromKey)
                   { return new UnmodifiableSortedMap<>(sm.tailMap(fromKey)); }
        public K firstKey()                           { return sm.firstKey(); }
        public K lastKey()                             { return sm.lastKey(); }
    }

    /**
     * Returns an unmodifiable view of the specified navigable map.  This method
     * allows modules to provide users with "read-only" access to internal
     * navigable maps.  Query operations on the returned navigable map "read
     * through" to the specified navigable map.  Attempts to modify the returned
     * navigable map, whether direct, via its collection views, or via its
     * {@code subMap}, {@code headMap}, or {@code tailMap} views, result in
     * an {@code UnsupportedOperationException}.<p>
     *
     * The returned navigable map will be serializable if the specified
     * navigable map is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param m the navigable map for which an unmodifiable view is to be
     *        returned
     * @return an unmodifiable view of the specified navigable map
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> m) {
        return new UnmodifiableNavigableMap<>(m);
    }

    /**
     * @serial include
     */
    static class UnmodifiableNavigableMap<K,V>
          extends UnmodifiableSortedMap<K,V>
          implements NavigableMap<K,V>, Serializable {
        private static final long serialVersionUID = -4858195264774772197L;

        /**
         * A class for the {@link EMPTY_NAVIGABLE_MAP} which needs readResolve
         * to preserve singleton property.
         *
         * @param <K> type of keys, if there were any, and of bounds
         * @param <V> type of values, if there were any
         */
        private static class EmptyNavigableMap<K,V> extends UnmodifiableNavigableMap<K,V>
            implements Serializable {

            private static final long serialVersionUID = -2239321462712562324L;

            EmptyNavigableMap()                       { super(new TreeMap<K,V>()); }

            @Override
            public NavigableSet<K> navigableKeySet()
                                                { return emptyNavigableSet(); }

            private Object readResolve()        { return EMPTY_NAVIGABLE_MAP; }
        }

        /**
         * Singleton for {@link emptyNavigableMap()} which is also immutable.
         */
        private static final EmptyNavigableMap<?,?> EMPTY_NAVIGABLE_MAP =
            new EmptyNavigableMap<>();

        /**
         * The instance we wrap and protect.
         */
        private final NavigableMap<K, ? extends V> nm;

        UnmodifiableNavigableMap(NavigableMap<K, ? extends V> m)
                                                            {super(m); nm = m;}

        public K lowerKey(K key)                   { return nm.lowerKey(key); }
        public K floorKey(K key)                   { return nm.floorKey(key); }
        public K ceilingKey(K key)               { return nm.ceilingKey(key); }
        public K higherKey(K key)                 { return nm.higherKey(key); }

        @SuppressWarnings("unchecked")
        public Entry<K, V> lowerEntry(K key) {
            Entry<K,V> lower = (Entry<K, V>) nm.lowerEntry(key);
            return (null != lower)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(lower)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> floorEntry(K key) {
            Entry<K,V> floor = (Entry<K, V>) nm.floorEntry(key);
            return (null != floor)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(floor)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> ceilingEntry(K key) {
            Entry<K,V> ceiling = (Entry<K, V>) nm.ceilingEntry(key);
            return (null != ceiling)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(ceiling)
                : null;
        }


        @SuppressWarnings("unchecked")
        public Entry<K, V> higherEntry(K key) {
            Entry<K,V> higher = (Entry<K, V>) nm.higherEntry(key);
            return (null != higher)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(higher)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> firstEntry() {
            Entry<K,V> first = (Entry<K, V>) nm.firstEntry();
            return (null != first)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(first)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> lastEntry() {
            Entry<K,V> last = (Entry<K, V>) nm.lastEntry();
            return (null != last)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(last)
                : null;
        }

        public Entry<K, V> pollFirstEntry()
                                 { throw new UnsupportedOperationException(); }
        public Entry<K, V> pollLastEntry()
                                 { throw new UnsupportedOperationException(); }
        public NavigableMap<K, V> descendingMap()
                       { return unmodifiableNavigableMap(nm.descendingMap()); }
        public NavigableSet<K> navigableKeySet()
                     { return unmodifiableNavigableSet(nm.navigableKeySet()); }
        public NavigableSet<K> descendingKeySet()
                    { return unmodifiableNavigableSet(nm.descendingKeySet()); }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return unmodifiableNavigableMap(
                nm.subMap(fromKey, fromInclusive, toKey, toInclusive));
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive)
             { return unmodifiableNavigableMap(nm.headMap(toKey, inclusive)); }
        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
           { return unmodifiableNavigableMap(nm.tailMap(fromKey, inclusive)); }
    }

    // Synch Wrappers

    /**
     * Returns a synchronized (thread-safe) collection backed by the specified
     * collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when traversing it via {@link Iterator}, {@link Spliterator}
     * or {@link Stream}:
     * <pre>
     *  Collection c = Collections.synchronizedCollection(myCollection);
     *     ...
     *  synchronized (c) {
     *      Iterator i = c.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *         foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the {@code hashCode}
     * and {@code equals} operations through to the backing collection, but
     * relies on {@code Object}'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  <T> the class of the objects in the collection
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static <T> Collection<T> synchronizedCollection(Collection<T> c) {
        return new SynchronizedCollection<>(c);
    }

    static <T> Collection<T> synchronizedCollection(Collection<T> c, Object mutex) {
        return new SynchronizedCollection<>(c, mutex);
    }

    /**
     * @serial include
     */
    static class SynchronizedCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 3053995032091335093L;

        final Collection<E> c;  // Backing Collection
        final Object mutex;     // Object on which to synchronize

        SynchronizedCollection(Collection<E> c) {
            this.c = Objects.requireNonNull(c);
            mutex = this;
        }

        SynchronizedCollection(Collection<E> c, Object mutex) {
            this.c = Objects.requireNonNull(c);
            this.mutex = Objects.requireNonNull(mutex);
        }

        public int size() {
            synchronized (mutex) {return c.size();}
        }
        public boolean isEmpty() {
            synchronized (mutex) {return c.isEmpty();}
        }
        public boolean contains(Object o) {
            synchronized (mutex) {return c.contains(o);}
        }
        public Object[] toArray() {
            synchronized (mutex) {return c.toArray();}
        }
        public <T> T[] toArray(T[] a) {
            synchronized (mutex) {return c.toArray(a);}
        }

        public Iterator<E> iterator() {
            return c.iterator(); // Must be manually synched by user!
        }

        public boolean add(E e) {
            synchronized (mutex) {return c.add(e);}
        }
        public boolean remove(Object o) {
            synchronized (mutex) {return c.remove(o);}
        }

        public boolean containsAll(Collection<?> coll) {
            synchronized (mutex) {return c.containsAll(coll);}
        }
        public boolean addAll(Collection<? extends E> coll) {
            synchronized (mutex) {return c.addAll(coll);}
        }
        public boolean removeAll(Collection<?> coll) {
            synchronized (mutex) {return c.removeAll(coll);}
        }
        public boolean retainAll(Collection<?> coll) {
            synchronized (mutex) {return c.retainAll(coll);}
        }
        public void clear() {
            synchronized (mutex) {c.clear();}
        }
        public String toString() {
            synchronized (mutex) {return c.toString();}
        }
        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> consumer) {
            synchronized (mutex) {c.forEach(consumer);}
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            synchronized (mutex) {return c.removeIf(filter);}
        }
        @Override
        public Spliterator<E> spliterator() {
            return c.spliterator(); // Must be manually synched by user!
        }
        @Override
        public Stream<E> stream() {
            return c.stream(); // Must be manually synched by user!
        }
        @Override
        public Stream<E> parallelStream() {
            return c.parallelStream(); // Must be manually synched by user!
        }
        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {s.defaultWriteObject();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  Set s = Collections.synchronizedSet(new HashSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static <T> Set<T> synchronizedSet(Set<T> s) {
        return new SynchronizedSet<>(s);
    }

    static <T> Set<T> synchronizedSet(Set<T> s, Object mutex) {
        return new SynchronizedSet<>(s, mutex);
    }

    /**
     * @serial include
     */
    static class SynchronizedSet<E>
          extends SynchronizedCollection<E>
          implements Set<E> {
        private static final long serialVersionUID = 487447009682186044L;

        SynchronizedSet(Set<E> s) {
            super(s);
        }
        SynchronizedSet(Set<E> s, Object mutex) {
            super(s, mutex);
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return c.equals(o);}
        }
        public int hashCode() {
            synchronized (mutex) {return c.hashCode();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) sorted set backed by the specified
     * sorted set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing sorted set is accomplished
     * through the returned sorted set (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * sorted set when iterating over it or any of its <tt>subSet</tt>,
     * <tt>headSet</tt>, or <tt>tailSet</tt> views.
     * <pre>
     *  SortedSet s = Collections.synchronizedSortedSet(new TreeSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  SortedSet s = Collections.synchronizedSortedSet(new TreeSet());
     *  SortedSet s2 = s.headSet(foo);
     *      ...
     *  synchronized (s) {  // Note: s, not s2!!!
     *      Iterator i = s2.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned sorted set will be serializable if the specified
     * sorted set is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param  s the sorted set to be "wrapped" in a synchronized sorted set.
     * @return a synchronized view of the specified sorted set.
     */
    public static <T> SortedSet<T> synchronizedSortedSet(SortedSet<T> s) {
        return new SynchronizedSortedSet<>(s);
    }

    /**
     * @serial include
     */
    static class SynchronizedSortedSet<E>
        extends SynchronizedSet<E>
        implements SortedSet<E>
    {
        private static final long serialVersionUID = 8695801310862127406L;

        private final SortedSet<E> ss;

        SynchronizedSortedSet(SortedSet<E> s) {
            super(s);
            ss = s;
        }
        SynchronizedSortedSet(SortedSet<E> s, Object mutex) {
            super(s, mutex);
            ss = s;
        }

        public Comparator<? super E> comparator() {
            synchronized (mutex) {return ss.comparator();}
        }

        public SortedSet<E> subSet(E fromElement, E toElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<>(
                    ss.subSet(fromElement, toElement), mutex);
            }
        }
        public SortedSet<E> headSet(E toElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<>(ss.headSet(toElement), mutex);
            }
        }
        public SortedSet<E> tailSet(E fromElement) {
            synchronized (mutex) {
               return new SynchronizedSortedSet<>(ss.tailSet(fromElement),mutex);
            }
        }

        public E first() {
            synchronized (mutex) {return ss.first();}
        }
        public E last() {
            synchronized (mutex) {return ss.last();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) navigable set backed by the
     * specified navigable set.  In order to guarantee serial access, it is
     * critical that <strong>all</strong> access to the backing navigable set is
     * accomplished through the returned navigable set (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * navigable set when iterating over it or any of its {@code subSet},
     * {@code headSet}, or {@code tailSet} views.
     * <pre>
     *  NavigableSet s = Collections.synchronizedNavigableSet(new TreeSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  NavigableSet s = Collections.synchronizedNavigableSet(new TreeSet());
     *  NavigableSet s2 = s.headSet(foo, true);
     *      ...
     *  synchronized (s) {  // Note: s, not s2!!!
     *      Iterator i = s2.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned navigable set will be serializable if the specified
     * navigable set is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param  s the navigable set to be "wrapped" in a synchronized navigable
     * set
     * @return a synchronized view of the specified navigable set
     * @since 1.8
     */
    public static <T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s) {
        return new SynchronizedNavigableSet<>(s);
    }

    /**
     * @serial include
     */
    static class SynchronizedNavigableSet<E>
        extends SynchronizedSortedSet<E>
        implements NavigableSet<E>
    {
        private static final long serialVersionUID = -5505529816273629798L;

        private final NavigableSet<E> ns;

        SynchronizedNavigableSet(NavigableSet<E> s) {
            super(s);
            ns = s;
        }

        SynchronizedNavigableSet(NavigableSet<E> s, Object mutex) {
            super(s, mutex);
            ns = s;
        }
        public E lower(E e)      { synchronized (mutex) {return ns.lower(e);} }
        public E floor(E e)      { synchronized (mutex) {return ns.floor(e);} }
        public E ceiling(E e)  { synchronized (mutex) {return ns.ceiling(e);} }
        public E higher(E e)    { synchronized (mutex) {return ns.higher(e);} }
        public E pollFirst()  { synchronized (mutex) {return ns.pollFirst();} }
        public E pollLast()    { synchronized (mutex) {return ns.pollLast();} }

        public NavigableSet<E> descendingSet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.descendingSet(), mutex);
            }
        }

        public Iterator<E> descendingIterator()
                 { synchronized (mutex) { return descendingSet().iterator(); } }

        public NavigableSet<E> subSet(E fromElement, E toElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.subSet(fromElement, true, toElement, false), mutex);
            }
        }
        public NavigableSet<E> headSet(E toElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.headSet(toElement, false), mutex);
            }
        }
        public NavigableSet<E> tailSet(E fromElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.tailSet(fromElement, true), mutex);
            }
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), mutex);
            }
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.headSet(toElement, inclusive), mutex);
            }
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.tailSet(fromElement, inclusive), mutex);
            }
        }
    }

    /**
     * Returns a synchronized (thread-safe) list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  List list = Collections.synchronizedList(new ArrayList());
     *      ...
     *  synchronized (list) {
     *      Iterator i = list.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static <T> List<T> synchronizedList(List<T> list) {
        return (list instanceof RandomAccess ?
                new SynchronizedRandomAccessList<>(list) :
                new SynchronizedList<>(list));
    }

    static <T> List<T> synchronizedList(List<T> list, Object mutex) {
        return (list instanceof RandomAccess ?
                new SynchronizedRandomAccessList<>(list, mutex) :
                new SynchronizedList<>(list, mutex));
    }

    /**
     * @serial include
     */
    static class SynchronizedList<E>
        extends SynchronizedCollection<E>
        implements List<E> {
        private static final long serialVersionUID = -7754090372962971524L;

        final List<E> list;

        SynchronizedList(List<E> list) {
            super(list);
            this.list = list;
        }
        SynchronizedList(List<E> list, Object mutex) {
            super(list, mutex);
            this.list = list;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return list.equals(o);}
        }
        public int hashCode() {
            synchronized (mutex) {return list.hashCode();}
        }

        public E get(int index) {
            synchronized (mutex) {return list.get(index);}
        }
        public E set(int index, E element) {
            synchronized (mutex) {return list.set(index, element);}
        }
        public void add(int index, E element) {
            synchronized (mutex) {list.add(index, element);}
        }
        public E remove(int index) {
            synchronized (mutex) {return list.remove(index);}
        }

        public int indexOf(Object o) {
            synchronized (mutex) {return list.indexOf(o);}
        }
        public int lastIndexOf(Object o) {
            synchronized (mutex) {return list.lastIndexOf(o);}
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            synchronized (mutex) {return list.addAll(index, c);}
        }

        public ListIterator<E> listIterator() {
            return list.listIterator(); // Must be manually synched by user
        }

        public ListIterator<E> listIterator(int index) {
            return list.listIterator(index); // Must be manually synched by user
        }

        public List<E> subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedList<>(list.subList(fromIndex, toIndex),
                                            mutex);
            }
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            synchronized (mutex) {list.replaceAll(operator);}
        }
        @Override
        public void sort(Comparator<? super E> c) {
            synchronized (mutex) {list.sort(c);}
        }

        /**
         * SynchronizedRandomAccessList instances are serialized as
         * SynchronizedList instances to allow them to be deserialized
         * in pre-1.4 JREs (which do not have SynchronizedRandomAccessList).
         * This method inverts the transformation.  As a beneficial
         * side-effect, it also grafts the RandomAccess marker onto
         * SynchronizedList instances that were serialized in pre-1.4 JREs.
         *
         * Note: Unfortunately, SynchronizedRandomAccessList instances
         * serialized in 1.4.1 and deserialized in 1.4 will become
         * SynchronizedList instances, as this method was missing in 1.4.
         */
        private Object readResolve() {
            return (list instanceof RandomAccess
                    ? new SynchronizedRandomAccessList<>(list)
                    : this);
        }
    }

    /**
     * @serial include
     */
    static class SynchronizedRandomAccessList<E>
        extends SynchronizedList<E>
        implements RandomAccess {

        SynchronizedRandomAccessList(List<E> list) {
            super(list);
        }

        SynchronizedRandomAccessList(List<E> list, Object mutex) {
            super(list, mutex);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedRandomAccessList<>(
                    list.subList(fromIndex, toIndex), mutex);
            }
        }

        private static final long serialVersionUID = 1530674583602358482L;

        /**
         * Allows instances to be deserialized in pre-1.4 JREs (which do
         * not have SynchronizedRandomAccessList).  SynchronizedList has
         * a readResolve method that inverts this transformation upon
         * deserialization.
         */
        private Object writeReplace() {
            return new SynchronizedList<>(list);
        }
    }

    /**
     * Returns a synchronized (thread-safe) map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  Map m = Collections.synchronizedMap(new HashMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) {
        return new SynchronizedMap<>(m);
    }

    /**
     * @serial include
     */
    private static class SynchronizedMap<K,V>
        implements Map<K,V>, Serializable {
        private static final long serialVersionUID = 1978198479659022715L;

        private final Map<K,V> m;     // Backing Map
        final Object      mutex;        // Object on which to synchronize

        SynchronizedMap(Map<K,V> m) {
            this.m = Objects.requireNonNull(m);
            mutex = this;
        }

        SynchronizedMap(Map<K,V> m, Object mutex) {
            this.m = m;
            this.mutex = mutex;
        }

        public int size() {
            synchronized (mutex) {return m.size();}
        }
        public boolean isEmpty() {
            synchronized (mutex) {return m.isEmpty();}
        }
        public boolean containsKey(Object key) {
            synchronized (mutex) {return m.containsKey(key);}
        }
        public boolean containsValue(Object value) {
            synchronized (mutex) {return m.containsValue(value);}
        }
        public V get(Object key) {
            synchronized (mutex) {return m.get(key);}
        }

        public V put(K key, V value) {
            synchronized (mutex) {return m.put(key, value);}
        }
        public V remove(Object key) {
            synchronized (mutex) {return m.remove(key);}
        }
        public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (mutex) {m.putAll(map);}
        }
        public void clear() {
            synchronized (mutex) {m.clear();}
        }

        private transient Set<K> keySet;
        private transient Set<Map.Entry<K,V>> entrySet;
        private transient Collection<V> values;

        public Set<K> keySet() {
            synchronized (mutex) {
                if (keySet==null)
                    keySet = new SynchronizedSet<>(m.keySet(), mutex);
                return keySet;
            }
        }

        public Set<Map.Entry<K,V>> entrySet() {
            synchronized (mutex) {
                if (entrySet==null)
                    entrySet = new SynchronizedSet<>(m.entrySet(), mutex);
                return entrySet;
            }
        }

        public Collection<V> values() {
            synchronized (mutex) {
                if (values==null)
                    values = new SynchronizedCollection<>(m.values(), mutex);
                return values;
            }
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return m.equals(o);}
        }
        public int hashCode() {
            synchronized (mutex) {return m.hashCode();}
        }
        public String toString() {
            synchronized (mutex) {return m.toString();}
        }

        // Override default methods in Map
        @Override
        public V getOrDefault(Object k, V defaultValue) {
            synchronized (mutex) {return m.getOrDefault(k, defaultValue);}
        }
        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            synchronized (mutex) {m.forEach(action);}
        }
        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            synchronized (mutex) {m.replaceAll(function);}
        }
        @Override
        public V putIfAbsent(K key, V value) {
            synchronized (mutex) {return m.putIfAbsent(key, value);}
        }
        @Override
        public boolean remove(Object key, Object value) {
            synchronized (mutex) {return m.remove(key, value);}
        }
        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            synchronized (mutex) {return m.replace(key, oldValue, newValue);}
        }
        @Override
        public V replace(K key, V value) {
            synchronized (mutex) {return m.replace(key, value);}
        }
        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            synchronized (mutex) {return m.computeIfAbsent(key, mappingFunction);}
        }
        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            synchronized (mutex) {return m.computeIfPresent(key, remappingFunction);}
        }
        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            synchronized (mutex) {return m.compute(key, remappingFunction);}
        }
        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            synchronized (mutex) {return m.merge(key, value, remappingFunction);}
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {s.defaultWriteObject();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) sorted map backed by the specified
     * sorted map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing sorted map is accomplished
     * through the returned sorted map (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * sorted map when iterating over any of its collection views, or the
     * collections views of any of its <tt>subMap</tt>, <tt>headMap</tt> or
     * <tt>tailMap</tt> views.
     * <pre>
     *  SortedMap m = Collections.synchronizedSortedMap(new TreeMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  SortedMap m = Collections.synchronizedSortedMap(new TreeMap());
     *  SortedMap m2 = m.subMap(foo, bar);
     *      ...
     *  Set s2 = m2.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not m2 or s2!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned sorted map will be serializable if the specified
     * sorted map is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param  m the sorted map to be "wrapped" in a synchronized sorted map.
     * @return a synchronized view of the specified sorted map.
     */
    public static <K,V> SortedMap<K,V> synchronizedSortedMap(SortedMap<K,V> m) {
        return new SynchronizedSortedMap<>(m);
    }

    /**
     * @serial include
     */
    static class SynchronizedSortedMap<K,V>
        extends SynchronizedMap<K,V>
        implements SortedMap<K,V>
    {
        private static final long serialVersionUID = -8798146769416483793L;

        private final SortedMap<K,V> sm;

        SynchronizedSortedMap(SortedMap<K,V> m) {
            super(m);
            sm = m;
        }
        SynchronizedSortedMap(SortedMap<K,V> m, Object mutex) {
            super(m, mutex);
            sm = m;
        }

        public Comparator<? super K> comparator() {
            synchronized (mutex) {return sm.comparator();}
        }

        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<>(
                    sm.subMap(fromKey, toKey), mutex);
            }
        }
        public SortedMap<K,V> headMap(K toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<>(sm.headMap(toKey), mutex);
            }
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            synchronized (mutex) {
               return new SynchronizedSortedMap<>(sm.tailMap(fromKey),mutex);
            }
        }

        public K firstKey() {
            synchronized (mutex) {return sm.firstKey();}
        }
        public K lastKey() {
            synchronized (mutex) {return sm.lastKey();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) navigable map backed by the
     * specified navigable map.  In order to guarantee serial access, it is
     * critical that <strong>all</strong> access to the backing navigable map is
     * accomplished through the returned navigable map (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * navigable map when iterating over any of its collection views, or the
     * collections views of any of its {@code subMap}, {@code headMap} or
     * {@code tailMap} views.
     * <pre>
     *  NavigableMap m = Collections.synchronizedNavigableMap(new TreeMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  NavigableMap m = Collections.synchronizedNavigableMap(new TreeMap());
     *  NavigableMap m2 = m.subMap(foo, true, bar, false);
     *      ...
     *  Set s2 = m2.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not m2 or s2!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned navigable map will be serializable if the specified
     * navigable map is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param  m the navigable map to be "wrapped" in a synchronized navigable
     *              map
     * @return a synchronized view of the specified navigable map.
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> synchronizedNavigableMap(NavigableMap<K,V> m) {
        return new SynchronizedNavigableMap<>(m);
    }

    /**
     * A synchronized NavigableMap.
     *
     * @serial include
     */
    static class SynchronizedNavigableMap<K,V>
        extends SynchronizedSortedMap<K,V>
        implements NavigableMap<K,V>
    {
        private static final long serialVersionUID = 699392247599746807L;

        private final NavigableMap<K,V> nm;

        SynchronizedNavigableMap(NavigableMap<K,V> m) {
            super(m);
            nm = m;
        }
        SynchronizedNavigableMap(NavigableMap<K,V> m, Object mutex) {
            super(m, mutex);
            nm = m;
        }

        public Entry<K, V> lowerEntry(K key)
                        { synchronized (mutex) { return nm.lowerEntry(key); } }
        public K lowerKey(K key)
                          { synchronized (mutex) { return nm.lowerKey(key); } }
        public Entry<K, V> floorEntry(K key)
                        { synchronized (mutex) { return nm.floorEntry(key); } }
        public K floorKey(K key)
                          { synchronized (mutex) { return nm.floorKey(key); } }
        public Entry<K, V> ceilingEntry(K key)
                      { synchronized (mutex) { return nm.ceilingEntry(key); } }
        public K ceilingKey(K key)
                        { synchronized (mutex) { return nm.ceilingKey(key); } }
        public Entry<K, V> higherEntry(K key)
                       { synchronized (mutex) { return nm.higherEntry(key); } }
        public K higherKey(K key)
                         { synchronized (mutex) { return nm.higherKey(key); } }
        public Entry<K, V> firstEntry()
                           { synchronized (mutex) { return nm.firstEntry(); } }
        public Entry<K, V> lastEntry()
                            { synchronized (mutex) { return nm.lastEntry(); } }
        public Entry<K, V> pollFirstEntry()
                       { synchronized (mutex) { return nm.pollFirstEntry(); } }
        public Entry<K, V> pollLastEntry()
                        { synchronized (mutex) { return nm.pollLastEntry(); } }

        public NavigableMap<K, V> descendingMap() {
            synchronized (mutex) {
                return
                    new SynchronizedNavigableMap<>(nm.descendingMap(), mutex);
            }
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(nm.navigableKeySet(), mutex);
            }
        }

        public NavigableSet<K> descendingKeySet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(nm.descendingKeySet(), mutex);
            }
        }


        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.subMap(fromKey, true, toKey, false), mutex);
            }
        }
        public SortedMap<K,V> headMap(K toKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(nm.headMap(toKey, false), mutex);
            }
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            synchronized (mutex) {
        return new SynchronizedNavigableMap<>(nm.tailMap(fromKey, true),mutex);
            }
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.subMap(fromKey, fromInclusive, toKey, toInclusive), mutex);
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                        nm.headMap(toKey, inclusive), mutex);
            }
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.tailMap(fromKey, inclusive), mutex);
            }
        }
    }

    // Dynamically typesafe collection wrappers

    /**
     * Returns a dynamically typesafe view of the specified collection.
     * Any attempt to insert an element of the wrong type will result in an
     * immediate {@link ClassCastException}.  Assuming a collection
     * contains no incorrectly typed elements prior to the time a
     * dynamically typesafe view is generated, and that all subsequent
     * access to the collection takes place through the view, it is
     * <i>guaranteed</i> that the collection cannot contain an incorrectly
     * typed element.
     *
     * <p>The generics mechanism in the language provides compile-time
     * (static) type checking, but it is possible to defeat this mechanism
     * with unchecked casts.  Usually this is not a problem, as the compiler
     * issues warnings on all such unchecked operations.  There are, however,
     * times when static type checking alone is not sufficient.  For example,
     * suppose a collection is passed to a third-party library and it is
     * imperative that the library code not corrupt the collection by
     * inserting an element of the wrong type.
     *
     * <p>Another use of dynamically typesafe views is debugging.  Suppose a
     * program fails with a {@code ClassCastException}, indicating that an
     * incorrectly typed element was put into a parameterized collection.
     * Unfortunately, the exception can occur at any time after the erroneous
     * element is inserted, so it typically provides little or no information
     * as to the real source of the problem.  If the problem is reproducible,
     * one can quickly determine its source by temporarily modifying the
     * program to wrap the collection with a dynamically typesafe view.
     * For example, this declaration:
     *  <pre> {@code
     *     Collection<String> c = new HashSet<>();
     * }</pre>
     * may be replaced temporarily by this one:
     *  <pre> {@code
     *     Collection<String> c = Collections.checkedCollection(
     *         new HashSet<>(), String.class);
     * }</pre>
     * Running the program again will cause it to fail at the point where
     * an incorrectly typed element is inserted into the collection, clearly
     * identifying the source of the problem.  Once the problem is fixed, the
     * modified declaration may be reverted back to the original.
     *
     * <p>The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * {@code Object}'s {@code equals} and {@code hashCode} methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.
     *
     * <p>The returned collection will be serializable if the specified
     * collection is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned collection permits insertion of null elements
     * whenever the backing collection does.
     *
     * @param <E> the class of the objects in the collection
     * @param c the collection for which a dynamically typesafe view is to be
     *          returned
     * @param type the type of element that {@code c} is permitted to hold
     * @return a dynamically typesafe view of the specified collection
     * @since 1.5
     */
    public static <E> Collection<E> checkedCollection(Collection<E> c,
                                                      Class<E> type) {
        return new CheckedCollection<>(c, type);
    }

    @SuppressWarnings("unchecked")
    static <T> T[] zeroLengthArray(Class<T> type) {
        return (T[]) Array.newInstance(type, 0);
    }

    /**
     * @serial include
     */
    static class CheckedCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 1578914078182001775L;

        final Collection<E> c;
        final Class<E> type;

        @SuppressWarnings("unchecked")
        E typeCheck(Object o) {
            if (o != null && !type.isInstance(o))
                throw new ClassCastException(badElementMsg(o));
            return (E) o;
        }

        private String badElementMsg(Object o) {
            return "Attempt to insert " + o.getClass() +
                " element into collection with element type " + type;
        }

        CheckedCollection(Collection<E> c, Class<E> type) {
            this.c = Objects.requireNonNull(c, "c");
            this.type = Objects.requireNonNull(type, "type");
        }

        public int size()                 { return c.size(); }
        public boolean isEmpty()          { return c.isEmpty(); }
        public boolean contains(Object o) { return c.contains(o); }
        public Object[] toArray()         { return c.toArray(); }
        public <T> T[] toArray(T[] a)     { return c.toArray(a); }
        public String toString()          { return c.toString(); }
        public boolean remove(Object o)   { return c.remove(o); }
        public void clear()               {        c.clear(); }

        public boolean containsAll(Collection<?> coll) {
            return c.containsAll(coll);
        }
        public boolean removeAll(Collection<?> coll) {
            return c.removeAll(coll);
        }
        public boolean retainAll(Collection<?> coll) {
            return c.retainAll(coll);
        }

        public Iterator<E> iterator() {
            // JDK-6363904 - unwrapped iterator could be typecast to
            // ListIterator with unsafe set()
            final Iterator<E> it = c.iterator();
            return new Iterator<E>() {
                public boolean hasNext() { return it.hasNext(); }
                public E next()          { return it.next(); }
                public void remove()     {        it.remove(); }};
        }

        public boolean add(E e)          { return c.add(typeCheck(e)); }

        private E[] zeroLengthElementArray; // Lazily initialized

        private E[] zeroLengthElementArray() {
            return zeroLengthElementArray != null ? zeroLengthElementArray :
                (zeroLengthElementArray = zeroLengthArray(type));
        }

        @SuppressWarnings("unchecked")
        Collection<E> checkedCopyOf(Collection<? extends E> coll) {
            Object[] a;
            try {
                E[] z = zeroLengthElementArray();
                a = coll.toArray(z);
                // Defend against coll violating the toArray contract
                if (a.getClass() != z.getClass())
                    a = Arrays.copyOf(a, a.length, z.getClass());
            } catch (ArrayStoreException ignore) {
                // To get better and consistent diagnostics,
                // we call typeCheck explicitly on each element.
                // We call clone() to defend against coll retaining a
                // reference to the returned array and storing a bad
                // element into it after it has been type checked.
                a = coll.toArray().clone();
                for (Object o : a)
                    typeCheck(o);
            }
            // A slight abuse of the type system, but safe here.
            return (Collection<E>) Arrays.asList(a);
        }

        public boolean addAll(Collection<? extends E> coll) {
            // Doing things this way insulates us from concurrent changes
            // in the contents of coll and provides all-or-nothing
            // semantics (which we wouldn't get if we type-checked each
            // element as we added it)
            return c.addAll(checkedCopyOf(coll));
        }

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {c.forEach(action);}
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return c.removeIf(filter);
        }
        @Override
        public Spliterator<E> spliterator() {return c.spliterator();}
        @Override
        public Stream<E> stream()           {return c.stream();}
        @Override
        public Stream<E> parallelStream()   {return c.parallelStream();}
    }

    /**
     * Returns a dynamically typesafe view of the specified queue.
     * Any attempt to insert an element of the wrong type will result in
     * an immediate {@link ClassCastException}.  Assuming a queue contains
     * no incorrectly typed elements prior to the time a dynamically typesafe
     * view is generated, and that all subsequent access to the queue
     * takes place through the view, it is <i>guaranteed</i> that the
     * queue cannot contain an incorrectly typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned queue will be serializable if the specified queue
     * is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned queue permits insertion of {@code null} elements
     * whenever the backing queue does.
     *
     * @param <E> the class of the objects in the queue
     * @param queue the queue for which a dynamically typesafe view is to be
     *             returned
     * @param type the type of element that {@code queue} is permitted to hold
     * @return a dynamically typesafe view of the specified queue
     * @since 1.8
     */
    public static <E> Queue<E> checkedQueue(Queue<E> queue, Class<E> type) {
        return new CheckedQueue<>(queue, type);
    }

    /**
     * @serial include
     */
    static class CheckedQueue<E>
        extends CheckedCollection<E>
        implements Queue<E>, Serializable
    {
        private static final long serialVersionUID = 1433151992604707767L;
        final Queue<E> queue;

        CheckedQueue(Queue<E> queue, Class<E> elementType) {
            super(queue, elementType);
            this.queue = queue;
        }

        public E element()              {return queue.element();}
        public boolean equals(Object o) {return o == this || c.equals(o);}
        public int hashCode()           {return c.hashCode();}
        public E peek()                 {return queue.peek();}
        public E poll()                 {return queue.poll();}
        public E remove()               {return queue.remove();}
        public boolean offer(E e)       {return queue.offer(typeCheck(e));}
    }

    /**
     * Returns a dynamically typesafe view of the specified set.
     * Any attempt to insert an element of the wrong type will result in
     * an immediate {@link ClassCastException}.  Assuming a set contains
     * no incorrectly typed elements prior to the time a dynamically typesafe
     * view is generated, and that all subsequent access to the set
     * takes place through the view, it is <i>guaranteed</i> that the
     * set cannot contain an incorrectly typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned set permits insertion of null elements whenever
     * the backing set does.
     *
     * @param <E> the class of the objects in the set
     * @param s the set for which a dynamically typesafe view is to be
     *          returned
     * @param type the type of element that {@code s} is permitted to hold
     * @return a dynamically typesafe view of the specified set
     * @since 1.5
     */
    public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
        return new CheckedSet<>(s, type);
    }

    /**
     * @serial include
     */
    static class CheckedSet<E> extends CheckedCollection<E>
                                 implements Set<E>, Serializable
    {
        private static final long serialVersionUID = 4694047833775013803L;

        CheckedSet(Set<E> s, Class<E> elementType) { super(s, elementType); }

        public boolean equals(Object o) { return o == this || c.equals(o); }
        public int hashCode()           { return c.hashCode(); }
    }

    /**
     * Returns a dynamically typesafe view of the specified sorted set.
     * Any attempt to insert an element of the wrong type will result in an
     * immediate {@link ClassCastException}.  Assuming a sorted set
     * contains no incorrectly typed elements prior to the time a
     * dynamically typesafe view is generated, and that all subsequent
     * access to the sorted set takes place through the view, it is
     * <i>guaranteed</i> that the sorted set cannot contain an incorrectly
     * typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned sorted set will be serializable if the specified sorted
     * set is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned sorted set permits insertion of null elements
     * whenever the backing sorted set does.
     *
     * @param <E> the class of the objects in the set
     * @param s the sorted set for which a dynamically typesafe view is to be
     *          returned
     * @param type the type of element that {@code s} is permitted to hold
     * @return a dynamically typesafe view of the specified sorted set
     * @since 1.5
     */
    public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s,
                                                    Class<E> type) {
        return new CheckedSortedSet<>(s, type);
    }

    /**
     * @serial include
     */
    static class CheckedSortedSet<E> extends CheckedSet<E>
        implements SortedSet<E>, Serializable
    {
        private static final long serialVersionUID = 1599911165492914959L;

        private final SortedSet<E> ss;

        CheckedSortedSet(SortedSet<E> s, Class<E> type) {
            super(s, type);
            ss = s;
        }

        public Comparator<? super E> comparator() { return ss.comparator(); }
        public E first()                   { return ss.first(); }
        public E last()                    { return ss.last(); }

        public SortedSet<E> subSet(E fromElement, E toElement) {
            return checkedSortedSet(ss.subSet(fromElement,toElement), type);
        }
        public SortedSet<E> headSet(E toElement) {
            return checkedSortedSet(ss.headSet(toElement), type);
        }
        public SortedSet<E> tailSet(E fromElement) {
            return checkedSortedSet(ss.tailSet(fromElement), type);
        }
    }

/**
     * Returns a dynamically typesafe view of the specified navigable set.
     * Any attempt to insert an element of the wrong type will result in an
     * immediate {@link ClassCastException}.  Assuming a navigable set
     * contains no incorrectly typed elements prior to the time a
     * dynamically typesafe view is generated, and that all subsequent
     * access to the navigable set takes place through the view, it is
     * <em>guaranteed</em> that the navigable set cannot contain an incorrectly
     * typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned navigable set will be serializable if the specified
     * navigable set is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned navigable set permits insertion of null elements
     * whenever the backing sorted set does.
     *
     * @param <E> the class of the objects in the set
     * @param s the navigable set for which a dynamically typesafe view is to be
     *          returned
     * @param type the type of element that {@code s} is permitted to hold
     * @return a dynamically typesafe view of the specified navigable set
     * @since 1.8
     */
    public static <E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> s,
                                                    Class<E> type) {
        return new CheckedNavigableSet<>(s, type);
    }

    /**
     * @serial include
     */
    static class CheckedNavigableSet<E> extends CheckedSortedSet<E>
        implements NavigableSet<E>, Serializable
    {
        private static final long serialVersionUID = -5429120189805438922L;

        private final NavigableSet<E> ns;

        CheckedNavigableSet(NavigableSet<E> s, Class<E> type) {
            super(s, type);
            ns = s;
        }

        public E lower(E e)                             { return ns.lower(e); }
        public E floor(E e)                             { return ns.floor(e); }
        public E ceiling(E e)                         { return ns.ceiling(e); }
        public E higher(E e)                           { return ns.higher(e); }
        public E pollFirst()                         { return ns.pollFirst(); }
        public E pollLast()                            {return ns.pollLast(); }
        public NavigableSet<E> descendingSet()
                      { return checkedNavigableSet(ns.descendingSet(), type); }
        public Iterator<E> descendingIterator()
            {return checkedNavigableSet(ns.descendingSet(), type).iterator(); }

        public NavigableSet<E> subSet(E fromElement, E toElement) {
            return checkedNavigableSet(ns.subSet(fromElement, true, toElement, false), type);
        }
        public NavigableSet<E> headSet(E toElement) {
            return checkedNavigableSet(ns.headSet(toElement, false), type);
        }
        public NavigableSet<E> tailSet(E fromElement) {
            return checkedNavigableSet(ns.tailSet(fromElement, true), type);
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return checkedNavigableSet(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), type);
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return checkedNavigableSet(ns.headSet(toElement, inclusive), type);
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return checkedNavigableSet(ns.tailSet(fromElement, inclusive), type);
        }
    }

    /**
     * Returns a dynamically typesafe view of the specified list.
     * Any attempt to insert an element of the wrong type will result in
     * an immediate {@link ClassCastException}.  Assuming a list contains
     * no incorrectly typed elements prior to the time a dynamically typesafe
     * view is generated, and that all subsequent access to the list
     * takes place through the view, it is <i>guaranteed</i> that the
     * list cannot contain an incorrectly typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned list will be serializable if the specified list
     * is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned list permits insertion of null elements whenever
     * the backing list does.
     *
     * @param <E> the class of the objects in the list
     * @param list the list for which a dynamically typesafe view is to be
     *             returned
     * @param type the type of element that {@code list} is permitted to hold
     * @return a dynamically typesafe view of the specified list
     * @since 1.5
     */
    public static <E> List<E> checkedList(List<E> list, Class<E> type) {
        return (list instanceof RandomAccess ?
                new CheckedRandomAccessList<>(list, type) :
                new CheckedList<>(list, type));
    }

    /**
     * @serial include
     */
    static class CheckedList<E>
        extends CheckedCollection<E>
        implements List<E>
    {
        private static final long serialVersionUID = 65247728283967356L;
        final List<E> list;

        CheckedList(List<E> list, Class<E> type) {
            super(list, type);
            this.list = list;
        }

        public boolean equals(Object o)  { return o == this || list.equals(o); }
        public int hashCode()            { return list.hashCode(); }
        public E get(int index)          { return list.get(index); }
        public E remove(int index)       { return list.remove(index); }
        public int indexOf(Object o)     { return list.indexOf(o); }
        public int lastIndexOf(Object o) { return list.lastIndexOf(o); }

        public E set(int index, E element) {
            return list.set(index, typeCheck(element));
        }

        public void add(int index, E element) {
            list.add(index, typeCheck(element));
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            return list.addAll(index, checkedCopyOf(c));
        }
        public ListIterator<E> listIterator()   { return listIterator(0); }

        public ListIterator<E> listIterator(final int index) {
            final ListIterator<E> i = list.listIterator(index);

            return new ListIterator<E>() {
                public boolean hasNext()     { return i.hasNext(); }
                public E next()              { return i.next(); }
                public boolean hasPrevious() { return i.hasPrevious(); }
                public E previous()          { return i.previous(); }
                public int nextIndex()       { return i.nextIndex(); }
                public int previousIndex()   { return i.previousIndex(); }
                public void remove()         {        i.remove(); }

                public void set(E e) {
                    i.set(typeCheck(e));
                }

                public void add(E e) {
                    i.add(typeCheck(e));
                }

                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    i.forEachRemaining(action);
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new CheckedList<>(list.subList(fromIndex, toIndex), type);
        }

        /**
         * {@inheritDoc}
         *
         * @throws ClassCastException if the class of an element returned by the
         *         operator prevents it from being added to this collection. The
         *         exception may be thrown after some elements of the list have
         *         already been replaced.
         */
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            Objects.requireNonNull(operator);
            list.replaceAll(e -> typeCheck(operator.apply(e)));
        }

        @Override
        public void sort(Comparator<? super E> c) {
            list.sort(c);
        }
    }

    /**
     * @serial include
     */
    static class CheckedRandomAccessList<E> extends CheckedList<E>
                                            implements RandomAccess
    {
        private static final long serialVersionUID = 1638200125423088369L;

        CheckedRandomAccessList(List<E> list, Class<E> type) {
            super(list, type);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new CheckedRandomAccessList<>(
                    list.subList(fromIndex, toIndex), type);
        }
    }

    /**
     * Returns a dynamically typesafe view of the specified map.
     * Any attempt to insert a mapping whose key or value have the wrong
     * type will result in an immediate {@link ClassCastException}.
     * Similarly, any attempt to modify the value currently associated with
     * a key will result in an immediate {@link ClassCastException},
     * whether the modification is attempted directly through the map
     * itself, or through a {@link Map.Entry} instance obtained from the
     * map's {@link Map#entrySet() entry set} view.
     *
     * <p>Assuming a map contains no incorrectly typed keys or values
     * prior to the time a dynamically typesafe view is generated, and
     * that all subsequent access to the map takes place through the view
     * (or one of its collection views), it is <i>guaranteed</i> that the
     * map cannot contain an incorrectly typed key or value.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned map permits insertion of null keys or values
     * whenever the backing map does.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param m the map for which a dynamically typesafe view is to be
     *          returned
     * @param keyType the type of key that {@code m} is permitted to hold
     * @param valueType the type of value that {@code m} is permitted to hold
     * @return a dynamically typesafe view of the specified map
     * @since 1.5
     */
    public static <K, V> Map<K, V> checkedMap(Map<K, V> m,
                                              Class<K> keyType,
                                              Class<V> valueType) {
        return new CheckedMap<>(m, keyType, valueType);
    }


    /**
     * @serial include
     */
    private static class CheckedMap<K,V>
        implements Map<K,V>, Serializable
    {
        private static final long serialVersionUID = 5742860141034234728L;

        private final Map<K, V> m;
        final Class<K> keyType;
        final Class<V> valueType;

        private void typeCheck(Object key, Object value) {
            if (key != null && !keyType.isInstance(key))
                throw new ClassCastException(badKeyMsg(key));

            if (value != null && !valueType.isInstance(value))
                throw new ClassCastException(badValueMsg(value));
        }

        private BiFunction<? super K, ? super V, ? extends V> typeCheck(
                BiFunction<? super K, ? super V, ? extends V> func) {
            Objects.requireNonNull(func);
            return (k, v) -> {
                V newValue = func.apply(k, v);
                typeCheck(k, newValue);
                return newValue;
            };
        }

        private String badKeyMsg(Object key) {
            return "Attempt to insert " + key.getClass() +
                    " key into map with key type " + keyType;
        }

        private String badValueMsg(Object value) {
            return "Attempt to insert " + value.getClass() +
                    " value into map with value type " + valueType;
        }

        CheckedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
            this.m = Objects.requireNonNull(m);
            this.keyType = Objects.requireNonNull(keyType);
            this.valueType = Objects.requireNonNull(valueType);
        }

        public int size()                      { return m.size(); }
        public boolean isEmpty()               { return m.isEmpty(); }
        public boolean containsKey(Object key) { return m.containsKey(key); }
        public boolean containsValue(Object v) { return m.containsValue(v); }
        public V get(Object key)               { return m.get(key); }
        public V remove(Object key)            { return m.remove(key); }
        public void clear()                    { m.clear(); }
        public Set<K> keySet()                 { return m.keySet(); }
        public Collection<V> values()          { return m.values(); }
        public boolean equals(Object o)        { return o == this || m.equals(o); }
        public int hashCode()                  { return m.hashCode(); }
        public String toString()               { return m.toString(); }

        public V put(K key, V value) {
            typeCheck(key, value);
            return m.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public void putAll(Map<? extends K, ? extends V> t) {
            // Satisfy the following goals:
            // - good diagnostics in case of type mismatch
            // - all-or-nothing semantics
            // - protection from malicious t
            // - correct behavior if t is a concurrent map
            Object[] entries = t.entrySet().toArray();
            List<Map.Entry<K,V>> checked = new ArrayList<>(entries.length);
            for (Object o : entries) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object k = e.getKey();
                Object v = e.getValue();
                typeCheck(k, v);
                checked.add(
                        new AbstractMap.SimpleImmutableEntry<>((K)k, (V)v));
            }
            for (Map.Entry<K,V> e : checked)
                m.put(e.getKey(), e.getValue());
        }

        private transient Set<Map.Entry<K,V>> entrySet;

        public Set<Map.Entry<K,V>> entrySet() {
            if (entrySet==null)
                entrySet = new CheckedEntrySet<>(m.entrySet(), valueType);
            return entrySet;
        }

        // Override default methods in Map
        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            m.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            m.replaceAll(typeCheck(function));
        }

        @Override
        public V putIfAbsent(K key, V value) {
            typeCheck(key, value);
            return m.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return m.remove(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            typeCheck(key, newValue);
            return m.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            typeCheck(key, value);
            return m.replace(key, value);
        }

        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            return m.computeIfAbsent(key, k -> {
                V value = mappingFunction.apply(k);
                typeCheck(k, value);
                return value;
            });
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return m.computeIfPresent(key, typeCheck(remappingFunction));
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return m.compute(key, typeCheck(remappingFunction));
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            return m.merge(key, value, (v1, v2) -> {
                V newValue = remappingFunction.apply(v1, v2);
                typeCheck(null, newValue);
                return newValue;
            });
        }

        /**
         * We need this class in addition to CheckedSet as Map.Entry permits
         * modification of the backing Map via the setValue operation.  This
         * class is subtle: there are many possible attacks that must be
         * thwarted.
         *
         * @serial exclude
         */
        static class CheckedEntrySet<K,V> implements Set<Map.Entry<K,V>> {
            private final Set<Map.Entry<K,V>> s;
            private final Class<V> valueType;

            CheckedEntrySet(Set<Map.Entry<K, V>> s, Class<V> valueType) {
                this.s = s;
                this.valueType = valueType;
            }

            public int size()        { return s.size(); }
            public boolean isEmpty() { return s.isEmpty(); }
            public String toString() { return s.toString(); }
            public int hashCode()    { return s.hashCode(); }
            public void clear()      {        s.clear(); }

            public boolean add(Map.Entry<K, V> e) {
                throw new UnsupportedOperationException();
            }
            public boolean addAll(Collection<? extends Map.Entry<K, V>> coll) {
                throw new UnsupportedOperationException();
            }

            public Iterator<Map.Entry<K,V>> iterator() {
                final Iterator<Map.Entry<K, V>> i = s.iterator();
                final Class<V> valueType = this.valueType;

                return new Iterator<Map.Entry<K,V>>() {
                    public boolean hasNext() { return i.hasNext(); }
                    public void remove()     { i.remove(); }

                    public Map.Entry<K,V> next() {
                        return checkedEntry(i.next(), valueType);
                    }
                };
            }

            @SuppressWarnings("unchecked")
            public Object[] toArray() {
                Object[] source = s.toArray();

                /*
                 * Ensure that we don't get an ArrayStoreException even if
                 * s.toArray returns an array of something other than Object
                 */
                Object[] dest = (CheckedEntry.class.isInstance(
                    source.getClass().getComponentType()) ? source :
                                 new Object[source.length]);

                for (int i = 0; i < source.length; i++)
                    dest[i] = checkedEntry((Map.Entry<K,V>)source[i],
                                           valueType);
                return dest;
            }

            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] a) {
                // We don't pass a to s.toArray, to avoid window of
                // vulnerability wherein an unscrupulous multithreaded client
                // could get his hands on raw (unwrapped) Entries from s.
                T[] arr = s.toArray(a.length==0 ? a : Arrays.copyOf(a, 0));

                for (int i=0; i<arr.length; i++)
                    arr[i] = (T) checkedEntry((Map.Entry<K,V>)arr[i],
                                              valueType);
                if (arr.length > a.length)
                    return arr;

                System.arraycopy(arr, 0, a, 0, arr.length);
                if (a.length > arr.length)
                    a[arr.length] = null;
                return a;
            }

            /**
             * This method is overridden to protect the backing set against
             * an object with a nefarious equals function that senses
             * that the equality-candidate is Map.Entry and calls its
             * setValue method.
             */
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                return s.contains(
                    (e instanceof CheckedEntry) ? e : checkedEntry(e, valueType));
            }

            /**
             * The bulk collection methods are overridden to protect
             * against an unscrupulous collection whose contains(Object o)
             * method senses when o is a Map.Entry, and calls o.setValue.
             */
            public boolean containsAll(Collection<?> c) {
                for (Object o : c)
                    if (!contains(o)) // Invokes safe contains() above
                        return false;
                return true;
            }

            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                return s.remove(new AbstractMap.SimpleImmutableEntry
                                <>((Map.Entry<?,?>)o));
            }

            public boolean removeAll(Collection<?> c) {
                return batchRemove(c, false);
            }
            public boolean retainAll(Collection<?> c) {
                return batchRemove(c, true);
            }
            private boolean batchRemove(Collection<?> c, boolean complement) {
                Objects.requireNonNull(c);
                boolean modified = false;
                Iterator<Map.Entry<K,V>> it = iterator();
                while (it.hasNext()) {
                    if (c.contains(it.next()) != complement) {
                        it.remove();
                        modified = true;
                    }
                }
                return modified;
            }

            public boolean equals(Object o) {
                if (o == this)
                    return true;
                if (!(o instanceof Set))
                    return false;
                Set<?> that = (Set<?>) o;
                return that.size() == s.size()
                    && containsAll(that); // Invokes safe containsAll() above
            }

            static <K,V,T> CheckedEntry<K,V,T> checkedEntry(Map.Entry<K,V> e,
                                                            Class<T> valueType) {
                return new CheckedEntry<>(e, valueType);
            }

            /**
             * This "wrapper class" serves two purposes: it prevents
             * the client from modifying the backing Map, by short-circuiting
             * the setValue method, and it protects the backing Map against
             * an ill-behaved Map.Entry that attempts to modify another
             * Map.Entry when asked to perform an equality check.
             */
            private static class CheckedEntry<K,V,T> implements Map.Entry<K,V> {
                private final Map.Entry<K, V> e;
                private final Class<T> valueType;

                CheckedEntry(Map.Entry<K, V> e, Class<T> valueType) {
                    this.e = Objects.requireNonNull(e);
                    this.valueType = Objects.requireNonNull(valueType);
                }

                public K getKey()        { return e.getKey(); }
                public V getValue()      { return e.getValue(); }
                public int hashCode()    { return e.hashCode(); }
                public String toString() { return e.toString(); }

                public V setValue(V value) {
                    if (value != null && !valueType.isInstance(value))
                        throw new ClassCastException(badValueMsg(value));
                    return e.setValue(value);
                }

                private String badValueMsg(Object value) {
                    return "Attempt to insert " + value.getClass() +
                        " value into map with value type " + valueType;
                }

                public boolean equals(Object o) {
                    if (o == this)
                        return true;
                    if (!(o instanceof Map.Entry))
                        return false;
                    return e.equals(new AbstractMap.SimpleImmutableEntry
                                    <>((Map.Entry<?,?>)o));
                }
            }
        }
    }

    /**
     * Returns a dynamically typesafe view of the specified sorted map.
     * Any attempt to insert a mapping whose key or value have the wrong
     * type will result in an immediate {@link ClassCastException}.
     * Similarly, any attempt to modify the value currently associated with
     * a key will result in an immediate {@link ClassCastException},
     * whether the modification is attempted directly through the map
     * itself, or through a {@link Map.Entry} instance obtained from the
     * map's {@link Map#entrySet() entry set} view.
     *
     * <p>Assuming a map contains no incorrectly typed keys or values
     * prior to the time a dynamically typesafe view is generated, and
     * that all subsequent access to the map takes place through the view
     * (or one of its collection views), it is <i>guaranteed</i> that the
     * map cannot contain an incorrectly typed key or value.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned map permits insertion of null keys or values
     * whenever the backing map does.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param m the map for which a dynamically typesafe view is to be
     *          returned
     * @param keyType the type of key that {@code m} is permitted to hold
     * @param valueType the type of value that {@code m} is permitted to hold
     * @return a dynamically typesafe view of the specified map
     * @since 1.5
     */
    public static <K,V> SortedMap<K,V> checkedSortedMap(SortedMap<K, V> m,
                                                        Class<K> keyType,
                                                        Class<V> valueType) {
        return new CheckedSortedMap<>(m, keyType, valueType);
    }

    /**
     * @serial include
     */
    static class CheckedSortedMap<K,V> extends CheckedMap<K,V>
        implements SortedMap<K,V>, Serializable
    {
        private static final long serialVersionUID = 1599671320688067438L;

        private final SortedMap<K, V> sm;

        CheckedSortedMap(SortedMap<K, V> m,
                         Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            sm = m;
        }

        public Comparator<? super K> comparator() { return sm.comparator(); }
        public K firstKey()                       { return sm.firstKey(); }
        public K lastKey()                        { return sm.lastKey(); }

        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            return checkedSortedMap(sm.subMap(fromKey, toKey),
                                    keyType, valueType);
        }
        public SortedMap<K,V> headMap(K toKey) {
            return checkedSortedMap(sm.headMap(toKey), keyType, valueType);
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            return checkedSortedMap(sm.tailMap(fromKey), keyType, valueType);
        }
    }

    /**
     * Returns a dynamically typesafe view of the specified navigable map.
     * Any attempt to insert a mapping whose key or value have the wrong
     * type will result in an immediate {@link ClassCastException}.
     * Similarly, any attempt to modify the value currently associated with
     * a key will result in an immediate {@link ClassCastException},
     * whether the modification is attempted directly through the map
     * itself, or through a {@link Map.Entry} instance obtained from the
     * map's {@link Map#entrySet() entry set} view.
     *
     * <p>Assuming a map contains no incorrectly typed keys or values
     * prior to the time a dynamically typesafe view is generated, and
     * that all subsequent access to the map takes place through the view
     * (or one of its collection views), it is <em>guaranteed</em> that the
     * map cannot contain an incorrectly typed key or value.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned map permits insertion of null keys or values
     * whenever the backing map does.
     *
     * @param <K> type of map keys
     * @param <V> type of map values
     * @param m the map for which a dynamically typesafe view is to be
     *          returned
     * @param keyType the type of key that {@code m} is permitted to hold
     * @param valueType the type of value that {@code m} is permitted to hold
     * @return a dynamically typesafe view of the specified map
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> checkedNavigableMap(NavigableMap<K, V> m,
                                                        Class<K> keyType,
                                                        Class<V> valueType) {
        return new CheckedNavigableMap<>(m, keyType, valueType);
    }

    /**
     * @serial include
     */
    static class CheckedNavigableMap<K,V> extends CheckedSortedMap<K,V>
        implements NavigableMap<K,V>, Serializable
    {
        private static final long serialVersionUID = -4852462692372534096L;

        private final NavigableMap<K, V> nm;

        CheckedNavigableMap(NavigableMap<K, V> m,
                         Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            nm = m;
        }

        public Comparator<? super K> comparator()   { return nm.comparator(); }
        public K firstKey()                           { return nm.firstKey(); }
        public K lastKey()                             { return nm.lastKey(); }

        public Entry<K, V> lowerEntry(K key) {
            Entry<K,V> lower = nm.lowerEntry(key);
            return (null != lower)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(lower, valueType)
                : null;
        }

        public K lowerKey(K key)                   { return nm.lowerKey(key); }

        public Entry<K, V> floorEntry(K key) {
            Entry<K,V> floor = nm.floorEntry(key);
            return (null != floor)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(floor, valueType)
                : null;
        }

        public K floorKey(K key)                   { return nm.floorKey(key); }

        public Entry<K, V> ceilingEntry(K key) {
            Entry<K,V> ceiling = nm.ceilingEntry(key);
            return (null != ceiling)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(ceiling, valueType)
                : null;
        }

        public K ceilingKey(K key)               { return nm.ceilingKey(key); }

        public Entry<K, V> higherEntry(K key) {
            Entry<K,V> higher = nm.higherEntry(key);
            return (null != higher)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(higher, valueType)
                : null;
        }

        public K higherKey(K key)                 { return nm.higherKey(key); }

        public Entry<K, V> firstEntry() {
            Entry<K,V> first = nm.firstEntry();
            return (null != first)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(first, valueType)
                : null;
        }

        public Entry<K, V> lastEntry() {
            Entry<K,V> last = nm.lastEntry();
            return (null != last)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(last, valueType)
                : null;
        }

        public Entry<K, V> pollFirstEntry() {
            Entry<K,V> entry = nm.pollFirstEntry();
            return (null == entry)
                ? null
                : new CheckedMap.CheckedEntrySet.CheckedEntry<>(entry, valueType);
        }

        public Entry<K, V> pollLastEntry() {
            Entry<K,V> entry = nm.pollLastEntry();
            return (null == entry)
                ? null
                : new CheckedMap.CheckedEntrySet.CheckedEntry<>(entry, valueType);
        }

        public NavigableMap<K, V> descendingMap() {
            return checkedNavigableMap(nm.descendingMap(), keyType, valueType);
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            return checkedNavigableSet(nm.navigableKeySet(), keyType);
        }

        public NavigableSet<K> descendingKeySet() {
            return checkedNavigableSet(nm.descendingKeySet(), keyType);
        }

        @Override
        public NavigableMap<K,V> subMap(K fromKey, K toKey) {
            return checkedNavigableMap(nm.subMap(fromKey, true, toKey, false),
                                    keyType, valueType);
        }

        @Override
        public NavigableMap<K,V> headMap(K toKey) {
            return checkedNavigableMap(nm.headMap(toKey, false), keyType, valueType);
        }

        @Override
        public NavigableMap<K,V> tailMap(K fromKey) {
            return checkedNavigableMap(nm.tailMap(fromKey, true), keyType, valueType);
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return checkedNavigableMap(nm.subMap(fromKey, fromInclusive, toKey, toInclusive), keyType, valueType);
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            return checkedNavigableMap(nm.headMap(toKey, inclusive), keyType, valueType);
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            return checkedNavigableMap(nm.tailMap(fromKey, inclusive), keyType, valueType);
        }
    }

    // Empty collections

    /**
     * Returns an iterator that has no elements.  More precisely,
     *
     * <ul>
     * <li>{@link Iterator#hasNext hasNext} always returns {@code
     * false}.</li>
     * <li>{@link Iterator#next next} always throws {@link
     * NoSuchElementException}.</li>
     * <li>{@link Iterator#remove remove} always throws {@link
     * IllegalStateException}.</li>
     * </ul>
     *
     * <p>Implementations of this method are permitted, but not
     * required, to return the same object from multiple invocations.
     *
     * @param <T> type of elements, if there were any, in the iterator
     * @return an empty iterator
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
    }

    private static class EmptyIterator<E> implements Iterator<E> {
        static final EmptyIterator<Object> EMPTY_ITERATOR
            = new EmptyIterator<>();

        public boolean hasNext() { return false; }
        public E next() { throw new NoSuchElementException(); }
        public void remove() { throw new IllegalStateException(); }
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }
    }

    /**
     * Returns a list iterator that has no elements.  More precisely,
     *
     * <ul>
     * <li>{@link Iterator#hasNext hasNext} and {@link
     * ListIterator#hasPrevious hasPrevious} always return {@code
     * false}.</li>
     * <li>{@link Iterator#next next} and {@link ListIterator#previous
     * previous} always throw {@link NoSuchElementException}.</li>
     * <li>{@link Iterator#remove remove} and {@link ListIterator#set
     * set} always throw {@link IllegalStateException}.</li>
     * <li>{@link ListIterator#add add} always throws {@link
     * UnsupportedOperationException}.</li>
     * <li>{@link ListIterator#nextIndex nextIndex} always returns
     * {@code 0}.</li>
     * <li>{@link ListIterator#previousIndex previousIndex} always
     * returns {@code -1}.</li>
     * </ul>
     *
     * <p>Implementations of this method are permitted, but not
     * required, to return the same object from multiple invocations.
     *
     * @param <T> type of elements, if there were any, in the iterator
     * @return an empty list iterator
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> ListIterator<T> emptyListIterator() {
        return (ListIterator<T>) EmptyListIterator.EMPTY_ITERATOR;
    }

    private static class EmptyListIterator<E>
        extends EmptyIterator<E>
        implements ListIterator<E>
    {
        static final EmptyListIterator<Object> EMPTY_ITERATOR
            = new EmptyListIterator<>();

        public boolean hasPrevious() { return false; }
        public E previous() { throw new NoSuchElementException(); }
        public int nextIndex()     { return 0; }
        public int previousIndex() { return -1; }
        public void set(E e) { throw new IllegalStateException(); }
        public void add(E e) { throw new UnsupportedOperationException(); }
    }

    /**
     * Returns an enumeration that has no elements.  More precisely,
     *
     * <ul>
     * <li>{@link Enumeration#hasMoreElements hasMoreElements} always
     * returns {@code false}.</li>
     * <li> {@link Enumeration#nextElement nextElement} always throws
     * {@link NoSuchElementException}.</li>
     * </ul>
     *
     * <p>Implementations of this method are permitted, but not
     * required, to return the same object from multiple invocations.
     *
     * @param  <T> the class of the objects in the enumeration
     * @return an empty enumeration
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) EmptyEnumeration.EMPTY_ENUMERATION;
    }

    private static class EmptyEnumeration<E> implements Enumeration<E> {
        static final EmptyEnumeration<Object> EMPTY_ENUMERATION
            = new EmptyEnumeration<>();

        public boolean hasMoreElements() { return false; }
        public E nextElement() { throw new NoSuchElementException(); }
    }

    /**
     * The empty set (immutable).  This set is serializable.
     *
     * @see #emptySet()
     */
    @SuppressWarnings("rawtypes")
    public static final Set EMPTY_SET = new EmptySet<>();

    /**
     * Returns an empty set (immutable).  This set is serializable.
     * Unlike the like-named field, this method is parameterized.
     *
     * <p>This example illustrates the type-safe way to obtain an empty set:
     * <pre>
     *     Set&lt;String&gt; s = Collections.emptySet();
     * </pre>
     * @implNote Implementations of this method need not create a separate
     * {@code Set} object for each call.  Using this method is likely to have
     * comparable cost to using the like-named field.  (Unlike this method, the
     * field does not provide type safety.)
     *
     * @param  <T> the class of the objects in the set
     * @return the empty set
     *
     * @see #EMPTY_SET
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public static final <T> Set<T> emptySet() {
        return (Set<T>) EMPTY_SET;
    }

    /**
     * @serial include
     */
    private static class EmptySet<E>
        extends AbstractSet<E>
        implements Serializable
    {
        private static final long serialVersionUID = 1582296315990362920L;

        public Iterator<E> iterator() { return emptyIterator(); }

        public int size() {return 0;}
        public boolean isEmpty() {return true;}

        public boolean contains(Object obj) {return false;}
        public boolean containsAll(Collection<?> c) { return c.isEmpty(); }

        public Object[] toArray() { return new Object[0]; }

        public <T> T[] toArray(T[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            Objects.requireNonNull(filter);
            return false;
        }
        @Override
        public Spliterator<E> spliterator() { return Spliterators.emptySpliterator(); }

        // Preserves singleton property
        private Object readResolve() {
            return EMPTY_SET;
        }
    }

    /**
     * Returns an empty sorted set (immutable).  This set is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty
     * sorted set:
     * <pre> {@code
     *     SortedSet<String> s = Collections.emptySortedSet();
     * }</pre>
     *
     * @implNote Implementations of this method need not create a separate
     * {@code SortedSet} object for each call.
     *
     * @param <E> type of elements, if there were any, in the set
     * @return the empty sorted set
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <E> SortedSet<E> emptySortedSet() {
        return (SortedSet<E>) UnmodifiableNavigableSet.EMPTY_NAVIGABLE_SET;
    }

    /**
     * Returns an empty navigable set (immutable).  This set is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty
     * navigable set:
     * <pre> {@code
     *     NavigableSet<String> s = Collections.emptyNavigableSet();
     * }</pre>
     *
     * @implNote Implementations of this method need not
     * create a separate {@code NavigableSet} object for each call.
     *
     * @param <E> type of elements, if there were any, in the set
     * @return the empty navigable set
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <E> NavigableSet<E> emptyNavigableSet() {
        return (NavigableSet<E>) UnmodifiableNavigableSet.EMPTY_NAVIGABLE_SET;
    }

    /**
     * The empty list (immutable).  This list is serializable.
     *
     * @see #emptyList()
     */
    @SuppressWarnings("rawtypes")
    public static final List EMPTY_LIST = new EmptyList<>();

    /**
     * Returns an empty list (immutable).  This list is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty list:
     * <pre>
     *     List&lt;String&gt; s = Collections.emptyList();
     * </pre>
     *
     * @implNote
     * Implementations of this method need not create a separate <tt>List</tt>
     * object for each call.   Using this method is likely to have comparable
     * cost to using the like-named field.  (Unlike this method, the field does
     * not provide type safety.)
     *
     * @param <T> type of elements, if there were any, in the list
     * @return an empty immutable list
     *
     * @see #EMPTY_LIST
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public static final <T> List<T> emptyList() {
        return (List<T>) EMPTY_LIST;
    }

    /**
     * @serial include
     */
    private static class EmptyList<E>
        extends AbstractList<E>
        implements RandomAccess, Serializable {
        private static final long serialVersionUID = 8842843931221139166L;

        public Iterator<E> iterator() {
            return emptyIterator();
        }
        public ListIterator<E> listIterator() {
            return emptyListIterator();
        }

        public int size() {return 0;}
        public boolean isEmpty() {return true;}

        public boolean contains(Object obj) {return false;}
        public boolean containsAll(Collection<?> c) { return c.isEmpty(); }

        public Object[] toArray() { return new Object[0]; }

        public <T> T[] toArray(T[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        public E get(int index) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }

        public boolean equals(Object o) {
            return (o instanceof List) && ((List<?>)o).isEmpty();
        }

        public int hashCode() { return 1; }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            Objects.requireNonNull(filter);
            return false;
        }
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            Objects.requireNonNull(operator);
        }
        @Override
        public void sort(Comparator<? super E> c) {
        }

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public Spliterator<E> spliterator() { return Spliterators.emptySpliterator(); }

        // Preserves singleton property
        private Object readResolve() {
            return EMPTY_LIST;
        }
    }

    /**
     * The empty map (immutable).  This map is serializable.
     *
     * @see #emptyMap()
     * @since 1.3
     */
    @SuppressWarnings("rawtypes")
    public static final Map EMPTY_MAP = new EmptyMap<>();

    /**
     * Returns an empty map (immutable).  This map is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty map:
     * <pre>
     *     Map&lt;String, Date&gt; s = Collections.emptyMap();
     * </pre>
     * @implNote Implementations of this method need not create a separate
     * {@code Map} object for each call.  Using this method is likely to have
     * comparable cost to using the like-named field.  (Unlike this method, the
     * field does not provide type safety.)
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @return an empty map
     * @see #EMPTY_MAP
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> Map<K,V> emptyMap() {
        return (Map<K,V>) EMPTY_MAP;
    }

    /**
     * Returns an empty sorted map (immutable).  This map is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty map:
     * <pre> {@code
     *     SortedMap<String, Date> s = Collections.emptySortedMap();
     * }</pre>
     *
     * @implNote Implementations of this method need not create a separate
     * {@code SortedMap} object for each call.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @return an empty sorted map
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> SortedMap<K,V> emptySortedMap() {
        return (SortedMap<K,V>) UnmodifiableNavigableMap.EMPTY_NAVIGABLE_MAP;
    }

    /**
     * Returns an empty navigable map (immutable).  This map is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty map:
     * <pre> {@code
     *     NavigableMap<String, Date> s = Collections.emptyNavigableMap();
     * }</pre>
     *
     * @implNote Implementations of this method need not create a separate
     * {@code NavigableMap} object for each call.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @return an empty navigable map
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> NavigableMap<K,V> emptyNavigableMap() {
        return (NavigableMap<K,V>) UnmodifiableNavigableMap.EMPTY_NAVIGABLE_MAP;
    }

    /**
     * @serial include
     */
    private static class EmptyMap<K,V>
        extends AbstractMap<K,V>
        implements Serializable
    {
        private static final long serialVersionUID = 6428348081105594320L;

        public int size()                          {return 0;}
        public boolean isEmpty()                   {return true;}
        public boolean containsKey(Object key)     {return false;}
        public boolean containsValue(Object value) {return false;}
        public V get(Object key)                   {return null;}
        public Set<K> keySet()                     {return emptySet();}
        public Collection<V> values()              {return emptySet();}
        public Set<Map.Entry<K,V>> entrySet()      {return emptySet();}

        public boolean equals(Object o) {
            return (o instanceof Map) && ((Map<?,?>)o).isEmpty();
        }

        public int hashCode()                      {return 0;}

        // Override default methods in Map
        @Override
        @SuppressWarnings("unchecked")
        public V getOrDefault(Object k, V defaultValue) {
            return defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            Objects.requireNonNull(function);
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        // Preserves singleton property
        private Object readResolve() {
            return EMPTY_MAP;
        }
    }

    // Singleton collections

    /**
     * Returns an immutable set containing only the specified object.
     * The returned set is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param o the sole object to be stored in the returned set.
     * @return an immutable set containing only the specified object.
     */
    public static <T> Set<T> singleton(T o) {
        return new SingletonSet<>(o);
    }

    static <E> Iterator<E> singletonIterator(final E e) {
        return new Iterator<E>() {
            private boolean hasNext = true;
            public boolean hasNext() {
                return hasNext;
            }
            public E next() {
                if (hasNext) {
                    hasNext = false;
                    return e;
                }
                throw new NoSuchElementException();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                Objects.requireNonNull(action);
                if (hasNext) {
                    action.accept(e);
                    hasNext = false;
                }
            }
        };
    }

    /**
     * Creates a {@code Spliterator} with only the specified element
     *
     * @param <T> Type of elements
     * @return A singleton {@code Spliterator}
     */
    static <T> Spliterator<T> singletonSpliterator(final T element) {
        return new Spliterator<T>() {
            long est = 1;

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                Objects.requireNonNull(consumer);
                if (est > 0) {
                    est--;
                    consumer.accept(element);
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                tryAdvance(consumer);
            }

            @Override
            public long estimateSize() {
                return est;
            }

            @Override
            public int characteristics() {
                int value = (element != null) ? Spliterator.NONNULL : 0;

                return value | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE |
                       Spliterator.DISTINCT | Spliterator.ORDERED;
            }
        };
    }

    /**
     * @serial include
     */
    private static class SingletonSet<E>
        extends AbstractSet<E>
        implements Serializable
    {
        private static final long serialVersionUID = 3193687207550431679L;

        private final E element;

        SingletonSet(E e) {element = e;}

        public Iterator<E> iterator() {
            return singletonIterator(element);
        }

        public int size() {return 1;}

        public boolean contains(Object o) {return eq(o, element);}

        // Override default methods for Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            action.accept(element);
        }
        @Override
        public Spliterator<E> spliterator() {
            return singletonSpliterator(element);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns an immutable list containing only the specified object.
     * The returned list is serializable.
     *
     * @param  <T> the class of the objects in the list
     * @param o the sole object to be stored in the returned list.
     * @return an immutable list containing only the specified object.
     * @since 1.3
     */
    public static <T> List<T> singletonList(T o) {
        return new SingletonList<>(o);
    }

    /**
     * @serial include
     */
    private static class SingletonList<E>
        extends AbstractList<E>
        implements RandomAccess, Serializable {

        private static final long serialVersionUID = 3093736618740652951L;

        private final E element;

        SingletonList(E obj)                {element = obj;}

        public Iterator<E> iterator() {
            return singletonIterator(element);
        }

        public int size()                   {return 1;}

        public boolean contains(Object obj) {return eq(obj, element);}

        public E get(int index) {
            if (index != 0)
              throw new IndexOutOfBoundsException("Index: "+index+", Size: 1");
            return element;
        }

        // Override default methods for Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            action.accept(element);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void sort(Comparator<? super E> c) {
        }
        @Override
        public Spliterator<E> spliterator() {
            return singletonSpliterator(element);
        }
    }

    /**
     * Returns an immutable map, mapping only the specified key to the
     * specified value.  The returned map is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param key the sole key to be stored in the returned map.
     * @param value the value to which the returned map maps <tt>key</tt>.
     * @return an immutable map containing only the specified key-value
     *         mapping.
     * @since 1.3
     */
    public static <K,V> Map<K,V> singletonMap(K key, V value) {
        return new SingletonMap<>(key, value);
    }

    /**
     * @serial include
     */
    private static class SingletonMap<K,V>
          extends AbstractMap<K,V>
          implements Serializable {
        private static final long serialVersionUID = -6979724477215052911L;

        private final K k;
        private final V v;

        SingletonMap(K key, V value) {
            k = key;
            v = value;
        }

        public int size()                                           {return 1;}
        public boolean isEmpty()                                {return false;}
        public boolean containsKey(Object key)             {return eq(key, k);}
        public boolean containsValue(Object value)       {return eq(value, v);}
        public V get(Object key)              {return (eq(key, k) ? v : null);}

        private transient Set<K> keySet;
        private transient Set<Map.Entry<K,V>> entrySet;
        private transient Collection<V> values;

        public Set<K> keySet() {
            if (keySet==null)
                keySet = singleton(k);
            return keySet;
        }

        public Set<Map.Entry<K,V>> entrySet() {
            if (entrySet==null)
                entrySet = Collections.<Map.Entry<K,V>>singleton(
                    new SimpleImmutableEntry<>(k, v));
            return entrySet;
        }

        public Collection<V> values() {
            if (values==null)
                values = singleton(v);
            return values;
        }

        // Override default methods in Map
        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return eq(key, k) ? v : defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            action.accept(k, v);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }
    }

    // Miscellaneous

    /**
     * Returns an immutable list consisting of <tt>n</tt> copies of the
     * specified object.  The newly allocated data object is tiny (it contains
     * a single reference to the data object).  This method is useful in
     * combination with the <tt>List.addAll</tt> method to grow lists.
     * The returned list is serializable.
     *
     * @param  <T> the class of the object to copy and of the objects
     *         in the returned list.
     * @param  n the number of elements in the returned list.
     * @param  o the element to appear repeatedly in the returned list.
     * @return an immutable list consisting of <tt>n</tt> copies of the
     *         specified object.
     * @throws IllegalArgumentException if {@code n < 0}
     * @see    List#addAll(Collection)
     * @see    List#addAll(int, Collection)
     */
    public static <T> List<T> nCopies(int n, T o) {
        if (n < 0)
            throw new IllegalArgumentException("List length = " + n);
        return new CopiesList<>(n, o);
    }

    /**
     * @serial include
     */
    private static class CopiesList<E>
        extends AbstractList<E>
        implements RandomAccess, Serializable
    {
        private static final long serialVersionUID = 2739099268398711800L;

        final int n;
        final E element;

        CopiesList(int n, E e) {
            assert n >= 0;
            this.n = n;
            element = e;
        }

        public int size() {
            return n;
        }

        public boolean contains(Object obj) {
            return n != 0 && eq(obj, element);
        }

        public int indexOf(Object o) {
            return contains(o) ? 0 : -1;
        }

        public int lastIndexOf(Object o) {
            return contains(o) ? n - 1 : -1;
        }

        public E get(int index) {
            if (index < 0 || index >= n)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+n);
            return element;
        }

        public Object[] toArray() {
            final Object[] a = new Object[n];
            if (element != null)
                Arrays.fill(a, 0, n, element);
            return a;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final int n = this.n;
            if (a.length < n) {
                a = (T[])java.lang.reflect.Array
                    .newInstance(a.getClass().getComponentType(), n);
                if (element != null)
                    Arrays.fill(a, 0, n, element);
            } else {
                Arrays.fill(a, 0, n, element);
                if (a.length > n)
                    a[n] = null;
            }
            return a;
        }

        public List<E> subList(int fromIndex, int toIndex) {
            if (fromIndex < 0)
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            if (toIndex > n)
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            if (fromIndex > toIndex)
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                                   ") > toIndex(" + toIndex + ")");
            return new CopiesList<>(toIndex - fromIndex, element);
        }

        @Override
        public int hashCode() {
            if (n == 0) return 1;
            // hashCode of n repeating elements is 31^n + elementHash * Sum(31^k, k = 0..n-1)
            // this implementation completes in O(log(n)) steps taking advantage of
            // 31^(2*n) = (31^n)^2 and Sum(31^k, k = 0..(2*n-1)) = Sum(31^k, k = 0..n-1) * (31^n + 1)
            int pow = 31;
            int sum = 1;
            for (int i = Integer.numberOfLeadingZeros(n) + 1; i < Integer.SIZE; i++) {
                sum *= pow + 1;
                pow *= pow;
                if ((n << i) < 0) {
                    pow *= 31;
                    sum = sum * 31 + 1;
                }
            }
            return pow + sum * (element == null ? 0 : element.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof CopiesList) {
                CopiesList<?> other = (CopiesList<?>) o;
                return n == other.n && (n == 0 || eq(element, other.element));
            }
            if (!(o instanceof List))
                return false;

            int remaining = n;
            E e = element;
            Iterator<?> itr = ((List<?>) o).iterator();
            if (e == null) {
                while (itr.hasNext() && remaining-- > 0) {
                    if (itr.next() != null)
                        return false;
                }
            } else {
                while (itr.hasNext() && remaining-- > 0) {
                    if (!e.equals(itr.next()))
                        return false;
                }
            }
            return remaining == 0 && !itr.hasNext();
        }

        // Override default methods in Collection
        @Override
        public Stream<E> stream() {
            return IntStream.range(0, n).mapToObj(i -> element);
        }

        @Override
        public Stream<E> parallelStream() {
            return IntStream.range(0, n).parallel().mapToObj(i -> element);
        }

        @Override
        public Spliterator<E> spliterator() {
            return stream().spliterator();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            SharedSecrets.getJavaOISAccess().checkArray(ois, Object[].class, n);
        }
    }

    /**
     * Returns a comparator that imposes the reverse of the <em>natural
     * ordering</em> on a collection of objects that implement the
     * {@code Comparable} interface.  (The natural ordering is the ordering
     * imposed by the objects' own {@code compareTo} method.)  This enables a
     * simple idiom for sorting (or maintaining) collections (or arrays) of
     * objects that implement the {@code Comparable} interface in
     * reverse-natural-order.  For example, suppose {@code a} is an array of
     * strings. Then: <pre>
     *          Arrays.sort(a, Collections.reverseOrder());
     * </pre> sorts the array in reverse-lexicographic (alphabetical) order.<p>
     *
     * The returned comparator is serializable.
     *
     * @param  <T> the class of the objects compared by the comparator
     * @return A comparator that imposes the reverse of the <i>natural
     *         ordering</i> on a collection of objects that implement
     *         the <tt>Comparable</tt> interface.
     * @see Comparable
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> reverseOrder() {
        return (Comparator<T>) ReverseComparator.REVERSE_ORDER;
    }

    /**
     * @serial include
     */
    private static class ReverseComparator
        implements Comparator<Comparable<Object>>, Serializable {

        private static final long serialVersionUID = 7207038068494060240L;

        static final ReverseComparator REVERSE_ORDER
            = new ReverseComparator();

        public int compare(Comparable<Object> c1, Comparable<Object> c2) {
            return c2.compareTo(c1);
        }

        private Object readResolve() { return Collections.reverseOrder(); }

        @Override
        public Comparator<Comparable<Object>> reversed() {
            return Comparator.naturalOrder();
        }
    }

    /**
     * Returns a comparator that imposes the reverse ordering of the specified
     * comparator.  If the specified comparator is {@code null}, this method is
     * equivalent to {@link #reverseOrder()} (in other words, it returns a
     * comparator that imposes the reverse of the <em>natural ordering</em> on
     * a collection of objects that implement the Comparable interface).
     *
     * <p>The returned comparator is serializable (assuming the specified
     * comparator is also serializable or {@code null}).
     *
     * @param <T> the class of the objects compared by the comparator
     * @param cmp a comparator who's ordering is to be reversed by the returned
     * comparator or {@code null}
     * @return A comparator that imposes the reverse ordering of the
     *         specified comparator.
     * @since 1.5
     */
    public static <T> Comparator<T> reverseOrder(Comparator<T> cmp) {
        if (cmp == null)
            return reverseOrder();

        if (cmp instanceof ReverseComparator2)
            return ((ReverseComparator2<T>)cmp).cmp;

        return new ReverseComparator2<>(cmp);
    }

    /**
     * @serial include
     */
    private static class ReverseComparator2<T> implements Comparator<T>,
        Serializable
    {
        private static final long serialVersionUID = 4374092139857L;

        /**
         * The comparator specified in the static factory.  This will never
         * be null, as the static factory returns a ReverseComparator
         * instance if its argument is null.
         *
         * @serial
         */
        final Comparator<T> cmp;

        ReverseComparator2(Comparator<T> cmp) {
            assert cmp != null;
            this.cmp = cmp;
        }

        public int compare(T t1, T t2) {
            return cmp.compare(t2, t1);
        }

        public boolean equals(Object o) {
            return (o == this) ||
                (o instanceof ReverseComparator2 &&
                 cmp.equals(((ReverseComparator2)o).cmp));
        }

        public int hashCode() {
            return cmp.hashCode() ^ Integer.MIN_VALUE;
        }

        @Override
        public Comparator<T> reversed() {
            return cmp;
        }
    }

    /**
     * Returns an enumeration over the specified collection.  This provides
     * interoperability with legacy APIs that require an enumeration
     * as input.
     *
     * @param  <T> the class of the objects in the collection
     * @param c the collection for which an enumeration is to be returned.
     * @return an enumeration over the specified collection.
     * @see Enumeration
     */
    public static <T> Enumeration<T> enumeration(final Collection<T> c) {
        return new Enumeration<T>() {
            private final Iterator<T> i = c.iterator();

            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public T nextElement() {
                return i.next();
            }
        };
    }

    /**
     * Returns an array list containing the elements returned by the
     * specified enumeration in the order they are returned by the
     * enumeration.  This method provides interoperability between
     * legacy APIs that return enumerations and new APIs that require
     * collections.
     *
     * @param <T> the class of the objects returned by the enumeration
     * @param e enumeration providing elements for the returned
     *          array list
     * @return an array list containing the elements returned
     *         by the specified enumeration.
     * @since 1.4
     * @see Enumeration
     * @see ArrayList
     */
    public static <T> ArrayList<T> list(Enumeration<T> e) {
        ArrayList<T> l = new ArrayList<>();
        while (e.hasMoreElements())
            l.add(e.nextElement());
        return l;
    }

    /**
     * Returns true if the specified arguments are equal, or both null.
     *
     * NB: Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    static boolean eq(Object o1, Object o2) {
        return o1==null ? o2==null : o1.equals(o2);
    }

    /**
     * Returns the number of elements in the specified collection equal to the
     * specified object.  More formally, returns the number of elements
     * <tt>e</tt> in the collection such that
     * <tt>(o == null ? e == null : o.equals(e))</tt>.
     *
     * @param c the collection in which to determine the frequency
     *     of <tt>o</tt>
     * @param o the object whose frequency is to be determined
     * @return the number of elements in {@code c} equal to {@code o}
     * @throws NullPointerException if <tt>c</tt> is null
     * @since 1.5
     */
    public static int frequency(Collection<?> c, Object o) {
        int result = 0;
        if (o == null) {
            for (Object e : c)
                if (e == null)
                    result++;
        } else {
            for (Object e : c)
                if (o.equals(e))
                    result++;
        }
        return result;
    }

    /**
     * Returns {@code true} if the two specified collections have no
     * elements in common.
     *
     * <p>Care must be exercised if this method is used on collections that
     * do not comply with the general contract for {@code Collection}.
     * Implementations may elect to iterate over either collection and test
     * for containment in the other collection (or to perform any equivalent
     * computation).  If either collection uses a nonstandard equality test
     * (as does a {@link SortedSet} whose ordering is not <em>compatible with
     * equals</em>, or the key set of an {@link IdentityHashMap}), both
     * collections must use the same nonstandard equality test, or the
     * result of this method is undefined.
     *
     * <p>Care must also be exercised when using collections that have
     * restrictions on the elements that they may contain. Collection
     * implementations are allowed to throw exceptions for any operation
     * involving elements they deem ineligible. For absolute safety the
     * specified collections should contain only elements which are
     * eligible elements for both collections.
     *
     * <p>Note that it is permissible to pass the same collection in both
     * parameters, in which case the method will return {@code true} if and
     * only if the collection is empty.
     *
     * @param c1 a collection
     * @param c2 a collection
     * @return {@code true} if the two specified collections have no
     * elements in common.
     * @throws NullPointerException if either collection is {@code null}.
     * @throws NullPointerException if one collection contains a {@code null}
     * element and {@code null} is not an eligible element for the other collection.
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if one collection contains an element that is
     * of a type which is ineligible for the other collection.
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @since 1.5
     */
    public static boolean disjoint(Collection<?> c1, Collection<?> c2) {
        // The collection to be used for contains(). Preference is given to
        // the collection who's contains() has lower O() complexity.
        Collection<?> contains = c2;
        // The collection to be iterated. If the collections' contains() impl
        // are of different O() complexity, the collection with slower
        // contains() will be used for iteration. For collections who's
        // contains() are of the same complexity then best performance is
        // achieved by iterating the smaller collection.
        Collection<?> iterate = c1;

        // Performance optimization cases. The heuristics:
        //   1. Generally iterate over c1.
        //   2. If c1 is a Set then iterate over c2.
        //   3. If either collection is empty then result is always true.
        //   4. Iterate over the smaller Collection.
        if (c1 instanceof Set) {
            // Use c1 for contains as a Set's contains() is expected to perform
            // better than O(N/2)
            iterate = c2;
            contains = c1;
        } else if (!(c2 instanceof Set)) {
            // Both are mere Collections. Iterate over smaller collection.
            // Example: If c1 contains 3 elements and c2 contains 50 elements and
            // assuming contains() requires ceiling(N/2) comparisons then
            // checking for all c1 elements in c2 would require 75 comparisons
            // (3 * ceiling(50/2)) vs. checking all c2 elements in c1 requiring
            // 100 comparisons (50 * ceiling(3/2)).
            int c1size = c1.size();
            int c2size = c2.size();
            if (c1size == 0 || c2size == 0) {
                // At least one collection is empty. Nothing will match.
                return true;
            }

            if (c1size > c2size) {
                iterate = c2;
                contains = c1;
            }
        }

        for (Object e : iterate) {
            if (contains.contains(e)) {
               // Found a common element. Collections are not disjoint.
                return false;
            }
        }

        // No common elements were found.
        return true;
    }

    /**
     * Adds all of the specified elements to the specified collection.
     * Elements to be added may be specified individually or as an array.
     * The behavior of this convenience method is identical to that of
     * <tt>c.addAll(Arrays.asList(elements))</tt>, but this method is likely
     * to run significantly faster under most implementations.
     *
     * <p>When elements are specified individually, this method provides a
     * convenient way to add a few elements to an existing collection:
     * <pre>
     *     Collections.addAll(flavors, "Peaches 'n Plutonium", "Rocky Racoon");
     * </pre>
     *
     * @param  <T> the class of the elements to add and of the collection
     * @param c the collection into which <tt>elements</tt> are to be inserted
     * @param elements the elements to insert into <tt>c</tt>
     * @return <tt>true</tt> if the collection changed as a result of the call
     * @throws UnsupportedOperationException if <tt>c</tt> does not support
     *         the <tt>add</tt> operation
     * @throws NullPointerException if <tt>elements</tt> contains one or more
     *         null values and <tt>c</tt> does not permit null elements, or
     *         if <tt>c</tt> or <tt>elements</tt> are <tt>null</tt>
     * @throws IllegalArgumentException if some property of a value in
     *         <tt>elements</tt> prevents it from being added to <tt>c</tt>
     * @see Collection#addAll(Collection)
     * @since 1.5
     */
    @SafeVarargs
    public static <T> boolean addAll(Collection<? super T> c, T... elements) {
        boolean result = false;
        for (T element : elements)
            result |= c.add(element);
        return result;
    }

    /**
     * Returns a set backed by the specified map.  The resulting set displays
     * the same ordering, concurrency, and performance characteristics as the
     * backing map.  In essence, this factory method provides a {@link Set}
     * implementation corresponding to any {@link Map} implementation.  There
     * is no need to use this method on a {@link Map} implementation that
     * already has a corresponding {@link Set} implementation (such as {@link
     * HashMap} or {@link TreeMap}).
     *
     * <p>Each method invocation on the set returned by this method results in
     * exactly one method invocation on the backing map or its <tt>keySet</tt>
     * view, with one exception.  The <tt>addAll</tt> method is implemented
     * as a sequence of <tt>put</tt> invocations on the backing map.
     *
     * <p>The specified map must be empty at the time this method is invoked,
     * and should not be accessed directly after this method returns.  These
     * conditions are ensured if the map is created empty, passed directly
     * to this method, and no reference to the map is retained, as illustrated
     * in the following code fragment:
     * <pre>
     *    Set&lt;Object&gt; weakHashSet = Collections.newSetFromMap(
     *        new WeakHashMap&lt;Object, Boolean&gt;());
     * </pre>
     *
     * @param <E> the class of the map keys and of the objects in the
     *        returned set
     * @param map the backing map
     * @return the set backed by the map
     * @throws IllegalArgumentException if <tt>map</tt> is not empty
     * @since 1.6
     */
    public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
        return new SetFromMap<>(map);
    }

    /**
     * @serial include
     */
    private static class SetFromMap<E> extends AbstractSet<E>
        implements Set<E>, Serializable
    {
        private final Map<E, Boolean> m;  // The backing map
        private transient Set<E> s;       // Its keySet

        SetFromMap(Map<E, Boolean> map) {
            if (!map.isEmpty())
                throw new IllegalArgumentException("Map is non-empty");
            m = map;
            s = map.keySet();
        }

        public void clear()               {        m.clear(); }
        public int size()                 { return m.size(); }
        public boolean isEmpty()          { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public boolean remove(Object o)   { return m.remove(o) != null; }
        public boolean add(E e) { return m.put(e, Boolean.TRUE) == null; }
        public Iterator<E> iterator()     { return s.iterator(); }
        public Object[] toArray()         { return s.toArray(); }
        public <T> T[] toArray(T[] a)     { return s.toArray(a); }
        public String toString()          { return s.toString(); }
        public int hashCode()             { return s.hashCode(); }
        public boolean equals(Object o)   { return o == this || s.equals(o); }
        public boolean containsAll(Collection<?> c) {return s.containsAll(c);}
        public boolean removeAll(Collection<?> c)   {return s.removeAll(c);}
        public boolean retainAll(Collection<?> c)   {return s.retainAll(c);}
        // addAll is the only inherited implementation

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            s.forEach(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return s.removeIf(filter);
        }

        @Override
        public Spliterator<E> spliterator() {return s.spliterator();}
        @Override
        public Stream<E> stream()           {return s.stream();}
        @Override
        public Stream<E> parallelStream()   {return s.parallelStream();}

        private static final long serialVersionUID = 2454657854757543876L;

        private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException
        {
            stream.defaultReadObject();
            s = m.keySet();
        }
    }

    /**
     * Returns a view of a {@link Deque} as a Last-in-first-out (Lifo)
     * {@link Queue}. Method <tt>add</tt> is mapped to <tt>push</tt>,
     * <tt>remove</tt> is mapped to <tt>pop</tt> and so on. This
     * view can be useful when you would like to use a method
     * requiring a <tt>Queue</tt> but you need Lifo ordering.
     *
     * <p>Each method invocation on the queue returned by this method
     * results in exactly one method invocation on the backing deque, with
     * one exception.  The {@link Queue#addAll addAll} method is
     * implemented as a sequence of {@link Deque#addFirst addFirst}
     * invocations on the backing deque.
     *
     * @param  <T> the class of the objects in the deque
     * @param deque the deque
     * @return the queue
     * @since  1.6
     */
    public static <T> Queue<T> asLifoQueue(Deque<T> deque) {
        return new AsLIFOQueue<>(deque);
    }

    /**
     * @serial include
     */
    static class AsLIFOQueue<E> extends AbstractQueue<E>
        implements Queue<E>, Serializable {
        private static final long serialVersionUID = 1802017725587941708L;
        private final Deque<E> q;
        AsLIFOQueue(Deque<E> q)           { this.q = q; }
        public boolean add(E e)           { q.addFirst(e); return true; }
        public boolean offer(E e)         { return q.offerFirst(e); }
        public E poll()                   { return q.pollFirst(); }
        public E remove()                 { return q.removeFirst(); }
        public E peek()                   { return q.peekFirst(); }
        public E element()                { return q.getFirst(); }
        public void clear()               {        q.clear(); }
        public int size()                 { return q.size(); }
        public boolean isEmpty()          { return q.isEmpty(); }
        public boolean contains(Object o) { return q.contains(o); }
        public boolean remove(Object o)   { return q.remove(o); }
        public Iterator<E> iterator()     { return q.iterator(); }
        public Object[] toArray()         { return q.toArray(); }
        public <T> T[] toArray(T[] a)     { return q.toArray(a); }
        public String toString()          { return q.toString(); }
        public boolean containsAll(Collection<?> c) {return q.containsAll(c);}
        public boolean removeAll(Collection<?> c)   {return q.removeAll(c);}
        public boolean retainAll(Collection<?> c)   {return q.retainAll(c);}
        // We use inherited addAll; forwarding addAll would be wrong

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {q.forEach(action);}
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return q.removeIf(filter);
        }
        @Override
        public Spliterator<E> spliterator() {return q.spliterator();}
        @Override
        public Stream<E> stream()           {return q.stream();}
        @Override
        public Stream<E> parallelStream()   {return q.parallelStream();}
    }
}
/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util;

import java.lang.reflect.Array;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching). This class also contains a static factory
 * that allows arrays to be viewed as lists.
 *
 * <p>The methods in this class all throw a {@code NullPointerException},
 * if the specified array reference is null, except where noted.
 *
 * <p>The documentation for the methods contained in this class includes
 * briefs description of the <i>implementations</i>. Such descriptions should
 * be regarded as <i>implementation notes</i>, rather than parts of the
 * <i>specification</i>. Implementors should feel free to substitute other
 * algorithms, so long as the specification itself is adhered to. (For
 * example, the algorithm used by {@code sort(Object[])} does not have to be
 * a MergeSort, but it does have to be <i>stable</i>.)
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Josh Bloch
 * @author Neal Gafter
 * @author John Rose
 * @since  1.2
 */
public class Arrays {

    /**
     * The minimum array length below which a parallel sorting
     * algorithm will not further partition the sorting task. Using
     * smaller sizes typically results in memory contention across
     * tasks that makes parallel speedups unlikely.
     */
    private static final int MIN_ARRAY_SORT_GRAN = 1 << 13;

    // Suppresses default constructor, ensuring non-instantiability.
    private Arrays() {}

    /**
     * A comparator that implements the natural ordering of a group of
     * mutually comparable elements. May be used when a supplied
     * comparator is null. To simplify code-sharing within underlying
     * implementations, the compare method only declares type Object
     * for its second argument.
     *
     * Arrays class implementor's note: It is an empirical matter
     * whether ComparableTimSort offers any performance benefit over
     * TimSort used with this comparator.  If not, you are better off
     * deleting or bypassing ComparableTimSort.  There is currently no
     * empirical case for separating them for parallel sorting, so all
     * public Object parallelSort methods use the same comparator
     * based implementation.
     */
    static final class NaturalOrder implements Comparator<Object> {
        @SuppressWarnings("unchecked")
        public int compare(Object first, Object second) {
            return ((Comparable<Object>)first).compareTo(second);
        }
        static final NaturalOrder INSTANCE = new NaturalOrder();
    }

    /**
     * Checks that {@code fromIndex} and {@code toIndex} are in
     * the range and throws an exception if they aren't.
     */
    private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }

    /*
     * Sorting methods. Note that all public "sort" methods take the
     * same form: Performing argument checks if necessary, and then
     * expanding arguments into those required for the internal
     * implementation methods residing in other package-private
     * classes (except for legacyMergeSort, included in this class).
     */

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(int[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(int[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(long[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(short[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(short[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(char[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(char[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(byte[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>The {@code <} relation does not provide a total order on all float
     * values: {@code -0.0f == 0.0f} is {@code true} and a {@code Float.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Float#compareTo}: {@code -0.0f} is treated as less than value
     * {@code 0.0f} and {@code Float.NaN} is considered greater than any
     * other value and all {@code Float.NaN} values are considered equal.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(float[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>The {@code <} relation does not provide a total order on all float
     * values: {@code -0.0f == 0.0f} is {@code true} and a {@code Float.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Float#compareTo}: {@code -0.0f} is treated as less than value
     * {@code 0.0f} and {@code Float.NaN} is considered greater than any
     * other value and all {@code Float.NaN} values are considered equal.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(float[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>The {@code <} relation does not provide a total order on all double
     * values: {@code -0.0d == 0.0d} is {@code true} and a {@code Double.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Double#compareTo}: {@code -0.0d} is treated as less than value
     * {@code 0.0d} and {@code Double.NaN} is considered greater than any
     * other value and all {@code Double.NaN} values are considered equal.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     */
    public static void sort(double[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty.
     *
     * <p>The {@code <} relation does not provide a total order on all double
     * values: {@code -0.0d == 0.0d} is {@code true} and a {@code Double.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Double#compareTo}: {@code -0.0d} is treated as less than value
     * {@code 0.0d} and {@code Double.NaN} is considered greater than any
     * other value and all {@code Double.NaN} values are considered equal.
     *
     * <p>Implementation note: The sorting algorithm is a Dual-Pivot Quicksort
     * by Vladimir Yaroslavskiy, Jon Bentley, and Joshua Bloch. This algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically
     * faster than traditional (one-pivot) Quicksort implementations.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(double[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(byte[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(byte[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(byte[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1);
        else
            new ArraysParallelSortHelpers.FJByte.Sorter
                (null, a, new byte[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(byte[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(byte[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(byte[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1);
        else
            new ArraysParallelSortHelpers.FJByte.Sorter
                (null, a, new byte[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(char[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(char[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(char[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJChar.Sorter
                (null, a, new char[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
      @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(char[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(char[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(char[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJChar.Sorter
                (null, a, new char[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(short[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(short[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(short[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJShort.Sorter
                (null, a, new short[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(short[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(short[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(short[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJShort.Sorter
                (null, a, new short[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(int[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(int[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(int[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJInt.Sorter
                (null, a, new int[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(int[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(int[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(int[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJInt.Sorter
                (null, a, new int[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(long[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(long[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(long[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJLong.Sorter
                (null, a, new long[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(long[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(long[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJLong.Sorter
                (null, a, new long[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>The {@code <} relation does not provide a total order on all float
     * values: {@code -0.0f == 0.0f} is {@code true} and a {@code Float.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Float#compareTo}: {@code -0.0f} is treated as less than value
     * {@code 0.0f} and {@code Float.NaN} is considered greater than any
     * other value and all {@code Float.NaN} values are considered equal.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(float[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(float[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(float[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJFloat.Sorter
                (null, a, new float[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
     * <p>The {@code <} relation does not provide a total order on all float
     * values: {@code -0.0f == 0.0f} is {@code true} and a {@code Float.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Float#compareTo}: {@code -0.0f} is treated as less than value
     * {@code 0.0f} and {@code Float.NaN} is considered greater than any
     * other value and all {@code Float.NaN} values are considered equal.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(float[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(float[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(float[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJFloat.Sorter
                (null, a, new float[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>The {@code <} relation does not provide a total order on all double
     * values: {@code -0.0d == 0.0d} is {@code true} and a {@code Double.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Double#compareTo}: {@code -0.0d} is treated as less than value
     * {@code 0.0d} and {@code Double.NaN} is considered greater than any
     * other value and all {@code Double.NaN} values are considered equal.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(double[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(double[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param a the array to be sorted
     *
     * @since 1.8
     */
    public static void parallelSort(double[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJDouble.Sorter
                (null, a, new double[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified range of the array into ascending numerical order.
     * The range to be sorted extends from the index {@code fromIndex},
     * inclusive, to the index {@code toIndex}, exclusive. If
     * {@code fromIndex == toIndex}, the range to be sorted is empty.
     *
     * <p>The {@code <} relation does not provide a total order on all double
     * values: {@code -0.0d == 0.0d} is {@code true} and a {@code Double.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Double#compareTo}: {@code -0.0d} is treated as less than value
     * {@code 0.0d} and {@code Double.NaN} is considered greater than any
     * other value and all {@code Double.NaN} values are considered equal.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(double[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(double[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     *
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(double[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJDouble.Sorter
                (null, a, new double[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * Sorts the specified array of objects into ascending order, according
     * to the {@linkplain Comparable natural ordering} of its elements.
     * All elements in the array must implement the {@link Comparable}
     * interface.  Furthermore, all elements in the array must be
     * <i>mutually comparable</i> (that is, {@code e1.compareTo(e2)} must
     * not throw a {@code ClassCastException} for any elements {@code e1}
     * and {@code e2} in the array).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(Object[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(Object[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param <T> the class of the objects to be sorted
     * @param a the array to be sorted
     *
     * @throws ClassCastException if the array contains elements that are not
     *         <i>mutually comparable</i> (for example, strings and integers)
     * @throws IllegalArgumentException (optional) if the natural
     *         ordering of the array elements is found to violate the
     *         {@link Comparable} contract
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void parallelSort(T[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, 0, n, NaturalOrder.INSTANCE, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 0, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, NaturalOrder.INSTANCE).invoke();
    }

    /**
     * Sorts the specified range of the specified array of objects into
     * ascending order, according to the
     * {@linkplain Comparable natural ordering} of its
     * elements.  The range to be sorted extends from index
     * {@code fromIndex}, inclusive, to index {@code toIndex}, exclusive.
     * (If {@code fromIndex==toIndex}, the range to be sorted is empty.)  All
     * elements in this range must implement the {@link Comparable}
     * interface.  Furthermore, all elements in this range must be <i>mutually
     * comparable</i> (that is, {@code e1.compareTo(e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the array).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(Object[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(Object[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param <T> the class of the objects to be sorted
     * @param a the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted
     * @param toIndex the index of the last element (exclusive) to be sorted
     * @throws IllegalArgumentException if {@code fromIndex > toIndex} or
     *         (optional) if the natural ordering of the array elements is
     *         found to violate the {@link Comparable} contract
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex < 0} or
     *         {@code toIndex > a.length}
     * @throws ClassCastException if the array contains elements that are
     *         not <i>mutually comparable</i> (for example, strings and
     *         integers).
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>>
    void parallelSort(T[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, fromIndex, toIndex, NaturalOrder.INSTANCE, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 fromIndex, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, NaturalOrder.INSTANCE).invoke();
    }

    /**
     * Sorts the specified array of objects according to the order induced by
     * the specified comparator.  All elements in the array must be
     * <i>mutually comparable</i> by the specified comparator (that is,
     * {@code c.compare(e1, e2)} must not throw a {@code ClassCastException}
     * for any elements {@code e1} and {@code e2} in the array).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(Object[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(Object[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param <T> the class of the objects to be sorted
     * @param a the array to be sorted
     * @param cmp the comparator to determine the order of the array.  A
     *        {@code null} value indicates that the elements'
     *        {@linkplain Comparable natural ordering} should be used.
     * @throws ClassCastException if the array contains elements that are
     *         not <i>mutually comparable</i> using the specified comparator
     * @throws IllegalArgumentException (optional) if the comparator is
     *         found to violate the {@link java.util.Comparator} contract
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T> void parallelSort(T[] a, Comparator<? super T> cmp) {
        if (cmp == null)
            cmp = NaturalOrder.INSTANCE;
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, 0, n, cmp, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 0, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, cmp).invoke();
    }

    /**
     * Sorts the specified range of the specified array of objects according
     * to the order induced by the specified comparator.  The range to be
     * sorted extends from index {@code fromIndex}, inclusive, to index
     * {@code toIndex}, exclusive.  (If {@code fromIndex==toIndex}, the
     * range to be sorted is empty.)  All elements in the range must be
     * <i>mutually comparable</i> by the specified comparator (that is,
     * {@code c.compare(e1, e2)} must not throw a {@code ClassCastException}
     * for any elements {@code e1} and {@code e2} in the range).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(Object[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(Object[]) Arrays.sort} method. The algorithm requires a working
     * space no greater than the size of the specified range of the original
     * array. The {@link ForkJoinPool#commonPool() ForkJoin common pool} is
     * used to execute any parallel tasks.
     *
     * @param <T> the class of the objects to be sorted
     * @param a the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted
     * @param toIndex the index of the last element (exclusive) to be sorted
     * @param cmp the comparator to determine the order of the array.  A
     *        {@code null} value indicates that the elements'
     *        {@linkplain Comparable natural ordering} should be used.
     * @throws IllegalArgumentException if {@code fromIndex > toIndex} or
     *         (optional) if the natural ordering of the array elements is
     *         found to violate the {@link Comparable} contract
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex < 0} or
     *         {@code toIndex > a.length}
     * @throws ClassCastException if the array contains elements that are
     *         not <i>mutually comparable</i> (for example, strings and
     *         integers).
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T> void parallelSort(T[] a, int fromIndex, int toIndex,
                                        Comparator<? super T> cmp) {
        rangeCheck(a.length, fromIndex, toIndex);
        if (cmp == null)
            cmp = NaturalOrder.INSTANCE;
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, fromIndex, toIndex, cmp, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 fromIndex, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, cmp).invoke();
    }

    /*
     * Sorting of complex type arrays.
     */

    /**
     * Old merge sort implementation can be selected (for
     * compatibility with broken comparators) using a system property.
     * Cannot be a static boolean in the enclosing class due to
     * circular dependencies. To be removed in a future release.
     */
    static final class LegacyMergeSort {
        private static final boolean userRequested =
            java.security.AccessController.doPrivileged(
                new sun.security.action.GetBooleanAction(
                    "java.util.Arrays.useLegacyMergeSort")).booleanValue();
    }

    /**
     * Sorts the specified array of objects into ascending order, according
     * to the {@linkplain Comparable natural ordering} of its elements.
     * All elements in the array must implement the {@link Comparable}
     * interface.  Furthermore, all elements in the array must be
     * <i>mutually comparable</i> (that is, {@code e1.compareTo(e2)} must
     * not throw a {@code ClassCastException} for any elements {@code e1}
     * and {@code e2} in the array).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * <p>Implementation note: This implementation is a stable, adaptive,
     * iterative mergesort that requires far fewer than n lg(n) comparisons
     * when the input array is partially sorted, while offering the
     * performance of a traditional mergesort when the input array is
     * randomly ordered.  If the input array is nearly sorted, the
     * implementation requires approximately n comparisons.  Temporary
     * storage requirements vary from a small constant for nearly sorted
     * input arrays to n/2 object references for randomly ordered input
     * arrays.
     *
     * <p>The implementation takes equal advantage of ascending and
     * descending order in its input array, and can take advantage of
     * ascending and descending order in different parts of the the same
     * input array.  It is well-suited to merging two or more sorted arrays:
     * simply concatenate the arrays and sort the resulting array.
     *
     * <p>The implementation was adapted from Tim Peters's list sort for Python
     * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>).  It uses techniques from Peter McIlroy's "Optimistic
     * Sorting and Information Theoretic Complexity", in Proceedings of the
     * Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
     * January 1993.
     *
     * @param a the array to be sorted
     * @throws ClassCastException if the array contains elements that are not
     *         <i>mutually comparable</i> (for example, strings and integers)
     * @throws IllegalArgumentException (optional) if the natural
     *         ordering of the array elements is found to violate the
     *         {@link Comparable} contract
     */
    public static void sort(Object[] a) {
        if (LegacyMergeSort.userRequested)
            legacyMergeSort(a);
        else
            ComparableTimSort.sort(a, 0, a.length, null, 0, 0);
    }

    /** To be removed in a future release. */
    private static void legacyMergeSort(Object[] a) {
        Object[] aux = a.clone();
        mergeSort(aux, a, 0, a.length, 0);
    }

    /**
     * Sorts the specified range of the specified array of objects into
     * ascending order, according to the
     * {@linkplain Comparable natural ordering} of its
     * elements.  The range to be sorted extends from index
     * {@code fromIndex}, inclusive, to index {@code toIndex}, exclusive.
     * (If {@code fromIndex==toIndex}, the range to be sorted is empty.)  All
     * elements in this range must implement the {@link Comparable}
     * interface.  Furthermore, all elements in this range must be <i>mutually
     * comparable</i> (that is, {@code e1.compareTo(e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the array).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * <p>Implementation note: This implementation is a stable, adaptive,
     * iterative mergesort that requires far fewer than n lg(n) comparisons
     * when the input array is partially sorted, while offering the
     * performance of a traditional mergesort when the input array is
     * randomly ordered.  If the input array is nearly sorted, the
     * implementation requires approximately n comparisons.  Temporary
     * storage requirements vary from a small constant for nearly sorted
     * input arrays to n/2 object references for randomly ordered input
     * arrays.
     *
     * <p>The implementation takes equal advantage of ascending and
     * descending order in its input array, and can take advantage of
     * ascending and descending order in different parts of the the same
     * input array.  It is well-suited to merging two or more sorted arrays:
     * simply concatenate the arrays and sort the resulting array.
     *
     * <p>The implementation was adapted from Tim Peters's list sort for Python
     * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>).  It uses techniques from Peter McIlroy's "Optimistic
     * Sorting and Information Theoretic Complexity", in Proceedings of the
     * Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
     * January 1993.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted
     * @param toIndex the index of the last element (exclusive) to be sorted
     * @throws IllegalArgumentException if {@code fromIndex > toIndex} or
     *         (optional) if the natural ordering of the array elements is
     *         found to violate the {@link Comparable} contract
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex < 0} or
     *         {@code toIndex > a.length}
     * @throws ClassCastException if the array contains elements that are
     *         not <i>mutually comparable</i> (for example, strings and
     *         integers).
     */
    public static void sort(Object[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        if (LegacyMergeSort.userRequested)
            legacyMergeSort(a, fromIndex, toIndex);
        else
            ComparableTimSort.sort(a, fromIndex, toIndex, null, 0, 0);
    }

    /** To be removed in a future release. */
    private static void legacyMergeSort(Object[] a,
                                        int fromIndex, int toIndex) {
        Object[] aux = copyOfRange(a, fromIndex, toIndex);
        mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
    }

    /**
     * Tuning parameter: list size at or below which insertion sort will be
     * used in preference to mergesort.
     * To be removed in a future release.
     */
    private static final int INSERTIONSORT_THRESHOLD = 7;

    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset to generate corresponding low, high in src
     * To be removed in a future release.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void mergeSort(Object[] src,
                                  Object[] dest,
                                  int low,
                                  int high,
                                  int off) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i=low; i<high; i++)
                for (int j=i; j>low &&
                         ((Comparable) dest[j-1]).compareTo(dest[j])>0; j--)
                    swap(dest, j, j-1);
            return;
        }

        // Recursively sort halves of dest into src
        int destLow  = low;
        int destHigh = high;
        low  += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (((Comparable)src[mid-1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && ((Comparable)src[p]).compareTo(src[q])<=0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Sorts the specified array of objects according to the order induced by
     * the specified comparator.  All elements in the array must be
     * <i>mutually comparable</i> by the specified comparator (that is,
     * {@code c.compare(e1, e2)} must not throw a {@code ClassCastException}
     * for any elements {@code e1} and {@code e2} in the array).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * <p>Implementation note: This implementation is a stable, adaptive,
     * iterative mergesort that requires far fewer than n lg(n) comparisons
     * when the input array is partially sorted, while offering the
     * performance of a traditional mergesort when the input array is
     * randomly ordered.  If the input array is nearly sorted, the
     * implementation requires approximately n comparisons.  Temporary
     * storage requirements vary from a small constant for nearly sorted
     * input arrays to n/2 object references for randomly ordered input
     * arrays.
     *
     * <p>The implementation takes equal advantage of ascending and
     * descending order in its input array, and can take advantage of
     * ascending and descending order in different parts of the the same
     * input array.  It is well-suited to merging two or more sorted arrays:
     * simply concatenate the arrays and sort the resulting array.
     *
     * <p>The implementation was adapted from Tim Peters's list sort for Python
     * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>).  It uses techniques from Peter McIlroy's "Optimistic
     * Sorting and Information Theoretic Complexity", in Proceedings of the
     * Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
     * January 1993.
     *
     * @param <T> the class of the objects to be sorted
     * @param a the array to be sorted
     * @param c the comparator to determine the order of the array.  A
     *        {@code null} value indicates that the elements'
     *        {@linkplain Comparable natural ordering} should be used.
     * @throws ClassCastException if the array contains elements that are
     *         not <i>mutually comparable</i> using the specified comparator
     * @throws IllegalArgumentException (optional) if the comparator is
     *         found to violate the {@link Comparator} contract
     */
    public static <T> void sort(T[] a, Comparator<? super T> c) {
        if (c == null) {
            sort(a);
        } else {
            if (LegacyMergeSort.userRequested)
                legacyMergeSort(a, c);
            else
                TimSort.sort(a, 0, a.length, c, null, 0, 0);
        }
    }

    /** To be removed in a future release. */
    private static <T> void legacyMergeSort(T[] a, Comparator<? super T> c) {
        T[] aux = a.clone();
        if (c==null)
            mergeSort(aux, a, 0, a.length, 0);
        else
            mergeSort(aux, a, 0, a.length, 0, c);
    }

    /**
     * Sorts the specified range of the specified array of objects according
     * to the order induced by the specified comparator.  The range to be
     * sorted extends from index {@code fromIndex}, inclusive, to index
     * {@code toIndex}, exclusive.  (If {@code fromIndex==toIndex}, the
     * range to be sorted is empty.)  All elements in the range must be
     * <i>mutually comparable</i> by the specified comparator (that is,
     * {@code c.compare(e1, e2)} must not throw a {@code ClassCastException}
     * for any elements {@code e1} and {@code e2} in the range).
     *
     * <p>This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.
     *
     * <p>Implementation note: This implementation is a stable, adaptive,
     * iterative mergesort that requires far fewer than n lg(n) comparisons
     * when the input array is partially sorted, while offering the
     * performance of a traditional mergesort when the input array is
     * randomly ordered.  If the input array is nearly sorted, the
     * implementation requires approximately n comparisons.  Temporary
     * storage requirements vary from a small constant for nearly sorted
     * input arrays to n/2 object references for randomly ordered input
     * arrays.
     *
     * <p>The implementation takes equal advantage of ascending and
     * descending order in its input array, and can take advantage of
     * ascending and descending order in different parts of the the same
     * input array.  It is well-suited to merging two or more sorted arrays:
     * simply concatenate the arrays and sort the resulting array.
     *
     * <p>The implementation was adapted from Tim Peters's list sort for Python
     * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>).  It uses techniques from Peter McIlroy's "Optimistic
     * Sorting and Information Theoretic Complexity", in Proceedings of the
     * Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
     * January 1993.
     *
     * @param <T> the class of the objects to be sorted
     * @param a the array to be sorted
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted
     * @param toIndex the index of the last element (exclusive) to be sorted
     * @param c the comparator to determine the order of the array.  A
     *        {@code null} value indicates that the elements'
     *        {@linkplain Comparable natural ordering} should be used.
     * @throws ClassCastException if the array contains elements that are not
     *         <i>mutually comparable</i> using the specified comparator.
     * @throws IllegalArgumentException if {@code fromIndex > toIndex} or
     *         (optional) if the comparator is found to violate the
     *         {@link Comparator} contract
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex < 0} or
     *         {@code toIndex > a.length}
     */
    public static <T> void sort(T[] a, int fromIndex, int toIndex,
                                Comparator<? super T> c) {
        if (c == null) {
            sort(a, fromIndex, toIndex);
        } else {
            rangeCheck(a.length, fromIndex, toIndex);
            if (LegacyMergeSort.userRequested)
                legacyMergeSort(a, fromIndex, toIndex, c);
            else
                TimSort.sort(a, fromIndex, toIndex, c, null, 0, 0);
        }
    }

    /** To be removed in a future release. */
    private static <T> void legacyMergeSort(T[] a, int fromIndex, int toIndex,
                                            Comparator<? super T> c) {
        T[] aux = copyOfRange(a, fromIndex, toIndex);
        if (c==null)
            mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
        else
            mergeSort(aux, a, fromIndex, toIndex, -fromIndex, c);
    }

    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset into src corresponding to low in dest
     * To be removed in a future release.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void mergeSort(Object[] src,
                                  Object[] dest,
                                  int low, int high, int off,
                                  Comparator c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i=low; i<high; i++)
                for (int j=i; j>low && c.compare(dest[j-1], dest[j])>0; j--)
                    swap(dest, j, j-1);
            return;
        }

        // Recursively sort halves of dest into src
        int destLow  = low;
        int destHigh = high;
        low  += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off, c);
        mergeSort(dest, src, mid, high, -off, c);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid-1], src[mid]) <= 0) {
           System.arraycopy(src, low, dest, destLow, length);
           return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    // Parallel prefix

    /**
     * Cumulates, in parallel, each element of the given array in place,
     * using the supplied function. For example if the array initially
     * holds {@code [2, 1, 0, 3]} and the operation performs addition,
     * then upon return the array holds {@code [2, 3, 3, 6]}.
     * Parallel prefix computation is usually more efficient than
     * sequential loops for large arrays.
     *
     * @param <T> the class of the objects in the array
     * @param array the array, which is modified in-place by this method
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static <T> void parallelPrefix(T[] array, BinaryOperator<T> op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.CumulateTask<>
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * Performs {@link #parallelPrefix(Object[], BinaryOperator)}
     * for the given subrange of the array.
     *
     * @param <T> the class of the objects in the array
     * @param array the array
     * @param fromIndex the index of the first element, inclusive
     * @param toIndex the index of the last element, exclusive
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > array.length}
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static <T> void parallelPrefix(T[] array, int fromIndex,
                                          int toIndex, BinaryOperator<T> op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.CumulateTask<>
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    /**
     * Cumulates, in parallel, each element of the given array in place,
     * using the supplied function. For example if the array initially
     * holds {@code [2, 1, 0, 3]} and the operation performs addition,
     * then upon return the array holds {@code [2, 3, 3, 6]}.
     * Parallel prefix computation is usually more efficient than
     * sequential loops for large arrays.
     *
     * @param array the array, which is modified in-place by this method
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static void parallelPrefix(long[] array, LongBinaryOperator op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.LongCumulateTask
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * Performs {@link #parallelPrefix(long[], LongBinaryOperator)}
     * for the given subrange of the array.
     *
     * @param array the array
     * @param fromIndex the index of the first element, inclusive
     * @param toIndex the index of the last element, exclusive
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > array.length}
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static void parallelPrefix(long[] array, int fromIndex,
                                      int toIndex, LongBinaryOperator op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.LongCumulateTask
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    /**
     * Cumulates, in parallel, each element of the given array in place,
     * using the supplied function. For example if the array initially
     * holds {@code [2.0, 1.0, 0.0, 3.0]} and the operation performs addition,
     * then upon return the array holds {@code [2.0, 3.0, 3.0, 6.0]}.
     * Parallel prefix computation is usually more efficient than
     * sequential loops for large arrays.
     *
     * <p> Because floating-point operations may not be strictly associative,
     * the returned result may not be identical to the value that would be
     * obtained if the operation was performed sequentially.
     *
     * @param array the array, which is modified in-place by this method
     * @param op a side-effect-free function to perform the cumulation
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static void parallelPrefix(double[] array, DoubleBinaryOperator op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.DoubleCumulateTask
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * Performs {@link #parallelPrefix(double[], DoubleBinaryOperator)}
     * for the given subrange of the array.
     *
     * @param array the array
     * @param fromIndex the index of the first element, inclusive
     * @param toIndex the index of the last element, exclusive
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > array.length}
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static void parallelPrefix(double[] array, int fromIndex,
                                      int toIndex, DoubleBinaryOperator op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.DoubleCumulateTask
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    /**
     * Cumulates, in parallel, each element of the given array in place,
     * using the supplied function. For example if the array initially
     * holds {@code [2, 1, 0, 3]} and the operation performs addition,
     * then upon return the array holds {@code [2, 3, 3, 6]}.
     * Parallel prefix computation is usually more efficient than
     * sequential loops for large arrays.
     *
     * @param array the array, which is modified in-place by this method
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static void parallelPrefix(int[] array, IntBinaryOperator op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.IntCumulateTask
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * Performs {@link #parallelPrefix(int[], IntBinaryOperator)}
     * for the given subrange of the array.
     *
     * @param array the array
     * @param fromIndex the index of the first element, inclusive
     * @param toIndex the index of the last element, exclusive
     * @param op a side-effect-free, associative function to perform the
     * cumulation
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > array.length}
     * @throws NullPointerException if the specified array or function is null
     * @since 1.8
     */
    public static void parallelPrefix(int[] array, int fromIndex,
                                      int toIndex, IntBinaryOperator op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.IntCumulateTask
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    // Searching

    /**
     * Searches the specified array of longs for the specified value using the
     * binary search algorithm.  The array must be sorted (as
     * by the {@link #sort(long[])} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(long[] a, long key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of longs for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link #sort(long[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(long[] a, int fromIndex, int toIndex,
                                   long key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(long[] a, int fromIndex, int toIndex,
                                     long key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array of ints for the specified value using the
     * binary search algorithm.  The array must be sorted (as
     * by the {@link #sort(int[])} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(int[] a, int key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of ints for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link #sort(int[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(int[] a, int fromIndex, int toIndex,
                                   int key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
                                     int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array of shorts for the specified value using
     * the binary search algorithm.  The array must be sorted
     * (as by the {@link #sort(short[])} method) prior to making this call.  If
     * it is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(short[] a, short key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of shorts for the specified value using
     * the binary search algorithm.
     * The range must be sorted
     * (as by the {@link #sort(short[], int, int)} method)
     * prior to making this call.  If
     * it is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(short[] a, int fromIndex, int toIndex,
                                   short key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(short[] a, int fromIndex, int toIndex,
                                     short key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            short midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array of chars for the specified value using the
     * binary search algorithm.  The array must be sorted (as
     * by the {@link #sort(char[])} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(char[] a, char key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of chars for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link #sort(char[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(char[] a, int fromIndex, int toIndex,
                                   char key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(char[] a, int fromIndex, int toIndex,
                                     char key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            char midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array of bytes for the specified value using the
     * binary search algorithm.  The array must be sorted (as
     * by the {@link #sort(byte[])} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(byte[] a, byte key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of bytes for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link #sort(byte[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(byte[] a, int fromIndex, int toIndex,
                                   byte key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(byte[] a, int fromIndex, int toIndex,
                                     byte key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            byte midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array of doubles for the specified value using
     * the binary search algorithm.  The array must be sorted
     * (as by the {@link #sort(double[])} method) prior to making this call.
     * If it is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.  This method considers all NaN values to be
     * equivalent and equal.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(double[] a, double key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of doubles for the specified value using
     * the binary search algorithm.
     * The range must be sorted
     * (as by the {@link #sort(double[], int, int)} method)
     * prior to making this call.
     * If it is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.  This method considers all NaN values to be
     * equivalent and equal.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(double[] a, int fromIndex, int toIndex,
                                   double key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(double[] a, int fromIndex, int toIndex,
                                     double key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = a[mid];

            if (midVal < key)
                low = mid + 1;  // Neither val is NaN, thisVal is smaller
            else if (midVal > key)
                high = mid - 1; // Neither val is NaN, thisVal is larger
            else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                if (midBits == keyBits)     // Values are equal
                    return mid;             // Key found
                else if (midBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
                    low = mid + 1;
                else                        // (0.0, -0.0) or (NaN, !NaN)
                    high = mid - 1;
            }
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array of floats for the specified value using
     * the binary search algorithm. The array must be sorted
     * (as by the {@link #sort(float[])} method) prior to making this call. If
     * it is not sorted, the results are undefined. If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found. This method considers all NaN values to be
     * equivalent and equal.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key. Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public static int binarySearch(float[] a, float key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array of floats for the specified value using
     * the binary search algorithm.
     * The range must be sorted
     * (as by the {@link #sort(float[], int, int)} method)
     * prior to making this call. If
     * it is not sorted, the results are undefined. If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found. This method considers all NaN values to be
     * equivalent and equal.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key. Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(float[] a, int fromIndex, int toIndex,
                                   float key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(float[] a, int fromIndex, int toIndex,
                                     float key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            float midVal = a[mid];

            if (midVal < key)
                low = mid + 1;  // Neither val is NaN, thisVal is smaller
            else if (midVal > key)
                high = mid - 1; // Neither val is NaN, thisVal is larger
            else {
                int midBits = Float.floatToIntBits(midVal);
                int keyBits = Float.floatToIntBits(key);
                if (midBits == keyBits)     // Values are equal
                    return mid;             // Key found
                else if (midBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
                    low = mid + 1;
                else                        // (0.0, -0.0) or (NaN, !NaN)
                    high = mid - 1;
            }
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array for the specified object using the binary
     * search algorithm. The array must be sorted into ascending order
     * according to the
     * {@linkplain Comparable natural ordering}
     * of its elements (as by the
     * {@link #sort(Object[])} method) prior to making this call.
     * If it is not sorted, the results are undefined.
     * (If the array contains elements that are not mutually comparable (for
     * example, strings and integers), it <i>cannot</i> be sorted according
     * to the natural ordering of its elements, hence results are undefined.)
     * If the array contains multiple
     * elements equal to the specified object, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ClassCastException if the search key is not comparable to the
     *         elements of the array.
     */
    public static int binarySearch(Object[] a, Object key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * Searches a range of
     * the specified array for the specified object using the binary
     * search algorithm.
     * The range must be sorted into ascending order
     * according to the
     * {@linkplain Comparable natural ordering}
     * of its elements (as by the
     * {@link #sort(Object[], int, int)} method) prior to making this
     * call.  If it is not sorted, the results are undefined.
     * (If the range contains elements that are not mutually comparable (for
     * example, strings and integers), it <i>cannot</i> be sorted according
     * to the natural ordering of its elements, hence results are undefined.)
     * If the range contains multiple
     * elements equal to the specified object, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ClassCastException if the search key is not comparable to the
     *         elements of the array within the specified range.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(Object[] a, int fromIndex, int toIndex,
                                   Object key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // Like public version, but without range checks.
    private static int binarySearch0(Object[] a, int fromIndex, int toIndex,
                                     Object key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            @SuppressWarnings("rawtypes")
            Comparable midVal = (Comparable)a[mid];
            @SuppressWarnings("unchecked")
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order
     * according to the specified comparator (as by the
     * {@link #sort(Object[], Comparator) sort(T[], Comparator)}
     * method) prior to making this call.  If it is
     * not sorted, the results are undefined.
     * If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     *
     * @param <T> the class of the objects in the array
     * @param a the array to be searched
     * @param key the value to be searched for
     * @param c the comparator by which the array is ordered.  A
     *        <tt>null</tt> value indicates that the elements'
     *        {@linkplain Comparable natural ordering} should be used.
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ClassCastException if the array contains elements that are not
     *         <i>mutually comparable</i> using the specified comparator,
     *         or the search key is not comparable to the
     *         elements of the array using this comparator.
     */
    public static <T> int binarySearch(T[] a, T key, Comparator<? super T> c) {
        return binarySearch0(a, 0, a.length, key, c);
    }

    /**
     * Searches a range of
     * the specified array for the specified object using the binary
     * search algorithm.
     * The range must be sorted into ascending order
     * according to the specified comparator (as by the
     * {@link #sort(Object[], int, int, Comparator)
     * sort(T[], int, int, Comparator)}
     * method) prior to making this call.
     * If it is not sorted, the results are undefined.
     * If the range contains multiple elements equal to the specified object,
     * there is no guarantee which one will be found.
     *
     * @param <T> the class of the objects in the array
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *          searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @param c the comparator by which the array is ordered.  A
     *        <tt>null</tt> value indicates that the elements'
     *        {@linkplain Comparable natural ordering} should be used.
     * @return index of the search key, if it is contained in the array
     *         within the specified range;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element in the range greater than the key,
     *         or <tt>toIndex</tt> if all
     *         elements in the range are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ClassCastException if the range contains elements that are not
     *         <i>mutually comparable</i> using the specified comparator,
     *         or the search key is not comparable to the
     *         elements in the range using this comparator.
     * @throws IllegalArgumentException
     *         if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static <T> int binarySearch(T[] a, int fromIndex, int toIndex,
                                       T key, Comparator<? super T> c) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key, c);
    }

    // Like public version, but without range checks.
    private static <T> int binarySearch0(T[] a, int fromIndex, int toIndex,
                                         T key, Comparator<? super T> c) {
        if (c == null) {
            return binarySearch0(a, fromIndex, toIndex, key);
        }
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = a[mid];
            int cmp = c.compare(midVal, key);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    // Equality Testing

    /**
     * Returns <tt>true</tt> if the two specified arrays of longs are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(long[] a, long[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of ints are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(int[] a, int[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of shorts are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(short[] a, short a2[]) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of chars are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(char[] a, char[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of bytes are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(byte[] a, byte[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of booleans are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(boolean[] a, boolean[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of doubles are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * Two doubles <tt>d1</tt> and <tt>d2</tt> are considered equal if:
     * <pre>    <tt>new Double(d1).equals(new Double(d2))</tt></pre>
     * (Unlike the <tt>==</tt> operator, this method considers
     * <tt>NaN</tt> equals to itself, and 0.0d unequal to -0.0d.)
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     * @see Double#equals(Object)
     */
    public static boolean equals(double[] a, double[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (Double.doubleToLongBits(a[i])!=Double.doubleToLongBits(a2[i]))
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of floats are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * Two floats <tt>f1</tt> and <tt>f2</tt> are considered equal if:
     * <pre>    <tt>new Float(f1).equals(new Float(f2))</tt></pre>
     * (Unlike the <tt>==</tt> operator, this method considers
     * <tt>NaN</tt> equals to itself, and 0.0f unequal to -0.0f.)
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     * @see Float#equals(Object)
     */
    public static boolean equals(float[] a, float[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (Float.floatToIntBits(a[i])!=Float.floatToIntBits(a2[i]))
                return false;

        return true;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays of Objects are
     * <i>equal</i> to one another.  The two arrays are considered equal if
     * both arrays contain the same number of elements, and all corresponding
     * pairs of elements in the two arrays are equal.  Two objects <tt>e1</tt>
     * and <tt>e2</tt> are considered <i>equal</i> if <tt>(e1==null ? e2==null
     * : e1.equals(e2))</tt>.  In other words, the two arrays are equal if
     * they contain the same elements in the same order.  Also, two array
     * references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
    public static boolean equals(Object[] a, Object[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
            Object o1 = a[i];
            Object o2 = a2[i];
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }

        return true;
    }

    // Filling

    /**
     * Assigns the specified long value to each element of the specified array
     * of longs.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(long[] a, long val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified long value to each element of the specified
     * range of the specified array of longs.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(long[] a, int fromIndex, int toIndex, long val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified int value to each element of the specified array
     * of ints.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(int[] a, int val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified int value to each element of the specified
     * range of the specified array of ints.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(int[] a, int fromIndex, int toIndex, int val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified short value to each element of the specified array
     * of shorts.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(short[] a, short val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified short value to each element of the specified
     * range of the specified array of shorts.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(short[] a, int fromIndex, int toIndex, short val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified char value to each element of the specified array
     * of chars.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(char[] a, char val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified char value to each element of the specified
     * range of the specified array of chars.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(char[] a, int fromIndex, int toIndex, char val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified byte value to each element of the specified array
     * of bytes.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(byte[] a, byte val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified byte value to each element of the specified
     * range of the specified array of bytes.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(byte[] a, int fromIndex, int toIndex, byte val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified boolean value to each element of the specified
     * array of booleans.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(boolean[] a, boolean val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified boolean value to each element of the specified
     * range of the specified array of booleans.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(boolean[] a, int fromIndex, int toIndex,
                            boolean val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified double value to each element of the specified
     * array of doubles.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(double[] a, double val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified double value to each element of the specified
     * range of the specified array of doubles.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(double[] a, int fromIndex, int toIndex,double val){
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified float value to each element of the specified array
     * of floats.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     */
    public static void fill(float[] a, float val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified float value to each element of the specified
     * range of the specified array of floats.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     */
    public static void fill(float[] a, int fromIndex, int toIndex, float val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified Object reference to each element of the specified
     * array of Objects.
     *
     * @param a the array to be filled
     * @param val the value to be stored in all elements of the array
     * @throws ArrayStoreException if the specified value is not of a
     *         runtime type that can be stored in the specified array
     */
    public static void fill(Object[] a, Object val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * Assigns the specified Object reference to each element of the specified
     * range of the specified array of Objects.  The range to be filled
     * extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be filled is empty.)
     *
     * @param a the array to be filled
     * @param fromIndex the index of the first element (inclusive) to be
     *        filled with the specified value
     * @param toIndex the index of the last element (exclusive) to be
     *        filled with the specified value
     * @param val the value to be stored in all elements of the array
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *         <tt>toIndex &gt; a.length</tt>
     * @throws ArrayStoreException if the specified value is not of a
     *         runtime type that can be stored in the specified array
     */
    public static void fill(Object[] a, int fromIndex, int toIndex, Object val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    // Cloning

    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of exactly the same class as the original array.
     *
     * @param <T> the class of the objects in the array
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }

    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of the class <tt>newType</tt>.
     *
     * @param <U> the class of the objects in the original array
     * @param <T> the class of the objects in the returned array
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @param newType the class of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @throws ArrayStoreException if an element copied from
     *     <tt>original</tt> is not of a runtime type that can be stored in
     *     an array of class <tt>newType</tt>
     * @since 1.6
     */
    public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        @SuppressWarnings("unchecked")
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>(byte)0</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>(short)0</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static short[] copyOf(short[] original, int newLength) {
        short[] copy = new short[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>0</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static int[] copyOf(int[] original, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>0L</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static long[] copyOf(long[] original, int newLength) {
        long[] copy = new long[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with null characters (if necessary)
     * so the copy has the specified length.  For all indices that are valid
     * in both the original array and the copy, the two arrays will contain
     * identical values.  For any indices that are valid in the copy but not
     * the original, the copy will contain <tt>'\\u000'</tt>.  Such indices
     * will exist if and only if the specified length is greater than that of
     * the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with null characters
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static char[] copyOf(char[] original, int newLength) {
        char[] copy = new char[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>0f</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static float[] copyOf(float[] original, int newLength) {
        float[] copy = new float[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>0d</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static double[] copyOf(double[] original, int newLength) {
        double[] copy = new double[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified array, truncating or padding with <tt>false</tt> (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>false</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with false elements
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static boolean[] copyOf(boolean[] original, int newLength) {
        boolean[] copy = new boolean[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>null</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     * <p>
     * The resulting array is of exactly the same class as the original array.
     *
     * @param <T> the class of the objects in the array
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with nulls to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOfRange(T[] original, int from, int to) {
        return copyOfRange(original, from, to, (Class<? extends T[]>) original.getClass());
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>null</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     * The resulting array is of the class <tt>newType</tt>.
     *
     * @param <U> the class of the objects in the original array
     * @param <T> the class of the objects in the returned array
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @param newType the class of the copy to be returned
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with nulls to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @throws ArrayStoreException if an element copied from
     *     <tt>original</tt> is not of a runtime type that can be stored in
     *     an array of class <tt>newType</tt>.
     * @since 1.6
     */
    public static <T,U> T[] copyOfRange(U[] original, int from, int to, Class<? extends T[]> newType) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        @SuppressWarnings("unchecked")
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>(byte)0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>(short)0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static short[] copyOfRange(short[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        short[] copy = new short[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static int[] copyOfRange(int[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        int[] copy = new int[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>0L</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static long[] copyOfRange(long[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        long[] copy = new long[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>'\\u000'</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with null characters to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static char[] copyOfRange(char[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        char[] copy = new char[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>0f</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static float[] copyOfRange(float[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        float[] copy = new float[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>0d</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static double[] copyOfRange(double[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        double[] copy = new double[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>false</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with false elements to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static boolean[] copyOfRange(boolean[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        boolean[] copy = new boolean[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    // Misc

    /**
     * Returns a fixed-size list backed by the specified array.  (Changes to
     * the returned list "write through" to the array.)  This method acts
     * as bridge between array-based and collection-based APIs, in
     * combination with {@link Collection#toArray}.  The returned list is
     * serializable and implements {@link RandomAccess}.
     *
     * <p>This method also provides a convenient way to create a fixed-size
     * list initialized to contain several elements:
     * <pre>
     *     List&lt;String&gt; stooges = Arrays.asList("Larry", "Moe", "Curly");
     * </pre>
     *
     * @param <T> the class of the objects in the array
     * @param a the array by which the list will be backed
     * @return a list view of the specified array
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> asList(T... a) {
        return new ArrayList<>(a);
    }

    /**
     * @serial include
     */
    private static class ArrayList<E> extends AbstractList<E>
        implements RandomAccess, java.io.Serializable
    {
        private static final long serialVersionUID = -2764017481108945198L;
        private final E[] a;

        ArrayList(E[] array) {
            a = Objects.requireNonNull(array);
        }

        @Override
        public int size() {
            return a.length;
        }

        @Override
        public Object[] toArray() {
            return a.clone();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int size = size();
            if (a.length < size)
                return Arrays.copyOf(this.a, size,
                                     (Class<? extends T[]>) a.getClass());
            System.arraycopy(this.a, 0, a, 0, size);
            if (a.length > size)
                a[size] = null;
            return a;
        }

        @Override
        public E get(int index) {
            return a[index];
        }

        @Override
        public E set(int index, E element) {
            E oldValue = a[index];
            a[index] = element;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            E[] a = this.a;
            if (o == null) {
                for (int i = 0; i < a.length; i++)
                    if (a[i] == null)
                        return i;
            } else {
                for (int i = 0; i < a.length; i++)
                    if (o.equals(a[i]))
                        return i;
            }
            return -1;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public Spliterator<E> spliterator() {
            return Spliterators.spliterator(a, Spliterator.ORDERED);
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            for (E e : a) {
                action.accept(e);
            }
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            Objects.requireNonNull(operator);
            E[] a = this.a;
            for (int i = 0; i < a.length; i++) {
                a[i] = operator.apply(a[i]);
            }
        }

        @Override
        public void sort(Comparator<? super E> c) {
            Arrays.sort(a, c);
        }
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>long</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Long}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(long a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (long element : a) {
            int elementHash = (int)(element ^ (element >>> 32));
            result = 31 * result + elementHash;
        }

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two non-null <tt>int</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Integer}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(int a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (int element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>short</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Short}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(short a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (short element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>char</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Character}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(char a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (char element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>byte</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Byte}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(byte a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (byte element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>boolean</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Boolean}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(boolean a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (boolean element : a)
            result = 31 * result + (element ? 1231 : 1237);

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>float</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Float}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(float a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (float element : a)
            result = 31 * result + Float.floatToIntBits(element);

        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.
     * For any two <tt>double</tt> arrays <tt>a</tt> and <tt>b</tt>
     * such that <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is the same value that would be
     * obtained by invoking the {@link List#hashCode() <tt>hashCode</tt>}
     * method on a {@link List} containing a sequence of {@link Double}
     * instances representing the elements of <tt>a</tt> in the same order.
     * If <tt>a</tt> is <tt>null</tt>, this method returns 0.
     *
     * @param a the array whose hash value to compute
     * @return a content-based hash code for <tt>a</tt>
     * @since 1.5
     */
    public static int hashCode(double a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (double element : a) {
            long bits = Double.doubleToLongBits(element);
            result = 31 * result + (int)(bits ^ (bits >>> 32));
        }
        return result;
    }

    /**
     * Returns a hash code based on the contents of the specified array.  If
     * the array contains other arrays as elements, the hash code is based on
     * their identities rather than their contents.  It is therefore
     * acceptable to invoke this method on an array that contains itself as an
     * element,  either directly or indirectly through one or more levels of
     * arrays.
     *
     * <p>For any two arrays <tt>a</tt> and <tt>b</tt> such that
     * <tt>Arrays.equals(a, b)</tt>, it is also the case that
     * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
     *
     * <p>The value returned by this method is equal to the value that would
     * be returned by <tt>Arrays.asList(a).hashCode()</tt>, unless <tt>a</tt>
     * is <tt>null</tt>, in which case <tt>0</tt> is returned.
     *
     * @param a the array whose content-based hash code to compute
     * @return a content-based hash code for <tt>a</tt>
     * @see #deepHashCode(Object[])
     * @since 1.5
     */
    public static int hashCode(Object a[]) {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a)
            result = 31 * result + (element == null ? 0 : element.hashCode());

        return result;
    }

    /**
     * Returns a hash code based on the "deep contents" of the specified
     * array.  If the array contains other arrays as elements, the
     * hash code is based on their contents and so on, ad infinitum.
     * It is therefore unacceptable to invoke this method on an array that
     * contains itself as an element, either directly or indirectly through
     * one or more levels of arrays.  The behavior of such an invocation is
     * undefined.
     *
     * <p>For any two arrays <tt>a</tt> and <tt>b</tt> such that
     * <tt>Arrays.deepEquals(a, b)</tt>, it is also the case that
     * <tt>Arrays.deepHashCode(a) == Arrays.deepHashCode(b)</tt>.
     *
     * <p>The computation of the value returned by this method is similar to
     * that of the value returned by {@link List#hashCode()} on a list
     * containing the same elements as <tt>a</tt> in the same order, with one
     * difference: If an element <tt>e</tt> of <tt>a</tt> is itself an array,
     * its hash code is computed not by calling <tt>e.hashCode()</tt>, but as
     * by calling the appropriate overloading of <tt>Arrays.hashCode(e)</tt>
     * if <tt>e</tt> is an array of a primitive type, or as by calling
     * <tt>Arrays.deepHashCode(e)</tt> recursively if <tt>e</tt> is an array
     * of a reference type.  If <tt>a</tt> is <tt>null</tt>, this method
     * returns 0.
     *
     * @param a the array whose deep-content-based hash code to compute
     * @return a deep-content-based hash code for <tt>a</tt>
     * @see #hashCode(Object[])
     * @since 1.5
     */
    public static int deepHashCode(Object a[]) {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a) {
            int elementHash = 0;
            if (element instanceof Object[])
                elementHash = deepHashCode((Object[]) element);
            else if (element instanceof byte[])
                elementHash = hashCode((byte[]) element);
            else if (element instanceof short[])
                elementHash = hashCode((short[]) element);
            else if (element instanceof int[])
                elementHash = hashCode((int[]) element);
            else if (element instanceof long[])
                elementHash = hashCode((long[]) element);
            else if (element instanceof char[])
                elementHash = hashCode((char[]) element);
            else if (element instanceof float[])
                elementHash = hashCode((float[]) element);
            else if (element instanceof double[])
                elementHash = hashCode((double[]) element);
            else if (element instanceof boolean[])
                elementHash = hashCode((boolean[]) element);
            else if (element != null)
                elementHash = element.hashCode();

            result = 31 * result + elementHash;
        }

        return result;
    }

    /**
     * Returns <tt>true</tt> if the two specified arrays are <i>deeply
     * equal</i> to one another.  Unlike the {@link #equals(Object[],Object[])}
     * method, this method is appropriate for use with nested arrays of
     * arbitrary depth.
     *
     * <p>Two array references are considered deeply equal if both
     * are <tt>null</tt>, or if they refer to arrays that contain the same
     * number of elements and all corresponding pairs of elements in the two
     * arrays are deeply equal.
     *
     * <p>Two possibly <tt>null</tt> elements <tt>e1</tt> and <tt>e2</tt> are
     * deeply equal if any of the following conditions hold:
     * <ul>
     *    <li> <tt>e1</tt> and <tt>e2</tt> are both arrays of object reference
     *         types, and <tt>Arrays.deepEquals(e1, e2) would return true</tt>
     *    <li> <tt>e1</tt> and <tt>e2</tt> are arrays of the same primitive
     *         type, and the appropriate overloading of
     *         <tt>Arrays.equals(e1, e2)</tt> would return true.
     *    <li> <tt>e1 == e2</tt>
     *    <li> <tt>e1.equals(e2)</tt> would return true.
     * </ul>
     * Note that this definition permits <tt>null</tt> elements at any depth.
     *
     * <p>If either of the specified arrays contain themselves as elements
     * either directly or indirectly through one or more levels of arrays,
     * the behavior of this method is undefined.
     *
     * @param a1 one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     * @see #equals(Object[],Object[])
     * @see Objects#deepEquals(Object, Object)
     * @since 1.5
     */
    public static boolean deepEquals(Object[] a1, Object[] a2) {
        if (a1 == a2)
            return true;
        if (a1 == null || a2==null)
            return false;
        int length = a1.length;
        if (a2.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            Object e1 = a1[i];
            Object e2 = a2[i];

            if (e1 == e2)
                continue;
            if (e1 == null)
                return false;

            // Figure out whether the two elements are equal
            boolean eq = deepEquals0(e1, e2);

            if (!eq)
                return false;
        }
        return true;
    }

    static boolean deepEquals0(Object e1, Object e2) {
        assert e1 != null;
        boolean eq;
        if (e1 instanceof Object[] && e2 instanceof Object[])
            eq = deepEquals ((Object[]) e1, (Object[]) e2);
        else if (e1 instanceof byte[] && e2 instanceof byte[])
            eq = equals((byte[]) e1, (byte[]) e2);
        else if (e1 instanceof short[] && e2 instanceof short[])
            eq = equals((short[]) e1, (short[]) e2);
        else if (e1 instanceof int[] && e2 instanceof int[])
            eq = equals((int[]) e1, (int[]) e2);
        else if (e1 instanceof long[] && e2 instanceof long[])
            eq = equals((long[]) e1, (long[]) e2);
        else if (e1 instanceof char[] && e2 instanceof char[])
            eq = equals((char[]) e1, (char[]) e2);
        else if (e1 instanceof float[] && e2 instanceof float[])
            eq = equals((float[]) e1, (float[]) e2);
        else if (e1 instanceof double[] && e2 instanceof double[])
            eq = equals((double[]) e1, (double[]) e2);
        else if (e1 instanceof boolean[] && e2 instanceof boolean[])
            eq = equals((boolean[]) e1, (boolean[]) e2);
        else
            eq = e1.equals(e2);
        return eq;
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(long)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(long[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(int)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt> is
     * <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(int[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(short)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(short[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(char)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(char[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements
     * are separated by the characters <tt>", "</tt> (a comma followed
     * by a space).  Elements are converted to strings as by
     * <tt>String.valueOf(byte)</tt>.  Returns <tt>"null"</tt> if
     * <tt>a</tt> is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(byte[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(boolean)</tt>.  Returns <tt>"null"</tt> if
     * <tt>a</tt> is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(boolean[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(float)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(float[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(double)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(double[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * If the array contains other arrays as elements, they are converted to
     * strings by the {@link Object#toString} method inherited from
     * <tt>Object</tt>, which describes their <i>identities</i> rather than
     * their contents.
     *
     * <p>The value returned by this method is equal to the value that would
     * be returned by <tt>Arrays.asList(a).toString()</tt>, unless <tt>a</tt>
     * is <tt>null</tt>, in which case <tt>"null"</tt> is returned.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @see #deepToString(Object[])
     * @since 1.5
     */
    public static String toString(Object[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the "deep contents" of the specified
     * array.  If the array contains other arrays as elements, the string
     * representation contains their contents and so on.  This method is
     * designed for converting multidimensional arrays to strings.
     *
     * <p>The string representation consists of a list of the array's
     * elements, enclosed in square brackets (<tt>"[]"</tt>).  Adjacent
     * elements are separated by the characters <tt>", "</tt> (a comma
     * followed by a space).  Elements are converted to strings as by
     * <tt>String.valueOf(Object)</tt>, unless they are themselves
     * arrays.
     *
     * <p>If an element <tt>e</tt> is an array of a primitive type, it is
     * converted to a string as by invoking the appropriate overloading of
     * <tt>Arrays.toString(e)</tt>.  If an element <tt>e</tt> is an array of a
     * reference type, it is converted to a string as by invoking
     * this method recursively.
     *
     * <p>To avoid infinite recursion, if the specified array contains itself
     * as an element, or contains an indirect reference to itself through one
     * or more levels of arrays, the self-reference is converted to the string
     * <tt>"[...]"</tt>.  For example, an array containing only a reference
     * to itself would be rendered as <tt>"[[...]]"</tt>.
     *
     * <p>This method returns <tt>"null"</tt> if the specified array
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @see #toString(Object[])
     * @since 1.5
     */
    public static String deepToString(Object[] a) {
        if (a == null)
            return "null";

        int bufLen = 20 * a.length;
        if (a.length != 0 && bufLen <= 0)
            bufLen = Integer.MAX_VALUE;
        StringBuilder buf = new StringBuilder(bufLen);
        deepToString(a, buf, new HashSet<Object[]>());
        return buf.toString();
    }

    private static void deepToString(Object[] a, StringBuilder buf,
                                     Set<Object[]> dejaVu) {
        if (a == null) {
            buf.append("null");
            return;
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            buf.append("[]");
            return;
        }

        dejaVu.add(a);
        buf.append('[');
        for (int i = 0; ; i++) {

            Object element = a[i];
            if (element == null) {
                buf.append("null");
            } else {
                Class<?> eClass = element.getClass();

                if (eClass.isArray()) {
                    if (eClass == byte[].class)
                        buf.append(toString((byte[]) element));
                    else if (eClass == short[].class)
                        buf.append(toString((short[]) element));
                    else if (eClass == int[].class)
                        buf.append(toString((int[]) element));
                    else if (eClass == long[].class)
                        buf.append(toString((long[]) element));
                    else if (eClass == char[].class)
                        buf.append(toString((char[]) element));
                    else if (eClass == float[].class)
                        buf.append(toString((float[]) element));
                    else if (eClass == double[].class)
                        buf.append(toString((double[]) element));
                    else if (eClass == boolean[].class)
                        buf.append(toString((boolean[]) element));
                    else { // element is an array of object references
                        if (dejaVu.contains(element))
                            buf.append("[...]");
                        else
                            deepToString((Object[])element, buf, dejaVu);
                    }
                } else {  // element is non-null and not an array
                    buf.append(element.toString());
                }
            }
            if (i == iMax)
                break;
            buf.append(", ");
        }
        buf.append(']');
        dejaVu.remove(a);
    }


    /**
     * Set all elements of the specified array, using the provided
     * generator function to compute each element.
     *
     * <p>If the generator function throws an exception, it is relayed to
     * the caller and the array is left in an indeterminate state.
     *
     * @param <T> type of elements of the array
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static <T> void setAll(T[] array, IntFunction<? extends T> generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.apply(i);
    }

    /**
     * Set all elements of the specified array, in parallel, using the
     * provided generator function to compute each element.
     *
     * <p>If the generator function throws an exception, an unchecked exception
     * is thrown from {@code parallelSetAll} and the array is left in an
     * indeterminate state.
     *
     * @param <T> type of elements of the array
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static <T> void parallelSetAll(T[] array, IntFunction<? extends T> generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.apply(i); });
    }

    /**
     * Set all elements of the specified array, using the provided
     * generator function to compute each element.
     *
     * <p>If the generator function throws an exception, it is relayed to
     * the caller and the array is left in an indeterminate state.
     *
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static void setAll(int[] array, IntUnaryOperator generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.applyAsInt(i);
    }

    /**
     * Set all elements of the specified array, in parallel, using the
     * provided generator function to compute each element.
     *
     * <p>If the generator function throws an exception, an unchecked exception
     * is thrown from {@code parallelSetAll} and the array is left in an
     * indeterminate state.
     *
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     * value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static void parallelSetAll(int[] array, IntUnaryOperator generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.applyAsInt(i); });
    }

    /**
     * Set all elements of the specified array, using the provided
     * generator function to compute each element.
     *
     * <p>If the generator function throws an exception, it is relayed to
     * the caller and the array is left in an indeterminate state.
     *
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static void setAll(long[] array, IntToLongFunction generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.applyAsLong(i);
    }

    /**
     * Set all elements of the specified array, in parallel, using the
     * provided generator function to compute each element.
     *
     * <p>If the generator function throws an exception, an unchecked exception
     * is thrown from {@code parallelSetAll} and the array is left in an
     * indeterminate state.
     *
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static void parallelSetAll(long[] array, IntToLongFunction generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.applyAsLong(i); });
    }

    /**
     * Set all elements of the specified array, using the provided
     * generator function to compute each element.
     *
     * <p>If the generator function throws an exception, it is relayed to
     * the caller and the array is left in an indeterminate state.
     *
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static void setAll(double[] array, IntToDoubleFunction generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.applyAsDouble(i);
    }

    /**
     * Set all elements of the specified array, in parallel, using the
     * provided generator function to compute each element.
     *
     * <p>If the generator function throws an exception, an unchecked exception
     * is thrown from {@code parallelSetAll} and the array is left in an
     * indeterminate state.
     *
     * @param array array to be initialized
     * @param generator a function accepting an index and producing the desired
     *        value for that position
     * @throws NullPointerException if the generator is null
     * @since 1.8
     */
    public static void parallelSetAll(double[] array, IntToDoubleFunction generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.applyAsDouble(i); });
    }

    /**
     * Returns a {@link Spliterator} covering all of the specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param <T> type of elements
     * @param array the array, assumed to be unmodified during use
     * @return a spliterator for the array elements
     * @since 1.8
     */
    public static <T> Spliterator<T> spliterator(T[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator} covering the specified range of the
     * specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param <T> type of elements
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a spliterator for the array elements
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static <T> Spliterator<T> spliterator(T[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator.OfInt} covering all of the specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param array the array, assumed to be unmodified during use
     * @return a spliterator for the array elements
     * @since 1.8
     */
    public static Spliterator.OfInt spliterator(int[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator.OfInt} covering the specified range of the
     * specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a spliterator for the array elements
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static Spliterator.OfInt spliterator(int[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator.OfLong} covering all of the specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param array the array, assumed to be unmodified during use
     * @return the spliterator for the array elements
     * @since 1.8
     */
    public static Spliterator.OfLong spliterator(long[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator.OfLong} covering the specified range of the
     * specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a spliterator for the array elements
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static Spliterator.OfLong spliterator(long[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator.OfDouble} covering all of the specified
     * array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param array the array, assumed to be unmodified during use
     * @return a spliterator for the array elements
     * @since 1.8
     */
    public static Spliterator.OfDouble spliterator(double[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a {@link Spliterator.OfDouble} covering the specified range of
     * the specified array.
     *
     * <p>The spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#IMMUTABLE}.
     *
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a spliterator for the array elements
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static Spliterator.OfDouble spliterator(double[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param <T> The type of the array elements
     * @param array The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     * @since 1.8
     */
    public static <T> Stream<T> stream(T[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * Returns a sequential {@link Stream} with the specified range of the
     * specified array as its source.
     *
     * @param <T> the type of the array elements
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a {@code Stream} for the array range
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static <T> Stream<T> stream(T[] array, int startInclusive, int endExclusive) {
        return StreamSupport.stream(spliterator(array, startInclusive, endExclusive), false);
    }

    /**
     * Returns a sequential {@link IntStream} with the specified array as its
     * source.
     *
     * @param array the array, assumed to be unmodified during use
     * @return an {@code IntStream} for the array
     * @since 1.8
     */
    public static IntStream stream(int[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * Returns a sequential {@link IntStream} with the specified range of the
     * specified array as its source.
     *
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return an {@code IntStream} for the array range
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static IntStream stream(int[] array, int startInclusive, int endExclusive) {
        return StreamSupport.intStream(spliterator(array, startInclusive, endExclusive), false);
    }

    /**
     * Returns a sequential {@link LongStream} with the specified array as its
     * source.
     *
     * @param array the array, assumed to be unmodified during use
     * @return a {@code LongStream} for the array
     * @since 1.8
     */
    public static LongStream stream(long[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * Returns a sequential {@link LongStream} with the specified range of the
     * specified array as its source.
     *
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a {@code LongStream} for the array range
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static LongStream stream(long[] array, int startInclusive, int endExclusive) {
        return StreamSupport.longStream(spliterator(array, startInclusive, endExclusive), false);
    }

    /**
     * Returns a sequential {@link DoubleStream} with the specified array as its
     * source.
     *
     * @param array the array, assumed to be unmodified during use
     * @return a {@code DoubleStream} for the array
     * @since 1.8
     */
    public static DoubleStream stream(double[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * Returns a sequential {@link DoubleStream} with the specified range of the
     * specified array as its source.
     *
     * @param array the array, assumed to be unmodified during use
     * @param startInclusive the first index to cover, inclusive
     * @param endExclusive index immediately past the last index to cover
     * @return a {@code DoubleStream} for the array range
     * @throws ArrayIndexOutOfBoundsException if {@code startInclusive} is
     *         negative, {@code endExclusive} is less than
     *         {@code startInclusive}, or {@code endExclusive} is greater than
     *         the array size
     * @since 1.8
     */
    public static DoubleStream stream(double[] array, int startInclusive, int endExclusive) {
        return StreamSupport.doubleStream(spliterator(array, startInclusive, endExclusive), false);
    }
}
