package com.dianping.squirrel.common.util;

/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link LinkedList} version with a maximum number of elements. When adding
 * elements to the end of the list, first elements in the list are discarded if
 * the maximum size is reached.
 * 
 * @author Kevin Gaudin
 * 
 * @param <E>
 */
public class BoundedLinkedList<E> extends LinkedList<E> {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MAX_SIZE = 1000;
    
    private final int maxSize;

    public BoundedLinkedList() {
        this(DEFAULT_MAX_SIZE);
    }
    
    public BoundedLinkedList(int maxSize) {
        this.maxSize = maxSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#add(java.lang.Object)
     */
    @Override
    public boolean add(E object) {
        if (size() == maxSize) {
            E element = removeFirst();
            overflow(element);
        }
        return super.add(object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#add(int, java.lang.Object)
     */
    @Override
    public void add(int location, E object) {
        if (size() == maxSize) {
            E element = removeFirst();
            overflow(element);
        }
        super.add(location, object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        final int totalNeededSize = size() + collection.size();
        final int overhead = totalNeededSize - maxSize;
        if (overhead > 0) {
            List<E> elements = subList(0, overhead);
            removeRange(0, overhead);
            overflow(elements);
        }
        return super.addAll(collection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        // int totalNeededSize = size() + collection.size();
        // int overhead = totalNeededSize - maxSize;
        // if(overhead > 0) {
        // removeRange(0, overhead);
        // }
        // return super.addAll(location, collection);
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addFirst(java.lang.Object)
     */
    @Override
    public void addFirst(E object) {
        // super.addFirst(object);
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addLast(java.lang.Object)
     */
    @Override
    public void addLast(E object) {
        add(object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        for (E object : this) {
            result.append(object.toString());
        }
        return result.toString();
    }
    
    protected void overflow(E object) {
    }
    
    protected void overflow(List<E> objects) {
    }
    
}
