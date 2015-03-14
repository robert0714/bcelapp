package org.robert.bcel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class DependencyEmitter extends EmptyVisitor {

    private JavaClass javaClass;
    private static final String regExcludeExpress = "\\[L.*\\;";
    
    public DependencyEmitter(JavaClass javaClass) {
      this.javaClass = javaClass;
    }
    private Set<String>  result =new HashSet<String>();
    
    @Override
    public void visitConstantClass(ConstantClass obj) {
      ConstantPool cp = javaClass.getConstantPool();
      String bytes = obj.getBytes(cp);
      if(StringUtils.isNotBlank(bytes) &&  !bytes.matches(regExcludeExpress)){
          
          result.add(  bytes.replace("/", ".")  );
      }
    }
    public static Set<String>  extractImportedClasses(final String className) throws ClassNotFoundException{    	
        final  JavaClass javaClass = Repository.lookupClass(className);
        
        return commmonExtractImportedClasses(javaClass);
    }
    private static Set<String> commmonExtractImportedClasses(final JavaClass javaClass ){
        final  DependencyEmitter visitor = new DependencyEmitter(javaClass);
        final  DescendingVisitor classWalker = new DescendingVisitor(javaClass, visitor);        
        classWalker.visit();
        return visitor.result;
    }
    public static Set<String>  extractImportedClasses(final InputStream is ,final  String className){
        Set<String> result =null;
        ClassParser parser = new ClassParser(is, className);
        try {           
            JavaClass javaClass = parser.parse();
            
            result = commmonExtractImportedClasses(javaClass);
        } catch (ClassFormatError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
//    public static void extractFieldIsInterface(final String className){
//        final  JavaClass javaClass = Repository.lookupClass(className);
//        extractFieldIsInterface(javaClass);
//    }
//    
//    public static void extractFieldIsInterface(final  JavaClass javaClass){
//        
//        Field[] fields = javaClass.getFields();
//        for(Field aField : fields ){
//            if(aField.isInterface()){
//               String name =  aField.getName();
//               System.out.println("interface field name: "+ name);
//            }
//             
//        }        
//    }
    
    
    
    public static void main(String[] args) throws Exception {
        
        byte[] data =  FileUtils.readFileToByteArray(new File("/opt/ramdisk/work/workspaces/eclipse_38/sris-rl-core/target/classes/tw/gov/moi/rl/rl02e00/service/Rl02e00ServiceImpl.class"));
        
        InputStream is = new ByteArrayInputStream(data);
        
        Set<String>  result =  DependencyEmitter.extractImportedClasses(is ,"tw.gov.moi.rl.rl02e00.service.Rl02e00ServiceImpl");
        
        for(String className : result ){
            System.out.println(className);
        }
    }

}
