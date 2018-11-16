package clear_analyser.affix;

import java.util.*;

/**
 * An affix can be used to implement a prefix or a suffix in the form of a list.
 */
public class Affix<A> implements Iterable<A> {
    private List<A> affix;

    public Affix() {
        affix = new ArrayList<>();
    }

    public Affix(List<A> actions) {
        affix = new ArrayList<>(actions);
    }

    public Affix(Affix<A> other) {
        copy(other);
    }

    public void set(List<A> actions) {
        affix = actions;
    }

    @Override
    public Iterator<A> iterator() {
        return new Iterator<A>() {
            private int currIndex = 0;

            @Override
            public boolean hasNext() {
                return currIndex < affix.size() && affix.get(currIndex)!=null;
            }

            @Override
            public A next() {
                return affix.get(currIndex++);
            }
        };
    }

    @Deprecated
    public boolean intersect(List<A> actions) {
        if (affix==null) {
            affix = new ArrayList<>();
            return true;
        }
        else
            return affix.retainAll(actions);
    }

    @Deprecated
    public boolean intersect(Affix<A> otherAffix) {
        if (affix==null) {
            affix = new ArrayList<>();
            return true;
        }
        else
            return affix.retainAll(otherAffix.toList());
    }

    @Deprecated
    public boolean union(List<A> actions) {
        if (affix==null)
            affix = new ArrayList<>();
        return affix.addAll(actions);
    }

    @Deprecated
    public boolean union(Affix<A> otherAffix) {
        if (affix==null)
            affix = new ArrayList<>();
        return affix.addAll(otherAffix.toList());
    }

    /**
     * Use method 'add' instead
     * @param action an action A
     * @return true if add method on the List returns true
     */
    @Deprecated
    public boolean union(A action) {
        if (affix==null)
            affix = new ArrayList<>();
        return affix.add(action);
    }

    /**
     * Retrieve the first element
     * @return the first element of affix
     */
    public A getFirstElement() {
        Iterator iter = affix.iterator();
        if (iter.hasNext()) {
            return (A) iter.next();
        } else {
            return null;
        }
    }



    /**
     * Remove all elements of this that also exist on otherAffix.
     * @param otherAffix another Affix
     */
    public void removeAll(Affix<A> otherAffix) {
        List<A> tmp = this.toList();
        tmp.removeAll(otherAffix.toList());
        affix = new ArrayList<>(tmp);
    }

    /**
     * Remove all elements of this that also exist on the given collection.
     * @param coll a collection of elements
     */
    public void removeAll(Collection<A> coll) {
        List<A> tmp = this.toList();
        tmp.removeAll(coll);
        affix = new ArrayList<>(tmp);
    }

    /**
     * Retain only elements of this that also exist on the given collection.
     * @param coll a collection of elements
     */
    public void retainAll(Collection<A> coll) {
        List<A> tmp = this.toList();
        tmp.retainAll(coll);
        affix = new ArrayList<>(tmp);
    }

    /**
     * Assumption: we assume that if this Affix is shorter than the one passed as parameter,
     * this Affix represent a prefix (suffix, depending on the use) of the second one.
     * @param otherAffix the other Affix to which this is compared
     */
    public void retainShortest(Affix<A> otherAffix) {
        if (otherAffix==null) {
            affix = new ArrayList<>();
            return;
        }
        if (affix.size() > otherAffix.toList().size())
            affix = new ArrayList<>(otherAffix.toList());
    }

    /**
     * Assumption: we assume that if this Affix is longer than the one passed as parameter,
     * the second one represent a prefix (suffix, depending on the use) of this.
     * @param otherAffix the other Affix to which this is compared
     */
    public void retainLongest(Affix<A> otherAffix) {
        if (otherAffix!=null) {
            if (affix.size() < otherAffix.toList().size())
                affix = new ArrayList<>(otherAffix.toList());
        }
    }

    public List<A> toList() {
        if (affix==null)
            affix = new ArrayList<>();
        return affix;
    }

    public boolean add(A action) {
        if (affix==null)
            affix = new ArrayList<>();
        return affix.add(action);
    }

    public void addOnTop(A action) {
        if (affix==null)
            affix = new ArrayList<>();
        affix.add(0, action);
    }

    public boolean isEmpty() {
        return affix.isEmpty();
    }

    public boolean copy(Affix<A> otherAffix) {
        if (otherAffix!=null) {
            affix = new ArrayList<>(otherAffix.toList());
            return true;
        }
        if (affix==null) {
            affix = new ArrayList<>();
            return true;
        }
        return false;
    }

    public int size() {
        // TODO: not sure this is the right solution
        if (affix==null)
            return 0;
        return affix.size();
    }

    /**
     * Copy the passed Affix removing the first element
     * @param other an Affix
     * @return a new Affix copied from the parameter without the first element
     */
    public Affix<A> removeFirst(Affix<A> other) {
        if (other.size()>=2) {
            List<A> tmp = other.toList().subList(1, other.size());
            return new Affix<>(tmp);
        } else return new Affix<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Affix<?> affix1 = (Affix<?>) o;

        return affix != null ? affix.equals(affix1.affix) : affix1.affix == null;
    }

    @Override
    public int hashCode() {
        return affix != null ? affix.hashCode() : 0;
    }

    @Override
    public String toString() {
        return affix.toString();
    }
}
