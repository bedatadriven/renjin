



// Generates Java stubs from the doxygen XML files

def parseFiles() {
    def doxygenXml = new File("target/doxygen/xml")
    for (file in doxygenXml.listFiles()) {
        if (file.name.endsWith(".xml")) {
            def xml = new XmlSlurper().parse(file)
            if (xml.compounddef.@kind.equals("file")) {
                writeStubs(xml)
            }
        }
    }
}



def writeStubs(xml) {

    def headerName = xml.compounddef.compoundname.text() as String

    System.out.println("Generating stubs for ${headerName}...")

    // Find all the function definitions
    def functions = xml.compounddef.'**'.findAll { node -> node.name() == "memberdef" && node.@kind == "function" }

    if(!functions.empty) {

        // Write the corresponding java file
        def className = headerName
                .substring(0, headerName.length() - ".h".length())
                .replace("-", "_")
        def javaName = className + ".java";

        def s = new PrintWriter("../gnur-runtime/src/main/java/org/renjin/gnur/api/${javaName}")

        // Write the preamble for the java source
        s.println("// Initial template generated from ${headerName} from R 3.2.2")
        s.println("package org.renjin.gnur.api;");
        s.println()
        s.println("import org.renjin.sexp.SEXP;")
        s.println("import org.renjin.gcc.runtime.*;")
        s.println()
        s.println("@SuppressWarnings(\"unused\")");
        s.println("public final class ${className} {")
        s.println();
        s.println("  private ${className}() { }");
        s.println();
        s.println();

        for (function in functions) {
            s.println();
            try {
                def functionName = function.name.text();
                def returnType = translateType(function.type.text())
                def parameterList = translateParameterList(function)
                
                if(function.type.text().contains("F77_NAME")) {
                    functionName = functionName + "_";
                }

                s.println("  public static ${returnType} ${functionName}(${parameterList}) {");
                s.println("     throw new UnsupportedGnuApi(\"${functionName}\");")
                s.println("  }");

            } catch (Exception) {
                // unsupported types or something, leave as commented out
                s.println("  // ${function.definition} ${function.argsstring}")
            }

        }

        s.println("}")
        s.close()
    }
}



def translateType(String type) {

    if(type.contains("F77_NAME()")) {
        // somehow doxygen's preprocessor seems to fall down here
        type = type.replace("F77_NAME()", "").trim()

    }

    if(type.endsWith("()")) {
        // this seems to be a problem in doxygen's parsing...
        type = type.substring(0, type.length() - "()".length())
    }
    

    // const keywords are not revelant for us
    type = type.replaceAll(/\s*const\s*/, "")
    
    switch (type) {
        case "void":
        case "int":
        case "double":
            return type;
        
        case "void NORET":
            return "void /*NORET*/";
        
        case "SEXP":
            return "SEXP";
        
        case "SEXPNORET":
            return "SEXP /*NORET*/";
        
        case "Rboolean":
            return "boolean";
        
        case "PROTECT_INDEX":
        case "SEXPTYPE":
        case "R_xlen_t":
        case "size_t":
            return "/*${type}*/ int";
   
        case "PROTECT_INDEX *":
            return "/*${type}*/ IntPtr";
        
        case "void *":
            return "Ptr";
        
        case "int *":
            return "IntPtr";
        
        case "long *":
            return "LongPtr";
        
        case "double *":
            return "DoublePtr";
        
        case "Rbyte *":
            return "BytePtr";
        
        case "char *":
            return "CharPtr";
    }
    
    throw new UnsupportedOperationException(type);
}

def translateParameterList(function) {
    
    def params = function.param;
    if(params.size() == 1 && params[0].type.text() == "void") {
        return "";
    } else {
        def javaParams = [];
        for(int i=0;i<params.size();++i) {
            def javaType = translateType(params[i].type.text())
            def name
            if(params[i].declname.text()) {
                name = params[i].declname.text()
            } else {
                name = "p${i}"
            }
            javaParams.add("${javaType} ${name}")
        }
        return javaParams.join(", ")
    }
}

parseFiles()