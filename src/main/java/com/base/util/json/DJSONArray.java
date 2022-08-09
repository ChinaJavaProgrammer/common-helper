package com.base.util.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DJSONArray implements DJSONList {

	List<DJSON> jsonArray = new ArrayList<>();
	
	
	@Override
	public int size() {
		return jsonArray.size();
	}

	@Override
	public boolean isEmpty() {
		return jsonArray.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return jsonArray.contains(o);
	}

	@Override
	public Iterator<DJSON> iterator() {
		return jsonArray.iterator();
	}

	@Override
	public Object[] toArray() {
		return jsonArray.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return jsonArray.toArray(a);
	}

	@Override
	public boolean add(DJSON e) {
		return jsonArray.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return jsonArray.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return jsonArray.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends DJSON> c) {
		return jsonArray.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends DJSON> c) {
		return jsonArray.addAll(index,c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return jsonArray.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return jsonArray.retainAll(c);
	}

	@Override
	public void clear() {
		jsonArray.clear();		
	}

	@Override
	public DJSON get(int index) {
		return jsonArray.get(index);
	}

	@Override
	public DJSON set(int index, DJSON element) {
		return jsonArray.set(index, element);
	}

	@Override
	public void add(int index, DJSON element) {
		jsonArray.add(index, element);		
	}

	@Override
	public DJSON remove(int index) {
		return jsonArray.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return jsonArray.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return jsonArray.lastIndexOf(o);
	}

	@Override
	public ListIterator<DJSON> listIterator() {
		return jsonArray.listIterator();
	}

	@Override
	public ListIterator<DJSON> listIterator(int index) {
		return jsonArray.listIterator(index);
	}

	@Override
	public List<DJSON> subList(int fromIndex, int toIndex) {
		return jsonArray.subList(fromIndex, toIndex);
	}

}
