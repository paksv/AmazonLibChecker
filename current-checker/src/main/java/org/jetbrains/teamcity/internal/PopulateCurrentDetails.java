package org.jetbrains.teamcity.internal;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.model.InstanceType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Sergey.Pak on 9/23/2015.
 */
public class PopulateCurrentDetails {

    public static void main(String[] args) throws IOException {
        new File("txt").mkdirs();
        populateInstanceTypes();
        populateRegions();
        System.out.println("DONE!!!!");
    }

    private static void populateRegions() throws IOException {
        final File file = new File("txt/supportedRegions.txt");
        PrintWriter writer = new PrintWriter(file);
        for (Region ec2Region : RegionUtils.getRegionsForService("ec2")) {
            writer.println(ec2Region.getServiceEndpoint("ec2"));
        }
        writer.close();
        System.out.println("Wrote region info to " + file.getCanonicalPath());
    }

    private static void populateInstanceTypes() throws IOException {
        final File file = new File("txt/supportedInstanceTypes.txt");
        PrintWriter writer = new PrintWriter(file);
        for (InstanceType type : InstanceType.values()) {
            writer.println(type.toString());
        }
        writer.close();
        System.out.println("Wrote instances info to " + file.getCanonicalPath());
    }
}
