/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream; 
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

/**
 * Verifies linkage between classes in a JAR (typically a module).
 * @author Jesse Glick
 * @see "#71675"
 * @see <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html">Class file spec</a>
 */

//-------------------------
//jglick: when considering rewrites please check https://github.com/jenkinsci/constant-pool-scanner 
//-------------------------
public class VerifyClassLinkageForTest02 extends VerifyClassLinkage {

    public VerifyClassLinkageForTest02() {}

    /*
    private boolean verifyMainJar = true;dependencies
    private boolean verifyClassPathExtensions = true;
    public void setVerifyClassPathExtensions(boolean verifyClassPathExtensions) {
        this.verifyClassPathExtensions = verifyClassPathExtensions;
    }
    public void setVerifyMainJar(boolean verifyMainJar) {
        this.verifyMainJar = verifyMainJar;
    }
     */

    private static final String GlobalClassRegExpr = "[\\w|\\.]*" ;
    private static final String byteExtractRegExpr = "L([\\w|\\/]*);" ;
    
    private static Set<String> extractClassName(final String input){
        if(input == null ){
            return null; 
        }
        final Set<String> result = new HashSet<String>();
        final  Matcher matcher = Pattern.compile(byteExtractRegExpr).matcher(input);
        for(int i =0 ; matcher.find() && i<= matcher.groupCount() ;i++){
            String s = matcher.group(i);
            while (s.charAt(0) == '[') {
                // array type
                s = s.substring(1);
            }
            String c;
            if (s.charAt(s.length() - 1) == ';' && s.charAt(0) == 'L') {
                // Uncommon but seems sometimes this happens.
                c = s.substring(1, s.length() - 1);                
            } else {
                c = s;
            }
            final String name = c.replace('/', '.');
            if(name.matches(GlobalClassRegExpr)){
                result.add(name);
            }
            
        }
        return result; 
    }
    
    public static Set<String> dependencies(byte[] data) throws IOException {
        Set<String> result = new TreeSet<String>();
        DataInput input = new DataInputStream(new ByteArrayInputStream(data));
        skip(input, 8); // magic, minor_version, major_version
        int size = input.readUnsignedShort() - 1; // constantPoolCount
        String[] utf8Strings = new String[size];
        boolean[] isClassName = new boolean[size];
        boolean[] isDescriptor = new boolean[size];
        for (int i = 0; i < size; i++) {
            byte tag = input.readByte();
            switch (tag) {
                case 1: // CONSTANT_Utf8
                    utf8Strings[i] = input.readUTF();
                    break;
                case 7: // CONSTANT_Class
                    int index = input.readUnsignedShort() - 1;
                    if (index >= size) {
                        throw new IOException("@" + i + ": CONSTANT_Class_info.name_index " + index + " too big for size of pool " + size);
                    }
                    //log("Class reference at " + index, Project.MSG_DEBUG);
                    isClassName[index] = true;
                    break;
                case 3: // CONSTANT_Integer
                case 4: // CONSTANT_Float
                case 9: // CONSTANT_Fieldref
                case 10: // CONSTANT_Methodref
                case 11: // CONSTANT_InterfaceMethodref
                    skip(input, 4);
                    break;
                case 12: // CONSTANT_NameAndType
                    skip(input, 2);
                    index = input.readUnsignedShort() - 1;
                    if (index >= size || index < 0) {
                        throw new IOException("@" + i + ": CONSTANT_NameAndType_info.descriptor_index " + index + " too big for size of pool " + size);
                    }
                    isDescriptor[index] = true;
                    break;
                case 8: // CONSTANT_String
                    skip(input, 2);
                    break;
                case 5: // CONSTANT_Long
                case 6: // CONSTANT_Double
                    skip(input, 8);
                    i++; // weirdness in spec
                    break;
                default:
                    throw new IOException("Unrecognized constant pool tag " + tag + " at index " + i +
                            "; running UTF-8 strings: " + Arrays.asList(utf8Strings));
            }
        }
        //task.log("UTF-8 strings: " + Arrays.asList(utf8Strings), Project.MSG_DEBUG);
        for (int i = 0; i < size; i++) {
            String s = utf8Strings[i];
            final Set <String> classNameSet = extractClassName(s);
            
            if(s!=null ){
                if(CollectionUtils.isNotEmpty(classNameSet)){
                    result.addAll(classNameSet);
                }
                
                
            }
            
        }
        return result;
    }
    private static void skip(DataInput input, int bytes) throws IOException {
        int skipped = input.skipBytes(bytes);
        if (skipped != bytes) {
            throw new IOException("Truncated class file");
        }
    }

}