package org.jetbrains.teamcity.internal;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
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
    private String myCurrentVersion;

    @BeforeMethod
    public void setUp() throws IOException {
        final ClassLoader libLoader = InstanceType.class.getClassLoader();
        final InputStream resStream = libLoader.getResourceAsStream("META-INF/maven/com.amazonaws/aws-java-sdk/pom.properties");
        final Properties props = new Properties();
        props.load(resStream);
        myLatestVersion = props.getProperty("version");
        myCurrentVersion = System.getProperty("aws.sdk.version");
        System.out.printf("Current AWS SDK version:%s\nLatest AWS SDK:%s\n",
                myCurrentVersion, myLatestVersion);
    }


    public void checkAllInstancesSupported() throws IOException {
        final Set<String> supported = new HashSet<String>();
        final Set<String> latest = new HashSet<String>();
        final File file = new File("../txt/supportedInstanceTypes.txt");
        System.out.println("Comparing instances info with " + file.getCanonicalPath());
        supported.addAll(FileUtils.readLines(file, Charset.defaultCharset()));
        final InstanceType[] values = InstanceType.values();
        for (InstanceType type : values) {
            latest.add(type.toString());
        }
        checkAllEntitiesSupported(supported, latest, "instance type");
    }

    public void checkAllRegionsSupported() throws IOException {
        final Set<String> supported = new HashSet<String>();
        final Set<String> latest = new HashSet<String>();
        final File file = new File("../txt/supportedRegions.txt");
        System.out.println("Comparing region info with " + file.getCanonicalPath());
        supported.addAll(FileUtils.readLines(file, Charset.defaultCharset()));
        final List<com.amazonaws.regions.Region> ec2Regions = RegionUtils.getRegionsForService("ec2");
        for (Region ec2Region : ec2Regions) {
            latest.add(ec2Region.getServiceEndpoint("ec2"));
        }
        checkAllEntitiesSupported(supported, latest, "region");
    }

    private void checkAllEntitiesSupported(final Set<String> supported, final Set<String> latest, String entityName){
        final Set<String> newUnsupported = new HashSet<String>();
        for (String type : latest) {
            logger.debug("Checking " + type);
            if (supported.contains(type)){
                supported.remove(type);
            } else {
                newUnsupported.add(type);
            }
        }
        boolean fail = newUnsupported.size() > 0 || supported.size() > 0;
        if (fail){
            final StringBuilder failureMsg = new StringBuilder();
            if (newUnsupported.size() > 0){
                failureMsg.append(String.format("Latest version %s has new %s(s) that %s hasn't: %s",
                        myLatestVersion,
                        entityName,
                        myCurrentVersion,
                        Arrays.toString(newUnsupported.toArray())));
            }
            if (supported.size() >0){
                if (failureMsg.length() > 0){
                    failureMsg.append("\n");
                }
                failureMsg.append(String.format("Latest version %s no longer support %s(s) that %s does: %s",
                        myLatestVersion,
                        entityName,
                        myCurrentVersion,
                        Arrays.toString(supported.toArray())));
            }
            Assert.fail(failureMsg.toString());
        }
    }

    @AfterMethod
    public void tearDown(){

    }
}
