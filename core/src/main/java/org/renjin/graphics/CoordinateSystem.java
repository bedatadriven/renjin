package org.renjin.graphics;

public enum CoordinateSystem {

  /**
   * devices natural coordinate system
   * (e.g., pixels, 1/72", ...)
   */
  DEVICE,
  
  /**
   * normalised device coordinates (0..1 on device)
   */
  NDC,
  
  LINES,
  
  /**
   * inches
   */
  INCHES,
  
  /**
   * outer margin coordinates
   */
  OMA,
  
  /**
   * normalised inner region coordinates
   * (0..1 on inner region)
   */
  NIC,
  
  /**
   *  normalised figure coordinates
   *  (0..1 on figure region)
   */
  NFC,
  
  /**
   * figure margin coordinates
   */
  MAR,
  
  /**
   * normalised plot coordinates
   * (0..1 on plot region)
   */
  NPC,
  
  /**
   *  world or data coordinates
   */
  USER
  
}
