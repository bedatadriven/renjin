package org.renjin.utils.table;


import com.google.common.collect.Lists;
import org.renjin.sexp.*;

import java.util.*;

public class TypeDetectingColumnBuffer implements ColumnBuffer {

    private final TableOptions options;
    private final TypeDetector.Buffer type;
    
    private StringBlock.Builder builder;
    
    private int offset = 0;

    public TypeDetectingColumnBuffer(TableOptions options, TypeDetector typeDetector) {
        this.options = options;
        builder = new StringBlock.Builder();
        type = typeDetector.newBuffer();
    }
    
    @Override
    public void add(String value) {
        builder.add(value);
        type.update(value);
    }

    @Override
    public void addNA() {
        builder.addNA();
    }

    @Override
    public AtomicVector build() {
        switch (type.getType()) {
            case TypeDetector.CHARACTER:
                if(options.stringsAsFactors) {
                    return buildFactor(builder.build());                    
                } else {
                    return new BlockStringVector(builder.build(), AttributeMap.EMPTY);
                }
            case TypeDetector.INTEGER:
                return new BlockIntVector(builder.build(), AttributeMap.EMPTY);
            case TypeDetector.DOUBLE:
                return new BlockDoubleVector(builder.build(), AttributeMap.EMPTY);
            
            default:
            case TypeDetector.LOGICAL:
                throw new UnsupportedOperationException("logical");
        }
    }

    private IntVector buildFactor(StringBlock block) {

        Set<String> labelSet = new HashSet<String>();
        for(int i=0;i<block.getCount();++i) {
            if(!block.isNA(i)) {
                labelSet.add(block.getStringAt(i));
            }
        }
        List<String> levels = Lists.newArrayList(labelSet);
        Collections.sort(levels);
        
        Map<String, Integer> labelMap = new HashMap<String, Integer>();
        for(int i=0;i<levels.size();++i) {
            labelMap.put(levels.get(i), i+1);
        }

        IntArrayVector.Builder factor = IntArrayVector.Builder.withInitialCapacity(block.getCount());
        for(int i=0;i<block.getCount();++i) {
            if(block.isNA(i)) {
                factor.addNA();
            } else {
                factor.add(labelMap.get(block.getStringAt(i)));
            }
        }
        factor.setAttribute(Symbols.CLASS, StringArrayVector.valueOf("factor"));
        factor.setAttribute(Symbols.LEVELS, new StringArrayVector(levels));
        
        return factor.build();
    }

    @Override
    public StringVector buildStringVector() {
        return new BlockStringVector(builder.build(), AttributeMap.EMPTY);
    }
}
