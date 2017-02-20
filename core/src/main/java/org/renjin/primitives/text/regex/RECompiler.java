/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.renjin.primitives.text.regex;

import java.util.Hashtable;

/**
 * A regular expression compiler class.  This class compiles a pattern string into a
 * regular expression program interpretable by the RE evaluator class.  The 'recompile'
 * command line tool uses this compiler to pre-compile regular expressions for use
 * with RE.  For a description of the syntax accepted by RECompiler and what you can
 * do with regular expressions, see the documentation for the RE matcher class.
 *
 * @see ExtendedPattern
 *
 * @author <a href="mailto:jonl@muppetlabs.com">Jonathan Locke</a>
 * @author <a href="mailto:gholam@xtra.co.nz">Michael McCallum</a>
 */
public class RECompiler
{
    // The compiled program
    char[] instruction;                                 // The compiled RE 'program' instruction buffer
    int lenInstruction;                                 // The amount of the program buffer currently in use

    // Input state for compiling regular expression
    String pattern;                                     // Input string
    int len;                                            // Length of the pattern string
    int idx;                                            // Current input index into ac
    int parens;                                         // Total number of paren pairs

    // Node flags
    static final int NODE_NORMAL   = 0;                 // No flags (nothing special)
    static final int NODE_NULLABLE = 1;                 // True if node is potentially null
    static final int NODE_TOPLEVEL = 2;                 // True if top level expr

    // Special types of 'escapes'
    static final int ESC_MASK      = 0xffff0;           // Escape complexity mask
    static final int ESC_BACKREF   = 0xfffff;           // Escape is really a backreference
    static final int ESC_COMPLEX   = 0xffffe;           // Escape isn't really a true character
    static final int ESC_CLASS     = 0xffffd;           // Escape represents a whole class of characters

    // {m,n} stacks
    static final int bracketUnbounded = -1;             // Unbounded value
    int bracketMin;                                     // Minimum number of matches
    int bracketOpt;                                     // Additional optional matches

    // Lookup table for POSIX character class names
    static final Hashtable hashPOSIX = new Hashtable();
    static
    {
        hashPOSIX.put("alnum",     new Character(ExtendedPattern.POSIX_CLASS_ALNUM));
        hashPOSIX.put("alpha",     new Character(ExtendedPattern.POSIX_CLASS_ALPHA));
        hashPOSIX.put("blank",     new Character(ExtendedPattern.POSIX_CLASS_BLANK));
        hashPOSIX.put("cntrl",     new Character(ExtendedPattern.POSIX_CLASS_CNTRL));
        hashPOSIX.put("digit",     new Character(ExtendedPattern.POSIX_CLASS_DIGIT));
        hashPOSIX.put("graph",     new Character(ExtendedPattern.POSIX_CLASS_GRAPH));
        hashPOSIX.put("lower",     new Character(ExtendedPattern.POSIX_CLASS_LOWER));
        hashPOSIX.put("print",     new Character(ExtendedPattern.POSIX_CLASS_PRINT));
        hashPOSIX.put("punct",     new Character(ExtendedPattern.POSIX_CLASS_PUNCT));
        hashPOSIX.put("space",     new Character(ExtendedPattern.POSIX_CLASS_SPACE));
        hashPOSIX.put("upper",     new Character(ExtendedPattern.POSIX_CLASS_UPPER));
        hashPOSIX.put("xdigit",    new Character(ExtendedPattern.POSIX_CLASS_XDIGIT));
        hashPOSIX.put("javastart", new Character(ExtendedPattern.POSIX_CLASS_JSTART));
        hashPOSIX.put("javapart",  new Character(ExtendedPattern.POSIX_CLASS_JPART));
    }

    /**
     * Constructor.  Creates (initially empty) storage for a regular expression program.
     */
    public RECompiler()
    {
        // Start off with a generous, yet reasonable, initial size
        instruction = new char[128];
        lenInstruction = 0;
    }

    /**
     * Ensures that n more characters can fit in the program buffer.
     * If n more can't fit, then the size is doubled until it can.
     * @param n Number of additional characters to ensure will fit.
     */
    void ensure(int n)
    {
        // Get current program length
        int curlen = instruction.length;

        // If the current length + n more is too much
        if (lenInstruction + n >= curlen)
        {
            // Double the size of the program array until n more will fit
            while (lenInstruction + n >= curlen)
            {
                curlen *= 2;
            }

            // Allocate new program array and move data into it
            char[] newInstruction = new char[curlen];
            System.arraycopy(instruction, 0, newInstruction, 0, lenInstruction);
            instruction = newInstruction;
        }
    }

    /**
     * Emit a single character into the program stream.
     * @param c Character to add
     */
    void emit(char c)
    {
        // Make room for character
        ensure(1);

        // Add character
        instruction[lenInstruction++] = c;
    }

    /**
     * Inserts a node with a given opcode and opdata at insertAt.  The node relative next
     * pointer is initialized to 0.
     * @param opcode Opcode for new node
     * @param opdata Opdata for new node (only the low 16 bits are currently used)
     * @param insertAt Index at which to insert the new node in the program
     */
    void nodeInsert(char opcode, int opdata, int insertAt)
    {
        // Make room for a new node
        ensure(ExtendedPattern.nodeSize);

        // Move everything from insertAt to the end down nodeSize elements
        System.arraycopy(instruction, insertAt, instruction, insertAt + ExtendedPattern.nodeSize, lenInstruction - insertAt);
        instruction[insertAt /* + RE.offsetOpcode */] = opcode;
        instruction[insertAt    + ExtendedPattern.offsetOpdata   ] = (char) opdata;
        instruction[insertAt    + ExtendedPattern.offsetNext     ] = 0;
        lenInstruction += ExtendedPattern.nodeSize;
    }

    /**
     * Appends a node to the end of a node chain
     * @param node Start of node chain to traverse
     * @param pointTo Node to have the tail of the chain point to
     */
    void setNextOfEnd(int node, int pointTo) throws RESyntaxException {
        // Traverse the chain until the next offset is 0
        int next = instruction[node + ExtendedPattern.offsetNext];
        // while the 'node' is not the last in the chain
        // and the 'node' is not the last in the program.
        while ( next != 0 && node < lenInstruction )
        {
            // if the node we are supposed to point to is in the chain then
            // point to the end of the program instead.
            // Michael McCallum <gholam@xtra.co.nz>
            // FIXME: This is a _hack_ to stop infinite programs.
            // I believe that the implementation of the reluctant matches is wrong but
            // have not worked out a better way yet.
            if (node == pointTo) {
                pointTo = lenInstruction;
            }
            node += next;
            next = instruction[node + ExtendedPattern.offsetNext];
        }

        // if we have reached the end of the program then dont set the pointTo.
        // im not sure if this will break any thing but passes all the tests.
        if ( node < lenInstruction ) {
            // Some patterns result in very large programs which exceed
            // capacity of the short used for specifying signed offset of the
            // next instruction. Example: a{1638}
            int offset = pointTo - node;
            if (offset != (short) offset) {
                throw new RESyntaxException("Exceeded short jump range.");
            }

            // Point the last node in the chain to pointTo.
            instruction[node + ExtendedPattern.offsetNext] = (char) (short) offset;
        }
    }

    /**
     * Adds a new node
     * @param opcode Opcode for node
     * @param opdata Opdata for node (only the low 16 bits are currently used)
     * @return Index of new node in program
     */
    int node(char opcode, int opdata)
    {
        // Make room for a new node
        ensure(ExtendedPattern.nodeSize);

        // Add new node at end
        instruction[lenInstruction /* + RE.offsetOpcode */] = opcode;
        instruction[lenInstruction    + ExtendedPattern.offsetOpdata   ] = (char) opdata;
        instruction[lenInstruction    + ExtendedPattern.offsetNext     ] = 0;
        lenInstruction += ExtendedPattern.nodeSize;

        // Return index of new node
        return lenInstruction - ExtendedPattern.nodeSize;
    }


    /**
     * Throws a new internal error exception
     * @exception Error Thrown in the event of an internal error.
     */
    void internalError() throws Error
    {
        throw new Error("Internal error!");
    }

    /**
     * Throws a new syntax error exception
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    void syntaxError(String s) throws RESyntaxException
    {
        throw new RESyntaxException(s + " at char " + (idx+1));
    }

    /**
     * Match bracket {m,n} expression put results in bracket member variables
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    void bracket() throws RESyntaxException
    {
        // Current character must be a '{'
        if (idx >= len || pattern.charAt(idx++) != '{')
        {
            internalError();
        }

        // Next char must be a digit
        if (idx >= len || !Character.isDigit(pattern.charAt(idx)))
        {
            syntaxError("Expected digit");
        }

        // Get min ('m' of {m,n}) number
        StringBuffer number = new StringBuffer();
        while (idx < len && Character.isDigit(pattern.charAt(idx)))
        {
            number.append(pattern.charAt(idx++));
        }
        try
        {
            bracketMin = Integer.parseInt(number.toString());
        }
        catch (NumberFormatException e)
        {
            syntaxError("Expected valid number");
        }

        // If out of input, fail
        if (idx >= len)
        {
            syntaxError("Expected comma or right bracket");
        }

        // If end of expr, optional limit is 0
        if (pattern.charAt(idx) == '}')
        {
            idx++;
            bracketOpt = 0;
            return;
        }

        // Must have at least {m,} and maybe {m,n}.
        if (idx >= len || pattern.charAt(idx++) != ',')
        {
            syntaxError("Expected comma");
        }

        // If out of input, fail
        if (idx >= len)
        {
            syntaxError("Expected comma or right bracket");
        }

        // If {m,} max is unlimited
        if (pattern.charAt(idx) == '}')
        {
            idx++;
            bracketOpt = bracketUnbounded;
            return;
        }

        // Next char must be a digit
        if (idx >= len || !Character.isDigit(pattern.charAt(idx)))
        {
            syntaxError("Expected digit");
        }

        // Get max number
        number.setLength(0);
        while (idx < len && Character.isDigit(pattern.charAt(idx)))
        {
            number.append(pattern.charAt(idx++));
        }
        try
        {
            bracketOpt = Integer.parseInt(number.toString()) - bracketMin;
        }
        catch (NumberFormatException e)
        {
            syntaxError("Expected valid number");
        }

        // Optional repetitions must be >= 0
        if (bracketOpt < 0)
        {
            syntaxError("Bad range");
        }

        // Must have close brace
        if (idx >= len || pattern.charAt(idx++) != '}')
        {
            syntaxError("Missing close brace");
        }
    }

    /**
     * Match an escape sequence.  Handles quoted chars and octal escapes as well
     * as normal escape characters.  Always advances the input stream by the
     * right amount. This code "understands" the subtle difference between an
     * octal escape and a backref.  You can access the type of ESC_CLASS or
     * ESC_COMPLEX or ESC_BACKREF by looking at pattern[idx - 1].
     * @return ESC_* code or character if simple escape
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int escape() throws RESyntaxException
    {
        // "Shouldn't" happen
        if (pattern.charAt(idx) != '\\')
        {
            internalError();
        }

        // Escape shouldn't occur as last character in string!
        if (idx + 1 == len)
        {
            syntaxError("Escape terminates string");
        }

        // Switch on character after backslash
        idx += 2;
        char escapeChar = pattern.charAt(idx - 1);
        switch (escapeChar)
        {
            case ExtendedPattern.E_BOUND:
            case ExtendedPattern.E_NBOUND:
                return ESC_COMPLEX;

            case ExtendedPattern.E_ALNUM:
            case ExtendedPattern.E_NALNUM:
            case ExtendedPattern.E_SPACE:
            case ExtendedPattern.E_NSPACE:
            case ExtendedPattern.E_DIGIT:
            case ExtendedPattern.E_NDIGIT:
                return ESC_CLASS;

            case 'x':
                {
                    // Max digits
                    int hexDigits = 2;
                    boolean bracketed = false;

                    if(idx + 1 < len && pattern.charAt(idx) == '{') {
                        bracketed = true;
                        hexDigits = Integer.MAX_VALUE;
                        idx++;
                    }

                    // Parse up to hexDigits characters from input
                    int val = 0;
                    for ( ; idx < len && hexDigits-- > 0; idx++)
                    {
                        // Get char
                        char c = pattern.charAt(idx);

                        if(bracketed && c == '}') {
                            break;
                        }

                        // If it's a hexadecimal digit (0-9)
                        if (c >= '0' && c <= '9')
                        {
                            // Compute new value
                            val = (val << 4) + c - '0';
                        }
                        else
                        {
                            // If it's a hexadecimal letter (a-f)
                            c = Character.toLowerCase(c);
                            if (c >= 'a' && c <= 'f')
                            {
                                // Compute new value
                                val = (val << 4) + (c - 'a') + 10;
                            }
                            else
                            {
                                // If it's not a valid digit or hex letter, the escape must be invalid
                                // because hexDigits of input have not been absorbed yet.
                                syntaxError("Expected " + hexDigits + " hexadecimal digits after \\" + escapeChar);
                            }
                        }
                    }
                    if ( bracketed ) {
                        if( ! (idx < len) || pattern.charAt(idx) != '}') {
                            syntaxError("Expected closing '}'");
                        }
                        idx++;
                    }
                    return val;
                }

            case 't':
                return '\t';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 'f':
                return '\f';

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':

                // An octal escape starts with a 0 or has two digits in a row
                if ((idx < len && Character.isDigit(pattern.charAt(idx))) || escapeChar == '0')
                {
                    // Handle \nnn octal escapes
                    int val = escapeChar - '0';
                    if (idx < len && Character.isDigit(pattern.charAt(idx)))
                    {
                        val = ((val << 3) + (pattern.charAt(idx++) - '0'));
                        if (idx < len && Character.isDigit(pattern.charAt(idx)))
                        {
                            val = ((val << 3) + (pattern.charAt(idx++) - '0'));
                        }
                    }
                    return val;
                }

                // It's actually a backreference (\[1-9]), not an escape
                return ESC_BACKREF;

            default:

                // Simple quoting of a character
                return escapeChar;
        }
    }

    /**
     * Compile a character class
     * @return Index of class node
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int characterClass() throws RESyntaxException
    {
        // Check for bad calling or empty class
        if (pattern.charAt(idx) != '[')
        {
            internalError();
        }

        // Check for unterminated or empty class
        if ((idx + 1) >= len || pattern.charAt(++idx) == ']')
        {
            syntaxError("Empty or unterminated class");
        }

        // Try to build a class.  Create OP_ANYOF node
        int ret = node(ExtendedPattern.OP_ANYOF, 0);

        // Parse class declaration
        char CHAR_INVALID = Character.MAX_VALUE;
        char last = CHAR_INVALID;
        char simpleChar;
        boolean include = true;
        boolean definingRange = false;
        int idxFirst = idx;
        char rangeStart = Character.MIN_VALUE;
        char rangeEnd;
        RERange range = new RERange();
        while (idx < len && pattern.charAt(idx) != ']')
        {

            switchOnCharacter:

            // Switch on character
            switch (pattern.charAt(idx))
            {
            
                case '^':
                    // Update to match R syntax:
                    // Caret is special character ONLY if it is the
                    // first character in the class, otherwise 
                    // treated as literal
                    if (idx == idxFirst)
                    {
                        range.include(Character.MIN_VALUE, Character.MAX_VALUE, true);
                        include = !include;
                        idx++;
                        continue;
                    } else {
                        // treat as literal
                        simpleChar = '^';
                        idx++;
                        break switchOnCharacter;
                    }

                case '[':
                    String className = posixClass();
                    if(className == null) {
                      simpleChar = '[';
                      idx++;
                      break switchOnCharacter;
                    } else {
                      idx += className.length() + "[::]".length();
                      if(className.equals("alpha")) {
                        range.include('A', 'Z', include);
                        range.include('a', 'z', include);
                      } else if(className.equals("alnum")) {
                        range.include('A', 'Z', include);
                        range.include('a', 'z', include);
                        range.include('0', '9', include);
                      } else if(className.equals("blank")) {
                        range.include(' ', include);
                        range.include('\t', include);
                      } else if(className.equals("digit")) {
                        range.include('0', '9', include);
                      } else if(className.equals("upper")) {
                        range.include('A', 'Z', include);
                      } else if(className.equals("lower")) {
                        range.include('a', 'z', include);
                      } else if(className.equals("space")) {
                        range.include(' ', include);
                        range.include('\t', include);
                        range.include('\r', include);
                        range.include('\n', include);
                        range.include((char)11, include); // vertical tab
                        range.include('\f', include);
                      } else {
                        throw new RESyntaxException("Posix class " + className + " not implemented");
                      }
                    }
                    continue;
                    
                case '\\':
                {
                    // Escape always advances the stream
                    int c;
                    switch (c = escape ())
                    {
                        case ESC_COMPLEX:
                        case ESC_BACKREF:

                            // Word boundaries and backrefs not allowed in a character class!
                            syntaxError("Bad character class");

                        case ESC_CLASS:

                            // Classes can't be an endpoint of a range
                            if (definingRange)
                            {
                                syntaxError("Bad character class");
                            }

                            // Handle specific type of class (some are ok)
                            switch (pattern.charAt(idx - 1))
                            {
                                case ExtendedPattern.E_NSPACE:
                                    range.include(Character.MIN_VALUE, 7, include);   // [Min - \b )
                                    range.include((char) 11, include);                // ( \n - \f )
                                    range.include(14, 31, include);                   // ( \r - ' ')
                                    range.include(33, Character.MAX_VALUE, include);  // (' ' - Max]
                                    break;

                                case ExtendedPattern.E_NALNUM:
                                    range.include(Character.MIN_VALUE, '/', include); // [Min - '0')
                                    range.include(':', '@', include);                 // ('9' - 'A')
                                    range.include('[', '^', include);                 // ('Z' - '_')
                                    range.include('`', include);                      // ('_' - 'a')
                                    range.include('{', Character.MAX_VALUE, include); // ('z' - Max]
                                    break;

                                case ExtendedPattern.E_NDIGIT:
                                    range.include(Character.MIN_VALUE, '/', include); // [Min - '0')
                                    range.include(':', Character.MAX_VALUE, include); // ('9' - Max]
                                    break;

                                case ExtendedPattern.E_SPACE:
                                    range.include('\t', include);
                                    range.include('\r', include);
                                    range.include('\f', include);
                                    range.include('\n', include);
                                    range.include('\b', include);
                                    range.include(' ', include);
                                    break;

                                case ExtendedPattern.E_ALNUM:
                                    range.include('a', 'z', include);
                                    range.include('A', 'Z', include);
                                    range.include('_', include);

                                    // Fall through!

                                case ExtendedPattern.E_DIGIT:
                                    range.include('0', '9', include);
                                    break;
                            }

                            // Make last char invalid (can't be a range start)
                            last = CHAR_INVALID;
                            break;

                        default:

                            // Escape is simple so treat as a simple char
                            simpleChar = (char) c;
                            break switchOnCharacter;
                    }
                }
                continue;

                case '-':

                    // Premature end of range. treat this as a literal '-'
                    // note that this is a change from the jakarta impl
                    // to match the R syntax
                    if ((idx + 1) < len && pattern.charAt(++idx) == ']') {
                      simpleChar = '-';
                      break;
                    }

                    // if this is the first character in the range, then
                    // we also consider it a literal [R specific]
                    if(last == CHAR_INVALID) {
                      simpleChar = '-';
                      break;
                    }

                    // Start a range if one isn't already started
                    if (definingRange)
                    {
                        syntaxError("Bad class range");
                    }

                    definingRange = true;
                    rangeStart = last;

                    continue;

                default:
                    simpleChar = pattern.charAt(idx++);
                    break;
            }

            // Handle simple character simpleChar
            if (definingRange)
            {
                // if we are defining a range make it now
                rangeEnd = simpleChar;

                // Actually create a range if the range is ok
                if (rangeStart >= rangeEnd)
                {
                    syntaxError("Bad character class");
                }
                range.include(rangeStart, rangeEnd, include);

                // We are done defining the range
                last = CHAR_INVALID;
                definingRange = false;
            }
            else
            {
                // If simple character and not start of range, include it
                if (idx >= len || !(pattern.charAt(idx) == '-' && idx+1 < len && pattern.charAt(idx+1) != ']' ) )
                {
                    range.include(simpleChar, include);
                }
                last = simpleChar;
            }
        }

        // Shouldn't be out of input
        if (idx == len)
        {
            syntaxError("Unterminated character class");
        }

        // Absorb the ']' end of class marker
        idx++;

        // Emit character class definition
        instruction[ret + ExtendedPattern.offsetOpdata] = (char)range.num;
        for (int i = 0; i < range.num; i++)
        {
            emit((char)range.minRange[i]);
            emit((char)range.maxRange[i]);
        }
        return ret;
    }

    private String posixClass() {
      int idx = this.idx;
      // Check for POSIX character class
      if ( (idx+1) < len && pattern.charAt(idx+1) == ':')
      {
          // Skip second bracket and colon
          idx+=2;        
          
          // POSIX character classes are denoted with lowercase ASCII strings
          int idxStart = idx;
          while (idx < len && pattern.charAt(idx) >= 'a' && pattern.charAt(idx) <= 'z')
          {
              idx++;
          }

          // Should be a ":]" to terminate the POSIX character class
          if ((idx + 1) < len && pattern.charAt(idx) == ':' &&
              pattern.charAt(idx + 1) == ']' /*&& pattern.charAt(idx + 2) == ']'*/)
          {
              // Get character class
              String charClass = pattern.substring(idxStart, idx);

              idx += 2;
              return charClass;
              
          }
      }
      return null;
    }

    /**
     * Absorb an atomic character string.  This method is a little tricky because
     * it can un-include the last character of string if a closure operator follows.
     * This is correct because *+? have higher precedence than concatentation (thus
     * ABC* means AB(C*) and NOT (ABC)*).
     * @return Index of new atom node
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int atom() throws RESyntaxException
    {
        // Create a string node
        int ret = node(ExtendedPattern.OP_ATOM, 0);

        // Length of atom
        int lenAtom = 0;

        // Loop while we've got input

        atomLoop:

        while (idx < len)
        {
            // Is there a next char?
            if ((idx + 1) < len)
            {
                char c = pattern.charAt(idx + 1);

                // If the next 'char' is an escape, look past the whole escape
                if (pattern.charAt(idx) == '\\')
                {
                    int idxEscape = idx;
                    escape();
                    if (idx < len)
                    {
                        c = pattern.charAt(idx);
                    }
                    idx = idxEscape;
                }

                // Switch on next char
                switch (c)
                {
                    case '{':
                    case '?':
                    case '*':
                    case '+':

                        // If the next character is a closure operator and our atom is non-empty, the
                        // current character should bind to the closure operator rather than the atom
                        if (lenAtom != 0)
                        {
                            break atomLoop;
                        }
                }
            }

            // Switch on current char
            switch (pattern.charAt(idx))
            {
                case ']':
                case '^':
                case '$':
                case '.':
                case '[':
                case '(':
                case ')':
                case '|':
                    break atomLoop;

                case '{':
                case '?':
                case '*':
                case '+':

                    // We should have an atom by now
                    if (lenAtom == 0)
                    {
                        // No atom before closure
                        syntaxError("Missing operand to closure");
                    }
                    break atomLoop;

                case '\\':

                    {
                        // Get the escaped character (advances input automatically)
                        int idxBeforeEscape = idx;
                        int c = escape();

                        // Check if it's a simple escape (as opposed to, say, a backreference)
                        if ((c & ESC_MASK) == ESC_MASK)
                        {
                            // Not a simple escape, so backup to where we were before the escape.
                            idx = idxBeforeEscape;
                            break atomLoop;
                        }

                        // Add escaped char to atom
                        emit((char) c);
                        lenAtom++;
                    }
                    break;

                default:

                    // Add normal character to atom
                    emit(pattern.charAt(idx++));
                    lenAtom++;
                    break;
            }
        }

        // This "shouldn't" happen
        if (lenAtom == 0)
        {
            internalError();
        }

        // Emit the atom length into the program
        instruction[ret + ExtendedPattern.offsetOpdata] = (char)lenAtom;
        return ret;
    }

    /**
     * Match a terminal node.
     * @param flags Flags
     * @return Index of terminal node (closeable)
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int terminal(int[] flags) throws RESyntaxException
    {
        switch (pattern.charAt(idx))
        {
        case ExtendedPattern.OP_EOL:
        case ExtendedPattern.OP_BOL:
        case ExtendedPattern.OP_ANY:
            return node(pattern.charAt(idx++), 0);

        case '[':
            return characterClass();

        case '(':
            return expr(flags);

        case ')':
            syntaxError("Unexpected close paren");

        case '|':
            internalError();

        case ']':
            syntaxError("Mismatched class");

        case 0:
            syntaxError("Unexpected end of input");

        case '?':
        case '+':
        case '{':
        case '*':
            syntaxError("Missing operand to closure");

        case '\\':
            {
                // Don't forget, escape() advances the input stream!
                int idxBeforeEscape = idx;

                // Switch on escaped character
                switch (escape())
                {
                    case ESC_CLASS:
                    case ESC_COMPLEX:
                        flags[0] &= ~NODE_NULLABLE;
                        return node(ExtendedPattern.OP_ESCAPE, pattern.charAt(idx - 1));

                    case ESC_BACKREF:
                        {
                            char backreference = (char)(pattern.charAt(idx - 1) - '0');
                            if (parens <= backreference)
                            {
                                syntaxError("Bad backreference");
                            }
                            flags[0] |= NODE_NULLABLE;
                            return node(ExtendedPattern.OP_BACKREF, backreference);
                        }

                    default:

                        // We had a simple escape and we want to have it end up in
                        // an atom, so we back up and fall though to the default handling
                        idx = idxBeforeEscape;
                        flags[0] &= ~NODE_NULLABLE;
                        break;
                }
            }
        }

        // Everything above either fails or returns.
        // If it wasn't one of the above, it must be the start of an atom.
        flags[0] &= ~NODE_NULLABLE;
        return atom();
    }

    /**
     * Compile a possibly closured terminal
     * @param flags Flags passed by reference
     * @return Index of closured node
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int closure(int[] flags) throws RESyntaxException
    {
        // Before terminal
        int idxBeforeTerminal = idx;

        // Values to pass by reference to terminal()
        int[] terminalFlags = { NODE_NORMAL };

        // Get terminal symbol
        int ret = terminal(terminalFlags);

        // Or in flags from terminal symbol
        flags[0] |= terminalFlags[0];

        // Advance input, set NODE_NULLABLE flag and do sanity checks
        if (idx >= len)
        {
            return ret;
        }

        boolean greedy = true;
        char closureType = pattern.charAt(idx);
        switch (closureType)
        {
            case '?':
            case '*':

                // The current node can be null
                flags[0] |= NODE_NULLABLE;

                // Drop through

            case '+':

                // Eat closure character
                idx++;

                // Drop through

            case '{':

                // Don't allow blantant stupidity
                int opcode = instruction[ret /* + RE.offsetOpcode */];
                if (opcode == ExtendedPattern.OP_BOL || opcode == ExtendedPattern.OP_EOL)
                {
                    syntaxError("Bad closure operand");
                }
        }

        // If the next character is a '?', make the closure non-greedy (reluctant)
        if (idx < len && pattern.charAt(idx) == '?')
        {
            idx++;
            greedy = false;
        }

        if (greedy)
        {
            // Actually do the closure now
            switch (closureType)
            {
                case '{':
                {
                    bracket();
                    int bracketEnd = idx;
                    int bracketMin = this.bracketMin;
                    int bracketOpt = this.bracketOpt;

                    // Pointer to the last terminal
                    int pos = ret;

                    // Process min first
                    for (int c = 0; c < bracketMin; c++)
                    {
                        // Rewind stream and run it through again - more matchers coming
                        idx = idxBeforeTerminal;
                        setNextOfEnd(pos, pos = terminal(terminalFlags));
                    }

                    // Do the right thing for maximum ({m,})
                    if (bracketOpt == bracketUnbounded)
                    {
                        // Drop through now and closure expression.
                        // We are done with the {m,} expr, so skip rest
                        idx = bracketEnd;
                        nodeInsert(ExtendedPattern.OP_STAR, 0, pos);
                        setNextOfEnd(pos + ExtendedPattern.nodeSize, pos);
                        break;
                    }
                    else if (bracketOpt > 0)
                    {
                        int opt[] = new int[bracketOpt + 1];
                        // Surround first optional terminal with MAYBE
                        nodeInsert(ExtendedPattern.OP_MAYBE, 0, pos);
                        opt[0] = pos;

                        // Add all the rest optional terminals with preceeding MAYBEs
                        for (int c = 1; c < bracketOpt; c++)
                        {
                            opt[c] = node(ExtendedPattern.OP_MAYBE, 0);
                            // Rewind stream and run it through again - more matchers coming
                            idx = idxBeforeTerminal;
                            terminal(terminalFlags);
                        }

                        // Tie ends together
                        int end = opt[bracketOpt] = node(ExtendedPattern.OP_NOTHING, 0);
                        for (int c = 0; c < bracketOpt; c++)
                        {
                            setNextOfEnd(opt[c], end);
                            setNextOfEnd(opt[c] + ExtendedPattern.nodeSize, opt[c + 1]);
                        }
                    }
                    else
                    {
                        // Rollback terminal - no opt matchers present
                        lenInstruction = pos;
                        node(ExtendedPattern.OP_NOTHING, 0);
                    }

                    // We are done. skip the reminder of {m,n} expr
                    idx = bracketEnd;
                    break;
                }

                case '?':
                {
                    nodeInsert(ExtendedPattern.OP_MAYBE, 0, ret);
                    int n = node(ExtendedPattern.OP_NOTHING, 0);
                    setNextOfEnd(ret, n);
                    setNextOfEnd(ret + ExtendedPattern.nodeSize, n);
                    break;
                }

                case '*':
                {
                    nodeInsert(ExtendedPattern.OP_STAR, 0, ret);
                    setNextOfEnd(ret + ExtendedPattern.nodeSize, ret);
                    break;
                }

                case '+':
                {
                    nodeInsert(ExtendedPattern.OP_CONTINUE, 0, ret);
                    int n = node(ExtendedPattern.OP_PLUS, 0);
                    setNextOfEnd(ret + ExtendedPattern.nodeSize, n);
                    setNextOfEnd(n, ret);
                    break;
                }
            }
        }
        else
        {
            // Actually do the closure now
            switch (closureType)
            {
                case '?':
                {
                    nodeInsert(ExtendedPattern.OP_RELUCTANTMAYBE, 0, ret);
                    int n = node(ExtendedPattern.OP_NOTHING, 0);
                    setNextOfEnd(ret, n);
                    setNextOfEnd(ret + ExtendedPattern.nodeSize, n);
                    break;
                }

                case '*':
                {
                    nodeInsert(ExtendedPattern.OP_RELUCTANTSTAR, 0, ret);
                    setNextOfEnd(ret + ExtendedPattern.nodeSize, ret);
                    break;
                }

                case '+':
                {
                    nodeInsert(ExtendedPattern.OP_CONTINUE, 0, ret);
                    int n = node(ExtendedPattern.OP_RELUCTANTPLUS, 0);
                    setNextOfEnd(n, ret);
                    setNextOfEnd(ret + ExtendedPattern.nodeSize, n);
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Compile body of one branch of an or operator (implements concatenation)
     *
     * @param flags Flags passed by reference
     * @return Pointer to first node in the branch
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int branch(int[] flags) throws RESyntaxException
    {
        // Get each possibly closured piece and concat
        int node;
        int ret = -1;
        int chain = -1;
        int[] closureFlags = new int[1];
        boolean nullable = true;
        while (idx < len && pattern.charAt(idx) != '|' && pattern.charAt(idx) != ')')
        {
            // Get new node
            closureFlags[0] = NODE_NORMAL;
            node = closure(closureFlags);
            if (closureFlags[0] == NODE_NORMAL)
            {
                nullable = false;
            }

            // If there's a chain, append to the end
            if (chain != -1)
            {
                setNextOfEnd(chain, node);
            }

            // Chain starts at current
            chain = node;
            if (ret == -1) {
                ret = node;
            }
        }

        // If we don't run loop, make a nothing node
        if (ret == -1)
        {
            ret = node(ExtendedPattern.OP_NOTHING, 0);
        }

        // Set nullable flag for this branch
        if (nullable)
        {
            flags[0] |= NODE_NULLABLE;
        }

        return ret;
    }

    /**
     * Compile an expression with possible parens around it.  Paren matching
     * is done at this level so we can tie the branch tails together.
     *
     * @param flags Flag value passed by reference
     * @return Node index of expression in instruction array
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     */
    int expr(int[] flags) throws RESyntaxException
    {
        // Create open paren node unless we were called from the top level (which has no parens)
        int paren = -1;
        int ret = -1;
        int closeParens = parens;
        if ((flags[0] & NODE_TOPLEVEL) == 0 && pattern.charAt(idx) == '(')
        {
            // if its a cluster ( rather than a proper subexpression ie with backrefs )
            if (idx + 2 < len && pattern.charAt(idx + 1) == '?' && pattern.charAt(idx + 2) == ':')
            {
                paren = 2;
                idx += 3;
                ret = node(ExtendedPattern.OP_OPEN_CLUSTER, 0);
            } else {
                
                // This implementation doesn't support look ahead/behind assertions.
                // But at least we can give a decent error message
                
                if(idx +2 < len && pattern.charAt(idx + 1) == '?') 
                {
                    // Check for assertions, which we do not support
                    char qualifier = pattern.charAt(idx + 2);
                    if(qualifier == '=' || qualifier == '!') {
                        throw new RESyntaxException("Look-ahead assertions are not implemented");
                    } else if(qualifier == '<' && idx + 3 < len) {
                        char next = pattern.charAt(idx + 3);
                        if(next == '=' || next == '!') {
                            throw new RESyntaxException("Look-behind assertions are not implemented");
                        }
                    }
                }
                paren = 1;
                idx++;
                ret = node(ExtendedPattern.OP_OPEN, parens++);
            }
        }
        flags[0] &= ~NODE_TOPLEVEL;

        // Process contents of first branch node
        boolean open = false;
        int branch = branch(flags);
        if (ret == -1)
        {
            ret = branch;
        }
        else
        {
            setNextOfEnd(ret, branch);
        }

        // Loop through branches
        while (idx < len && pattern.charAt(idx) == '|')
        {
            // Now open the first branch since there are more than one
            if (!open) {
                nodeInsert(ExtendedPattern.OP_BRANCH, 0, branch);
                open = true;
            }

            idx++;
            setNextOfEnd(branch, branch = node(ExtendedPattern.OP_BRANCH, 0));
            branch(flags);
        }

        // Create an ending node (either a close paren or an OP_END)
        int end;
        if (paren > 0)
        {
            if (idx < len && pattern.charAt(idx) == ')')
            {
                idx++;
            }
            else
            {
                syntaxError("Missing close paren");
            }
            if (paren == 1)
            {
                end = node(ExtendedPattern.OP_CLOSE, closeParens);
            }
            else
            {
                end = node(ExtendedPattern.OP_CLOSE_CLUSTER, 0);
            }
        }
        else
        {
            end = node(ExtendedPattern.OP_END, 0);
        }

        // Append the ending node to the ret nodelist
        setNextOfEnd(ret, end);

        // Hook the ends of each branch to the end node
        int currentNode = ret;
        int nextNodeOffset = instruction[currentNode + ExtendedPattern.offsetNext];
        // while the next node o
        while (nextNodeOffset != 0 && currentNode < lenInstruction)
        {
            // If branch, make the end of the branch's operand chain point to the end node.
            if (instruction[currentNode /* + RE.offsetOpcode */] == ExtendedPattern.OP_BRANCH)
            {
                setNextOfEnd(currentNode + ExtendedPattern.nodeSize, end);
            }
            nextNodeOffset = instruction[currentNode + ExtendedPattern.offsetNext];
            currentNode += nextNodeOffset;
        }

        // Return the node list
        return ret;
    }

    /**
     * Compiles a regular expression pattern into a program runnable by the pattern
     * matcher class 'RE'.
     * @param pattern Regular expression pattern to compile (see RECompiler class
     * for details).
     * @return A compiled regular expression program.
     * @exception RESyntaxException Thrown if the regular expression has invalid syntax.
     * @see RECompiler
     * @see ExtendedPattern
     */
    public REProgram compile(String pattern) throws RESyntaxException
    {
        // Initialize variables for compilation
        this.pattern = pattern;                         // Save pattern in instance variable
        len = pattern.length();                         // Precompute pattern length for speed
        idx = 0;                                        // Set parsing index to the first character
        lenInstruction = 0;                             // Set emitted instruction count to zero
        parens = 1;                                     // Set paren level to 1 (the implicit outer parens)

        // Initialize pass by reference flags value
        int[] flags = { NODE_TOPLEVEL };

        // Parse expression
        expr(flags);

        // Should be at end of input
        if (idx != len)
        {
            if (pattern.charAt(idx) == ')')
            {
                syntaxError("Unmatched close paren");
            }
            syntaxError("Unexpected input remains");
        }

        // Return the result
        char[] ins = new char[lenInstruction];
        System.arraycopy(instruction, 0, ins, 0, lenInstruction);
        return new REProgram(parens, ins);
    }

    /**
     * Local, nested class for maintaining character ranges for character classes.
     */
    class RERange
    {
        int size = 16;                      // Capacity of current range arrays
        int[] minRange = new int[size];     // Range minima
        int[] maxRange = new int[size];     // Range maxima
        int num = 0;                        // Number of range array elements in use

        /**
         * Deletes the range at a given index from the range lists
         * @param index Index of range to delete from minRange and maxRange arrays.
         */
        void delete(int index)
        {
            // Return if no elements left or index is out of range
            if (num == 0 || index >= num)
            {
                return;
            }

            // Move elements down
            while (++index < num)
            {
                if (index - 1 >= 0)
                {
                    minRange[index-1] = minRange[index];
                    maxRange[index-1] = maxRange[index];
                }
            }

            // One less element now
            num--;
        }

        /**
         * Merges a range into the range list, coalescing ranges if possible.
         * @param min Minimum end of range
         * @param max Maximum end of range
         */
        void merge(int min, int max)
        {
            // Loop through ranges
            for (int i = 0; i < num; i++)
            {
                // Min-max is subsumed by minRange[i]-maxRange[i]
                if (min >= minRange[i] && max <= maxRange[i])
                {
                    return;
                }

                // Min-max subsumes minRange[i]-maxRange[i]
                else if (min <= minRange[i] && max >= maxRange[i])
                {
                    delete(i);
                    merge(min, max);
                    return;
                }

                // Min is in the range, but max is outside
                else if (min >= minRange[i] && min <= maxRange[i])
                {
                    min = minRange[i];
                    delete(i);
                    merge(min, max);
                    return;
                }

                // Max is in the range, but min is outside
                else if (max >= minRange[i] && max <= maxRange[i])
                {
                    max = maxRange[i];
                    delete(i);
                    merge(min, max);
                    return;
                }
            }

            // Must not overlap any other ranges
            if (num >= size)
            {
                size *= 2;
                int[] newMin = new int[size];
                int[] newMax = new int[size];
                System.arraycopy(minRange, 0, newMin, 0, num);
                System.arraycopy(maxRange, 0, newMax, 0, num);
                minRange = newMin;
                maxRange = newMax;
            }
            minRange[num] = min;
            maxRange[num] = max;
            num++;
        }

        /**
         * Removes a range by deleting or shrinking all other ranges
         * @param min Minimum end of range
         * @param max Maximum end of range
         */
        void remove(int min, int max)
        {
            // Loop through ranges
            for (int i = 0; i < num; i++)
            {
                // minRange[i]-maxRange[i] is subsumed by min-max
                if (minRange[i] >= min && maxRange[i] <= max)
                {
                    delete(i);
                    return;
                }

                // min-max is subsumed by minRange[i]-maxRange[i]
                else if (min >= minRange[i] && max <= maxRange[i])
                {
                    int minr = minRange[i];
                    int maxr = maxRange[i];
                    delete(i);
                    if (minr < min)
                    {
                        merge(minr, min - 1);
                    }
                    if (max < maxr)
                    {
                        merge(max + 1, maxr);
                    }
                    return;
                }

                // minRange is in the range, but maxRange is outside
                else if (minRange[i] >= min && minRange[i] <= max)
                {
                    minRange[i] = max + 1;
                    return;
                }

                // maxRange is in the range, but minRange is outside
                else if (maxRange[i] >= min && maxRange[i] <= max)
                {
                    maxRange[i] = min - 1;
                    return;
                }
            }
        }

        /**
         * Includes (or excludes) the range from min to max, inclusive.
         * @param min Minimum end of range
         * @param max Maximum end of range
         * @param include True if range should be included.  False otherwise.
         */
        void include(int min, int max, boolean include)
        {
            if (include)
            {
                merge(min, max);
            }
            else
            {
                remove(min, max);
            }
        }

        /**
         * Includes a range with the same min and max
         * @param minmax Minimum and maximum end of range (inclusive)
         * @param include True if range should be included.  False otherwise.
         */
        void include(char minmax, boolean include)
        {
            include(minmax, minmax, include);
        }
    }
}