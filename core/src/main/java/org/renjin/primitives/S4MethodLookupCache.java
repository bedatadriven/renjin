/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives;

import org.renjin.sexp.Environment;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import java.util.*;

public class S4MethodLookupCache {
  private List<Environment> genericMethodTables = new ArrayList<>();
  private List<Integer> genericSignatureLengths = new ArrayList<>();
  private int maxGenericSignatureLength = 0;

  public void setGenericMethodTables(List<Environment> genericMethodTables) {
    this.genericMethodTables = genericMethodTables;
  }

  public void setGenericSignatureLengths(List<Integer> genericSignatureLengths) {
    this.genericSignatureLengths = genericSignatureLengths;
  }

  public void setGroupMethodTables(List<Environment> groupMethodTables) {
    this.groupMethodTables = groupMethodTables;
  }

  public void setGroupSignatureLengths(List<Integer> groupSignatureLengths) {
    this.groupSignatureLengths = groupSignatureLengths;
  }

  private List<Environment> groupMethodTables = new ArrayList<>();
  private List<Integer> groupSignatureLengths = new ArrayList<>();
  private int maxGroupSignatureLength = 0;

  private SEXP source;
  private PairList args;
  private Environment rho;
  private String group;
  private String opName;

  private List<S4.MethodRanking> genericRankings;
  private List<S4.MethodRanking> groupRankings;

  private Map<String, S4.SelectedMethod> selected = new HashMap<>();

  public void addSelectedMethod(String signature, S4.SelectedMethod method) {
    selected.put(signature, method);
  }
  public boolean inSelectedMethods(String signature) {
    return selected.containsKey(signature);
  }
  public S4.SelectedMethod getSelectedMethod(String signature) {
    return selected.get(signature);
  }

  public List<S4.MethodRanking> getGenericRankings() {
    return genericRankings;
  }

  public void setGenericRankings(List<S4.MethodRanking> genericRankings) {
    this.genericRankings = genericRankings;
  }

  public List<S4.MethodRanking> getGroupRankings() {
    return groupRankings;
  }

  public void setGroupRankings(List<S4.MethodRanking> groupRankings) {
    this.groupRankings = groupRankings;
  }

  public String getGroup() {
    return group;
  }

  public String getOpName() {
    return opName;
  }

  private String inputSignature;
  private PairList expandedArgs;
  private PairList.Builder promisedArgs;


  public void setInputSignature(String inputSignature) {
    this.inputSignature = inputSignature;
  }
  public String getInputSignature() {
    return this.inputSignature;
  }

  public void setInputs(SEXP source, PairList args, Environment rho, String group, String opName) {
    this.source = source;
    this.rho = rho;
    this.group = group;
    this.opName = opName;
  }

  public void setExpandedArgs(PairList expandedArgs) {
    this.expandedArgs = expandedArgs;
  }

  public void setPromisedArgs(PairList.Builder promisedArgs) {
    this.promisedArgs = promisedArgs;
  }

  public void setOpName(String opName) {
    this.opName = opName;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  private int maxSignatureLength = 0;

  public List<Integer> getGenericSignatureLengths() {
    return genericSignatureLengths;
  }

  public List<Integer> getGroupSignatureLengths() {
    return groupSignatureLengths;
  }

  public List<Environment> getGenericMethodTables() {
    return genericMethodTables;
  }

  public List<Environment> getGroupMethodTables() {
    return groupMethodTables;
  }

  public void addGeneric(S4.MethodTable generic) {
    genericMethodTables = generic.methods;
    genericSignatureLengths = generic.sigLengths;
  }

  public void addGroup(S4.MethodTable group) {
    groupMethodTables = group.methods;
    groupSignatureLengths = group.sigLengths;
  }

  public boolean hasGeneric() {
    return genericMethodTables.size() > 0;
  }

  public boolean hasGroup() {
    return groupMethodTables.size() > 0;
  }

  public boolean hasNoMethods() {
    return !hasGroup() && !hasGeneric();
  }

  private void setMaxLength(int value) {
    maxSignatureLength = value;
  }

  private void setMaxGenericLength(int value) {
    maxGenericSignatureLength = value;
  }

  private void setMaxGroupLength(int value) {
    maxGroupSignatureLength = value;
  }

  public int getMaxSignatureLength() {
    return maxSignatureLength;
  }

  public void findMaxSignatureLength() {
    int maxGenericMethodLength = 0;
    int maxGroupMethodLength = 0;

    if(genericMethodTables.size() > 0) {
      int length = Collections.max(genericSignatureLengths);
      maxGenericMethodLength = length > maxGenericMethodLength ? length : maxGenericMethodLength;
      this.setMaxGenericLength(maxGenericMethodLength);
    }

    if(groupMethodTables.size() > 0) {
      int length = Collections.max(groupSignatureLengths);
      maxGroupMethodLength = length > maxGroupMethodLength ? length : maxGroupMethodLength;
      this.setMaxGroupLength(maxGroupMethodLength);
    }

    this.setMaxLength(maxGenericMethodLength > maxGroupMethodLength ? maxGenericMethodLength : maxGroupMethodLength);
  }

}
