/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

import freemarker.template.Configuration;


def primitives = [ 'byte', 'short', 'char', 'int', 'long', 'float', 'double' ];


def templateCfg = new Configuration();
templateCfg.setDirectoryForTemplateLoading(new File("src/main/template"))
templateCfg.setDefaultEncoding("UTF-8");

// FieldPtr classes
// These provide a way to provide the address to a global variable
// compiled as a static field

def fieldPtrTemplate = templateCfg.getTemplate("FieldPtr.ftl")

primitives.each({ type ->
    def capitalizedType = type.substring(0, 1).toUpperCase() + type.substring(1);
    def className = "${capitalizedType}FieldPtr"
    def outputFile = new File("src/main/java/org/renjin/gcc/runtime/${className}.java")

    def model = [ className: className, type: type ];

    outputFile.withWriter("UTF-8") { writer ->
        fieldPtrTemplate.process(model, writer);
    }
})
