// Initial template generated from GraphicsDevice.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.Ptr;

@SuppressWarnings("unused")
public final class GraphicsDevice {

  private GraphicsDevice() { }



  // int Rf_ndevNumber (pDevDesc)

  public static int Rf_NumDevices() {
     throw new UnimplementedGnuApiMethod("Rf_NumDevices");
  }

  public static void R_CheckDeviceAvailable() {
     throw new UnimplementedGnuApiMethod("R_CheckDeviceAvailable");
  }

  public static boolean R_CheckDeviceAvailableBool() {
     throw new UnimplementedGnuApiMethod("R_CheckDeviceAvailableBool");
  }

  public static int Rf_curDevice() {
     throw new UnimplementedGnuApiMethod("Rf_curDevice");
  }

  public static int Rf_nextDevice(int p0) {
     throw new UnimplementedGnuApiMethod("Rf_nextDevice");
  }

  public static int Rf_prevDevice(int p0) {
     throw new UnimplementedGnuApiMethod("Rf_prevDevice");
  }

  public static int Rf_selectDevice(int p0) {
     throw new UnimplementedGnuApiMethod("Rf_selectDevice");
  }

  public static void Rf_killDevice(int p0) {
     throw new UnimplementedGnuApiMethod("Rf_killDevice");
  }

  public static int Rf_NoDevices() {
     throw new UnimplementedGnuApiMethod("Rf_NoDevices");
  }

  // void Rf_NewFrameConfirm (pDevDesc)

  // void Rf_doMouseEvent (pDevDesc dd, R_MouseEvent event, int buttons, double x, double y)

  // void Rf_doKeybd (pDevDesc dd, R_KeyName rkey, const char *keyname)

  public static void Rf_onintr() {
     throw new UnimplementedGnuApiMethod("Rf_onintr");
  }

  public static Ptr Rf_AdobeSymbol2utf8(CharPtr out, CharPtr in, /*size_t*/ int nwork) {
     throw new UnimplementedGnuApiMethod("Rf_AdobeSymbol2utf8");
  }

  // size_t Rf_ucstoutf8 (char *s, const unsigned int c)
}
