/**
 * Code generation strategies for record types that are always allocated in blocks of 1 record.
 * 
 * <p>When a record types are never allocated in blocks, then we can compile them using simple Java object
 * references.</p>
 */
package org.renjin.gcc.codegen.type.record.unit;