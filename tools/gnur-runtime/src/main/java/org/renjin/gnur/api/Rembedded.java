package org.renjin.gnur.api;

import org.renjin.gcc.annotations.GlobalVar;
import org.renjin.gcc.runtime.Ptr;

public class Rembedded {
  private Rembedded() {

  }

  public static int Rf_initEmbeddedR(int argc, Ptr argv) {
     throw new UnimplementedGnuApiMethod("Rf_initEmbeddedR");

  }

  public static void Rf_endEmbeddedR(int fatal) {
    throw new UnimplementedGnuApiMethod("Rf_initEmbeddedR");
  }

  public static int Rf_initialize_R(int ac, Ptr av) {
    throw new UnimplementedGnuApiMethod("Rf_initialize_R");
  }

  public static void setup_Rmainloop() {
    throw new UnimplementedGnuApiMethod("setup_Rmainloop");
  }

  public static void R_ReplDLLinit() {
    throw new UnimplementedGnuApiMethod("R_ReplDLLinit");
  }

  public static int R_ReplDLLdo1() {
    throw new UnimplementedGnuApiMethod("R_ReplDLLdo1");
  }

  public static void R_setStartTime() {
    throw new UnimplementedGnuApiMethod("R_setStartTime");
  }

  public static void R_RunExitFinalizers() {
    throw new UnimplementedGnuApiMethod("R_RunExitFinalizers");
  }

  public static void CleanEd() {
    throw new UnimplementedGnuApiMethod("CleanEd");
  }

  public static void Rf_KillAllDevices() {
    throw new UnimplementedGnuApiMethod("Rf_KillAllDevices");
  }

  @GlobalVar
  public static int R_DirtyImage() {
    throw new UnimplementedGnuApiMethod("R_DirtyImage");
  }

  public static void R_CleanTempDir() {
    throw new UnimplementedGnuApiMethod("R_CleanTempDir");
  }

  @GlobalVar
  public static Ptr R_TempDir() {
    throw new UnimplementedGnuApiMethod("R_TempDir");
  }

  public static void R_SaveGlobalEnv() {
    throw new UnimplementedGnuApiMethod("R_SaveGlobalEnv");
  }

}
