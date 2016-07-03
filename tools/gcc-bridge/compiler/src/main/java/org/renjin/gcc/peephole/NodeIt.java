package org.renjin.gcc.peephole;

import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.tree.AbstractInsnNode;
import org.renjin.repackaged.asm.tree.InsnList;
import org.renjin.repackaged.asm.tree.LabelNode;
import org.renjin.repackaged.asm.tree.LineNumberNode;

import java.util.Set;

/**
 * Iterator over instruction nodes. 
 */
public class NodeIt {

  private InsnList list;
  private Set<Label> jumpTargets;
  private AbstractInsnNode current;

  public NodeIt(InsnList list, Set<Label> jumpTargets) {
    this.list = list;
    this.jumpTargets = jumpTargets;
    this.current = list.getFirst();

    if(current == null) {
      throw new IllegalStateException("Empty instruction list");
    }
  }


  public boolean matches(Pattern... patterns) {
    AbstractInsnNode node = current;
    for (Pattern pattern : patterns) {
      if (node == null) {
        return false;
      }
      if (!pattern.match(node)) {
        return false;
      }
      node = nextIgnoringLabels(node);
    }
    return true;
  }

  public InsnList getList() {
    return list;
  }

  /**
   * Gets an instruction node at the given {@code offset}, relative to the current 
   * instruction node.
   */
  public <X extends AbstractInsnNode> X get(int offset) {
    AbstractInsnNode node = current;
    while(offset > 0) {
      node = nextIgnoringLabels(node);
      offset--;
    }
    return (X) node;
  }

  /**
   * Removes the given {@code count} of instructions, starting with the current instruction pointer.
   *
   * <p>After removal, the current instruction is set to the node before the previously current instruction,
   * or the beginning of the list if the iterator was the first element of the list.</p>
   *
   * @param count the number of instructions to delete
   */
  public void remove(int count) {
    if(count < 0) {
      throw new IllegalArgumentException("count: " + count);
    }
    if(count == 0) {
      return;
    }
    AbstractInsnNode deleting = current;
    current = current.getPrevious();

    while(count > 0) {
      AbstractInsnNode next = deleting.getNext();
      if(!ignored(deleting)) {
        list.remove(deleting);
        count--;
      }
      deleting = next;
    }

    if(current == null) {
      current = list.getFirst();
    }
  }

  public void replace(int offset, AbstractInsnNode node) {
    list.set(get(offset), node);
  }

  /**
   * Inserts a new instruction after the current position. 
   *
   * <p>Does not change the current instruction pointer.</p>
   *
   * @param node the node to insert
   */
  public void insert(AbstractInsnNode... nodes) {
    InsnList newList = new InsnList();
    for (AbstractInsnNode node : nodes) {
      newList.add(node);
    }
    list.insert(current, newList);
  }

  public boolean next() {
    current = nextIgnoringLabels(current);

    return (current != null);
  }


  private AbstractInsnNode nextIgnoringLabels(AbstractInsnNode node) {
    AbstractInsnNode next = node.getNext();
    if(next == null) {
      return null;
    }
    if (ignored(next)) {
      return nextIgnoringLabels(next);
    } else {
      return next;
    }
  }


  private boolean ignored(AbstractInsnNode node) {
    if(node instanceof LabelNode) {
      // Generally ignore labels, which are used mostly for line numbering, unless
      // this is a target of a jump instruction, which we can't delete
      LabelNode labelNode = (LabelNode) node;
      return !jumpTargets.contains(labelNode.getLabel());
    }
    if(node instanceof LineNumberNode) {
      return true;
    }

    return false;
  }
}
