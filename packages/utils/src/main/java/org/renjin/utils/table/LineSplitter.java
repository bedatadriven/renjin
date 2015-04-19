package org.renjin.utils.table;

import java.util.List;

/**
* Splits lines into columns
*/
interface LineSplitter {
  
    List<String> split(String line);
}
