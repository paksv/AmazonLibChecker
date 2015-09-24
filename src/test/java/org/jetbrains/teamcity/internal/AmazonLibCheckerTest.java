package org.jetbrains.teamcity.internal;

import com.amazonaws.services.ec2.model.InstanceType;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Sergey.Pak on 9/23/2015.
 */
@Test
public class AmazonLibCheckerTest {
    private static final Logger logger = Logger.getLogger(AmazonLibCheckerTest.class);

    private String myLatestVersion;

    @BeforeMethod
    public void setUp() throws IOException {
        final ClassLoader libLoader = InstanceType.class.getClassLoader();
        final InputStream resStream = libLoader.getResourceAsStream("META-INF/maven/com.amazonaws/aws-java-sdk/pom.properties");
        final Properties props = new Properties();
        props.load(resStream);
        myLatestVersion = props.getProperty("version");
    }


    public void checkAllInstancesSupported() throws IOException {
        final Set<String> supported = new HashSet<String>();
        final File file = new File("src/test/resources/supportedInstanceTypes.txt");
        supported.addAll(FileUtils.readLines(file, Charset.defaultCharset()));
        final InstanceType[] values = InstanceType.values();
        final Set<String> newUnsupported = new HashSet<String>();
        for (InstanceType type : values) {
            final String typeName = type.toString();
            logger.debug("Checking " + typeName);
            if (supported.contains(typeName)){
                supported.remove(typeName);
            } else {
                newUnsupported.add(typeName);
            }
        }
        boolean fail = newUnsupported.size() > 0 || supported.size() > 0;
        ClassLoader libLoader = InstanceType.class.getClassLoader();
        libLoader.getResource("test.txt");
        if (fail){
            final StringBuilder failureMsg = new StringBuilder();
            if (newUnsupported.size() > 0){
                failureMsg.append(String.format("Latest version %s has new instance types: %s", myLatestVersion, Arrays.toString(newUnsupported.toArray())));
            }
            if (supported.size() >0){
                if (failureMsg.length() > 0){
                    failureMsg.append("\n");
                }
                failureMsg.append(String.format("Latest version %s no longer support instance types: %s", myLatestVersion, Arrays.toString(supported.toArray())));
            }
            Assert.fail(failureMsg.toString());
        }
    }



    @AfterMethod
    public void tearDown(){

    }
}
