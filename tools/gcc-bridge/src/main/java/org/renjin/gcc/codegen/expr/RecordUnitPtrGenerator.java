package org.renjin.gcc.codegen.expr;

/**
 * Expression generator for a record pointer that is known to point to a single record.
 * 
 * <p>For unit pointers, we don't need to wrap the record in an array, we can simply use a 
 * a normal Java reference to the class </p>
 */
public interface RecordUnitPtrGenerator extends ExprGenerator  {
  
  
}
