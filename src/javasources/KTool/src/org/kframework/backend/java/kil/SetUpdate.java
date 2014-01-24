package org.kframework.backend.java.kil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.kframework.backend.java.symbolic.Transformer;
import org.kframework.backend.java.symbolic.Unifier;
import org.kframework.backend.java.symbolic.Visitor;
import org.kframework.backend.java.util.Utils;
import org.kframework.kil.ASTNode;

import com.google.common.collect.ImmutableSet;

/**
 *
 * @author TraianSF
 */
public class SetUpdate extends Term {

    /** {@link org.kframework.backend.java.kil.Term} representation of the map */
    private final Term set;
    /** {@code Set} of keys to be removed from the map */
    private final ImmutableSet<Term> removeSet;

    public SetUpdate(Term set, Set<Term> removeSet) {
        super(Kind.KITEM);
        this.set = set;
        this.removeSet = ImmutableSet.copyOf(removeSet);
    }

    public Term evaluateUpdate() {
        if (removeSet.isEmpty()) {
            return set;
        }

        if (!(set instanceof BuiltinSet)) {
            return this;
        }

        BuiltinSet builtinSet = ((BuiltinSet) set);

        Set<Term> entries = new HashSet<Term>(builtinSet.elements());
        for (Iterator<Term> iterator = removeSet.iterator(); iterator.hasNext();) {
            if (entries.remove(iterator.next())) {
                iterator.remove();
            }
        }

        if (!removeSet.isEmpty()) {
            return this;
        }

        if (builtinSet.hasFrame()) {
            return new BuiltinSet(entries, builtinSet.frame());
        } else {
            return new BuiltinSet(entries);
        }
    }

    public Term base() {
        return set;
    }

    public ImmutableSet<Term> removeSet() {
        return removeSet;
    }

    @Override
    public boolean isSymbolic() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * Utils.HASH_PRIME + set.hashCode();
        hash = hash * Utils.HASH_PRIME + removeSet.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SetUpdate)) {
            return false;
        }

        SetUpdate mapUpdate = (SetUpdate) object;
        return set.equals(mapUpdate.set) && removeSet.equals(mapUpdate.removeSet);
    }

    @Override
    public String toString() {
        String s = set.toString();
        for (Term key : removeSet) {
            s += "[" + key + " <- undef]";
        }
        return s;
    }

    @Override
    public void accept(Unifier unifier, Term pattern) {
        unifier.unify(this, pattern);
    }

    @Override
    public ASTNode accept(Transformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
