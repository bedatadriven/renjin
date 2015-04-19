package org.renjin.utils.table;

import java.util.Arrays;

/**
 * Identifies the type of a string input
 */
public class TypeDetector {
    
    public static final int INTEGER =   0x01;  // 001
    public static final int DOUBLE =    0x03;  // 011
    public static final int LOGICAL =   0x04;  // 100 
    public static final int CHARACTER = 0x07;  // 111
    
    private static final String LOGICAL_CHARACTERS = "truefalse";
    private static final String NUMERIC_CHARACTERS = "0123456789";
    
    private final int[] characters;
    
    public TypeDetector(TableOptions options) {
        
        characters = new int[255];
        
        // By default, consider a character as elevating to String
        Arrays.fill(characters, CHARACTER);
        
        // Certain characters are permissible in logical
        for(int i=0;i!=LOGICAL_CHARACTERS.length();++i) {
            characters[LOGICAL_CHARACTERS.charAt(i)] = LOGICAL;
        }
        
        // Digits are permitted in integers...
        for(int i=0;i!=NUMERIC_CHARACTERS.length();++i) {
            characters[NUMERIC_CHARACTERS.charAt(i)] = INTEGER;
        }
        
        // ... But decimal point signals floating point
        characters[options.dec] = DOUBLE;

    }
    
    public Buffer newBuffer() {
        return new Buffer();
    }
    
    public class Buffer {

        private int type = 0;
        
        public void update(String string) {
            // Null (NA) strings have no impact on type detection process
            if(string == null) {
                return;
            }
            
            // Already flagged as character, no need to look further
            if(type == CHARACTER) {
                return;
            }
            
            // Update the type indicator
            for(int i=0;i<string.length();++i) {
                char c = string.charAt(i);
                if(c > characters.length) {
                    type = CHARACTER;
                    break;
                }
                int mask = characters[c];
                type |= characters[c];
                if(type == CHARACTER) {
                    break;
                }
            }
        }

        public int getType() {
            if(type == 0 || type == LOGICAL) {
                return LOGICAL;
            } else if(type == INTEGER) {
                return INTEGER;
            } else if(type == DOUBLE) {
                return DOUBLE;
            } else {
                return CHARACTER;
            }
        }
    }

}
