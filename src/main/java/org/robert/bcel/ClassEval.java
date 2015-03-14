package org.robert.bcel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ClassEval.
 */
public class ClassEval {

    /** The Constant moi. */
    private static final String moi = "tw.gov.moi";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClassEval.class);

    /**
     * Gets the import class. 得到所有戶役政專案所直接引入的class
     * 
     * @param clazz
     *            the clazz
     * @return the import class
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Set<String> getImportClass(final Class<?> clazz) {
        InputStream is = null;
        Set<String> importedClasses;
        String canonicalName = clazz.getCanonicalName();
        if (ClassUtils.isInnerClass(clazz) && canonicalName != null) {
            canonicalName = StringUtils.substring(canonicalName, 0,
                    canonicalName.lastIndexOf("."));
        } else if (canonicalName == null) {
            return new HashSet<String>(0);
        }

        if (StringUtils.contains(canonicalName, "java.")

        ) {
            canonicalName = StringUtils.substring(canonicalName, 0,
                    canonicalName.indexOf("$"));
            // inner class
            importedClasses = new HashSet<String>(0);
            return importedClasses;
        }

        try {

            final String classPath = StringUtils.replace(canonicalName, ".",
                    File.separator) + ".class";
            final ClassLoader loader = clazz.getClassLoader();

            is = loader.getResourceAsStream(classPath);
            if (is == null) {
                System.out.println("............");
                String newCanonicalName = StringUtils.substring(canonicalName,
                        0, canonicalName.lastIndexOf("."));
                importedClasses = new HashSet<String>(0);
                return importedClasses;
            }
            importedClasses = DependencyEmitter.extractImportedClasses(is,
                    canonicalName);
            final Iterator<String> iterator = importedClasses.iterator();
            while (iterator.hasNext()) {
                String tmp = iterator.next();
                if (!tmp.contains(moi)) {
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            LOGGER.error("{}無法正常分析", clazz);
            LOGGER.error(e.getMessage(), e);
            importedClasses = new HashSet<String>(0);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return importedClasses;
    }

    private static ThreadLocal<Reflections> threadLocalReflections = new ThreadLocal<Reflections>() {

        @Override
        protected Reflections initialValue() {
            final List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
            classLoadersList.add(ClasspathHelper.contextClassLoader());
            classLoadersList.add(ClasspathHelper.staticClassLoader());
            final ConfigurationBuilder builder = new ConfigurationBuilder()
                    .setScanners(new SubTypesScanner(false),
                            new ResourcesScanner())
                    .setUrls(
                            ClasspathHelper.forClassLoader(classLoadersList
                                    .toArray(new ClassLoader[0])))
                    .filterInputsBy(
                            new FilterBuilder()
                                    .include(FilterBuilder.prefix("tw.gov.moi"))
                                    .excludePackage("tw.gov.moi.domain")
                                    .excludePackage("tw.gov.moi.security")
                                    .excludePackage("javax.servlet")
                                    .excludePackage("tw.gov.moi.dao"));

            final Reflections reflections = new Reflections(builder);
            return reflections;
        }

    };

    /**
     * Gets the all effect class. 得到所有戶役政專案所有直接、間接引入的class
     * 
     * @param clazz
     *            the clazz
     * @return the all effect class
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Set<String> getAllEffectClass(final Class<?> clazz) {
        final Set<String> result = new HashSet<String>();

        final Class<?>[] excludedInterfaces = clazz.getInterfaces();
        
        final Set<String> importedClasses = getImportClass(clazz);

        for (String className : importedClasses) {
            if (!result.contains(className)) {

                try {
                    final Class<? extends Object> unitClass = ClassUtils
                            .getClass(className, false);

                    // if(unitClass.isInterface()){
                    // TODO 如果是interface則是要將implemented的class翻出來

                    final Set<String> collection = basicAnalysisForDependency(unitClass);
                    result.addAll(collection);
                    
                    final Set<String> collection2 = implementedClassByInterface(unitClass, excludedInterfaces);

                    result.addAll(collection2);
                    
                    Set<String> importedClass = getImportClass(unitClass);
                    result.addAll(importedClass);
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (java.lang.NoSuchMethodError e) {
                    LOGGER.error("{}無法正常分析", className);
                    LOGGER.error(e.getMessage(), e);

                } catch (java.lang.NoClassDefFoundError e) {
                    LOGGER.error("{}無法正常分析", className);
                    LOGGER.error(e.getMessage(), e);
                } catch (java.lang.ExceptionInInitializerError e) {
                    LOGGER.error("{}無法正常分析", className);
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        /***
         * 自己的所實作的interface 就不需要找出關聯的..... interface所使用其方法的class才有必要找出來
         * ***/

        return result;
    }

    
    private Set<String>  implementedClassByInterface( final Class<? extends Object> unitClass, final Class<?>[] excludedInterfaces){
        final Reflections reflections = threadLocalReflections.get();
        final Set<String> result = new HashSet<String>();
        if (  !ArrayUtils.contains(excludedInterfaces, unitClass) 
                &&
                unitClass.isInterface()
                && !"java.io.Serializable".equals(unitClass.getCanonicalName())
                && StringUtils.contains(unitClass.getCanonicalName(), "tw.gov")) {

            try {
                final Set<?> allClassesSet = reflections
                        .getSubTypesOf(unitClass);
                final List<Class<?>> classes = new ArrayList<Class<?>>(
                        allClassesSet.size());
                classes.addAll((Collection<? extends Class<?>>) allClassesSet);
                final List<String> collection = ClassUtils
                        .convertClassesToClassNames(classes);
                result.addAll(collection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    private Set<String> basicAnalysisForDependency(
            final Class<? extends Object> unitClass) {
        final Set<String> importedClass = getImportClass(unitClass);       
        return importedClass;
    }
}
