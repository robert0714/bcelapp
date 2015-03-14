package org.reflections.scanners.sample;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetLibs  {
	/** The Constant logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GetLibs.class);
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());
		
		final ConfigurationBuilder builder = new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false), new ResourcesScanner())
				.setUrls(
						ClasspathHelper.forClassLoader(classLoadersList
								.toArray(new ClassLoader[0])))
				.filterInputsBy(
						new FilterBuilder()
								.include(FilterBuilder.prefix("tw.gov.moi.rl"))
								.excludePackage("tw.gov.moi.ae")
								.excludePackage("tw.gov.moi.domain")
								.excludePackage("tw.gov.moi.security")
								.excludePackage("javax.servlet")
								.excludePackage("tw.gov.moi.dao"));

		final Reflections reflections = new Reflections(builder);

		//得到當前class loader當中所有class資訊
		final Set<Class<? extends Object>> allClassesSet = reflections
				.getSubTypesOf(Object.class);
		
	}

	

}
