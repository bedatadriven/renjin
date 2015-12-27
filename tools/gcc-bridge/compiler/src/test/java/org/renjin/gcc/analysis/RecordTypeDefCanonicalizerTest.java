package org.renjin.gcc.analysis;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class RecordTypeDefCanonicalizerTest {
  
  public static final GimpleRealType DOUBLE_TYPE = new GimpleRealType(64);

  
  @Test
  public void simple() {

    // Each compilation unit will have their own copy of the 
    // GimpleRecordTypeDef with its own id
    GimpleRecordTypeDef point1 = new GimpleRecordTypeDef();
    point1.setId("p1");
    point1.getFields().add(new GimpleField("x", DOUBLE_TYPE));
    point1.getFields().add(new GimpleField("y", DOUBLE_TYPE));

    GimpleCompilationUnit unit1 = new GimpleCompilationUnit();
    unit1.getRecordTypes().add(point1);
    
    GimpleRecordTypeDef point2 = new GimpleRecordTypeDef();
    point2.setId("p2");
    point2.getFields().add(new GimpleField("x", DOUBLE_TYPE));
    point2.getFields().add(new GimpleField("y", DOUBLE_TYPE));

    GimpleCompilationUnit unit2 = new GimpleCompilationUnit();
    unit2.getRecordTypes().add(point2);

    Collection<GimpleRecordTypeDef> canonicalDefs = 
        RecordTypeDefCanonicalizer.canonicalize(Arrays.asList(unit1, unit2));

    
    assertThat(canonicalDefs, Matchers.hasSize(1));
    
    assertTrue(unit1.getRecordTypes().get(0) ==
               unit2.getRecordTypes().get(0));
    

  }
  
  @Test
  public void recursive() {

    // Each compilation unit will have their own copy of the 
    // GimpleRecordTypeDef with its own id
    
    GimpleRecordTypeDef node1 = new GimpleRecordTypeDef();
    node1.setId("N1");
    node1.getFields().add(new GimpleField("value", DOUBLE_TYPE));
    node1.getFields().add(new GimpleField("next", new GimpleRecordType("N1")));
    
    GimpleRecordTypeDef linkedList1 = new GimpleRecordTypeDef();
    linkedList1.setId("LL1");
    linkedList1.getFields().add(new GimpleField("head", new GimpleRecordType("N1")));
    
    GimpleCompilationUnit unit1 = new GimpleCompilationUnit();
    unit1.getRecordTypes().add(linkedList1);
    unit1.getRecordTypes().add(node1);

    // Second copy of the linked link and node structs, included in a second
    // compilation unit
    
    GimpleRecordTypeDef node2 = new GimpleRecordTypeDef();
    node2.setId("N2");
    node2.getFields().add(new GimpleField("value", DOUBLE_TYPE));
    node2.getFields().add(new GimpleField("next", new GimpleRecordType("N2")));

    GimpleRecordTypeDef linkedList2 = new GimpleRecordTypeDef();
    linkedList2.setId("LL2");
    linkedList2.getFields().add(new GimpleField("head", new GimpleRecordType("N2")));

    GimpleCompilationUnit unit2 = new GimpleCompilationUnit();
    unit2.getRecordTypes().add(linkedList2);
    unit2.getRecordTypes().add(node2);

    
    
    Collection<GimpleRecordTypeDef> canonicalDefs =
        RecordTypeDefCanonicalizer.canonicalize(Arrays.asList(unit1, unit2));
    
    
    
  }
  
}