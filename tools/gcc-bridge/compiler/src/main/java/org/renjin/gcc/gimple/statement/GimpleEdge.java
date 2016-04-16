package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleLabel;

public class GimpleEdge {

  public static final int EDGE_FALLTHRU		= 1;	/* 'Straight line' flow */
  public static final int EDGE_ABNORMAL		= 2;	/* Strange flow, like computed label, or eh */
  public static final int EDGE_ABNORMAL_CALL	= 4;	/* Call with abnormal exit like an exception, or sibcall */
  public static final int EDGE_EH				= 8;	/* Exception throw */
  public static final int EDGE_FAKE			= 16;	/* Not a real edge (profile.c) */
  public static final int EDGE_DFS_BACK		= 32;	/* A backwards edge */
  public static final int EDGE_CAN_FALLTHRU	= 64;	/* Candidate for straight line flow. */
  public static final int EDGE_IRREDUCIBLE_LOOP= 128;	/* Part of irreducible loop.  */
  public static final int EDGE_SIBCALL		= 256;	/* Edge from sibcall to exit.  */
  public static final int EDGE_LOOP_EXIT		= 512;	/* Exit of a loop.  */
  public static final int EDGE_TRUE_VALUE		= 1024;	/* Edge taken when controlling predicate is nonzero.  */
  public static final int EDGE_FALSE_VALUE	= 2048;	/* Edge taken when controlling predicate is zero.  */
  public static final int EDGE_EXECUTABLE		= 4096;	/* Edge is executable.  Only valid during SSA-CCP.  */
  public static final int EDGE_CROSSING		= 8192;    /* Edge crosses between hot and cold sections, when we do partitioning.  */
  public static final int EDGE_ALL_FLAGS		= 16383;
  public static final int EDGE_COMPLEX = (EDGE_ABNORMAL | EDGE_ABNORMAL_CALL | EDGE_EH);

  private int flags;
  private int source;
  private int target;

  public int getFlags() {
    return flags;
  }
  public void setFlags(int flags) {
    this.flags = flags;
  }
  public int getSource() {
    return source;
  }
  public void setSource(int source) {
    this.source = source;
  }
  public int getTarget() {
    return target;
  }
  public void setTarget(int target) {
    this.target = target;
  }

  public GimpleLabel getSourceLabel() {
    return new GimpleLabel("bb" + source);
  }

  public GimpleLabel getTargetLabel() {
    return new GimpleLabel("bb" + target);
  }

  @Override
  public String toString() {
    return "edge <" + getEdgeTypeName() + ">(" + getSourceLabel() + "," + getTargetLabel() + ")";
  }

  private String getEdgeTypeName() {
    final StringBuilder sb = new StringBuilder();
    final String[] names = {
        "EDGE_FALLTHRU"
        ,"EDGE_ABNORMAL"
        ,"EDGE_ABNORMAL_CALL"
        ,"EDGE_EH,EDGE_FAKE"
        ,"EDGE_DFS_BACK"
        ,"EDGE_CAN_FALLTHRU"
        ,"EDGE_IRREDUCIBLE_LOOP"
        ,"EDGE_SIBCALL"
        ,"EDGE_LOOP_EXIT"
        ,"EDGE_TRUE_VALUE"
        ,"EDGE_FALSE_VALUE"
        ,"EDGE_EXECUTABLE"
        ,"EDGE_CROSSING"
    };
    boolean needsComma = false;
    for(int i = 0; i < 15; ++i) {
      if((flags & (1 << i)) != 0) {
        if(needsComma)
          sb.append(",");
        else
          needsComma = true;
        sb.append(names[i]);
      }
    }
    return sb.toString();
  }
}