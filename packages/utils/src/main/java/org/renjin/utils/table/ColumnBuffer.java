package org.renjin.utils.table;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.StringVector;


public interface ColumnBuffer {
    void add(String value);
    void addNA();

    AtomicVector build();
    StringVector buildStringVector();

}
