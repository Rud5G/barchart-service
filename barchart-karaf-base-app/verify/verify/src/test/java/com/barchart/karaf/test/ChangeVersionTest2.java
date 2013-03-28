package com.barchart.karaf.test;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * Deploy feature-1.xml as feature-1.xml, then deploy feature-2.xml as
 * feature-2.xml. Ensure only bundle version change.
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class ChangeVersionTest2 extends TestAny {

	/**
	 * Test bundle name.
	 */
	final String BUNDLE = "com.barchart.test.barchart-karaf-base-test01";

	/**
	 * Test feature name.
	 */
	final String FEATURE = "barchart-karaf-base-test01-feature";

	/**
	 * Original version.
	 */
	final String VERSION_1 = "1.0.0.SNAPSHOT";

	/**
	 * Replacement version.
	 */
	final String VERSION_2 = "1.0.1.SNAPSHOT";

	@Configuration
	public Option[] config0() {
		return super.config();
	}

	@Configuration
	public Option[] config1() {
		return new Option[] {};
	}

	/**
	 * Deploy feature-1.xml as feature-1.xml, then deploy feature-2.xml as
	 * feature-2.xml. Ensure bundle version change.
	 */
	@Test
	public void changeVersion() throws Exception {

		final Path home = Paths.get(System.getProperty("karaf.home"));
		final Path test = home.resolve("../../../src/test/resources");

		log.info("\n\t home : {}\n\t test : {}", home, test);

		/** Original source. */
		final Path source1 = test.resolve("case-02/feature-1.xml");
		/** Replacement source. */
		final Path source2 = test.resolve("case-02/feature-2.xml");

		/** Original target */
		final Path target1 = home.resolve("etc/feature-1.xml");
		/** Replacement target */
		final Path target2 = home.resolve("etc/feature-2.xml");

		logFeatures();
		assertFeatureNotInstalled(FEATURE);
		assertBundleNotInstalled(BUNDLE);

		log.info("\n\t drop 1");
		Files.copy(source1, target1);

		log.info("\n\t wait for install 1");
		for (int k = 0; k < 100; k++) {
			if (null != bundle(BUNDLE, VERSION_1)) {
				break;
			}
			Thread.sleep(100);
		}

		logFeatures();
		assertNotNull("bundle 1 is here", bundle(BUNDLE, VERSION_1));
		assertNull("bundle 2 is missing", bundle(BUNDLE, VERSION_2));

		log.info("\n\t drop 2");
		Files.copy(source2, target2, StandardCopyOption.REPLACE_EXISTING);

		log.info("\n\t kill 1");
		Files.delete(target1);

		log.info("\n\t wait for removal 1");
		for (int k = 0; k < 100; k++) {
			if (null == bundle(BUNDLE, VERSION_1)) {
				break;
			}
			Thread.sleep(100);
		}

		assertNull("bundle 1 is gone", bundle(BUNDLE, VERSION_1));

		log.info("\n\t wait for install 2");
		for (int k = 0; k < 100; k++) {
			if (null != bundle(BUNDLE, VERSION_2)) {
				break;
			}
			Thread.sleep(100);
		}

		logFeatures();
		assertNotNull("bundle 2 is here", bundle(BUNDLE, VERSION_2));

		log.info("\n\t kill 2");
		Files.delete(target2);

		log.info("\n\t wait for removal 2");
		for (int k = 0; k < 100; k++) {
			if (null == bundle(BUNDLE, VERSION_2)) {
				break;
			}
			Thread.sleep(100);
		}

		logFeatures();
		assertBundleNotInstalled(BUNDLE);
		assertFeatureNotInstalled(FEATURE);

	}

	// @Test
	public void changeVersionMany() throws Exception {
		for (int k = 0; k < 3; k++) {
			changeVersion();
		}
	}

}
