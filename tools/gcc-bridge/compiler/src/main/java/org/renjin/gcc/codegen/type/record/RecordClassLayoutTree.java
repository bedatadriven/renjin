package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;


public class RecordClassLayoutTree {

  public class Node {

    /**
     * Offset from the beginning of the record, in bits
     */
    private int offset;

    /**
     * Size of this node, in bits.
     */
    private int size;
    
    private Set<GimpleField> fields = new HashSet<>();
    
    private Set<GimpleType> types = Sets.newHashSet();
    
    private boolean addressable = false;

    public Node(GimpleField field) {
      this.offset = field.getOffset();
      this.size = field.getType().getSize();
      addField(field);
    }
    
    private void addField(GimpleField field) {
      fields.add(field);
      types.add(field.getType());
      if(field.isAddressed()) {
        addressable = true;
      }
    }

    public boolean isAddressable() {
      return addressable;
    }

    public FieldTypeSet typeSet() {
      return new FieldTypeSet(fields);
    }

    public int getOffset() {
      return offset;
    }
    
    public String name() {
      String name = null;
      for (GimpleField field : fields) {
        if(field.getName() != null) {
          if(name == null) {
            name = field.getName();
          } else {
            name = Strings.commonPrefix(name, field.getName());
          }
        }
      }
      return Strings.nullToEmpty(name);
    }
    
  }

  private LinkedList<Node> tree = new LinkedList<>();

  public RecordClassLayoutTree(UnionSet unionSet) {

    // Add the NON-RECORD fields from the union
    for (GimpleRecordTypeDef unionDef : unionSet.getUnions()) {
      for (GimpleField field : unionDef.getFields()) {
        if(!(field.getType() instanceof GimpleRecordType)) {
          addField(field);
        }
      }
    }

    // Now add the fields from the records that are members of the union
    for (GimpleRecordTypeDef recordDef : unionSet.getRecords()) {
      for (GimpleField field : recordDef.getFields()) {
        addField(field);
      }
    }
  }

  private void addField(GimpleField field) {
    ListIterator<Node> it = tree.listIterator();
    while(it.hasNext()) {
      Node node = it.next();
      if(node.offset > field.getOffset()) {
        tree.add(it.previousIndex(), new Node(field));
        return;

      } else if(node.offset == field.getOffset()) {
        if(node.size != field.getType().getSize()) {
          throw new UnsupportedOperationException("TODO: overlapping fields");
        } else {
          node.fields.add(field);
          return;
        }
      }
    }
    // Add to end
    tree.add(new Node(field));
  }

  public LinkedList<Node> getTree() {
    return tree;
  }
}
