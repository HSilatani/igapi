package com.iggroup.webapi.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesUtil {

   private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);

   private static final String PROPERTY_FILENAME = "environment.properties";

   private static Properties theProperties;

   static{
       if (theProperties == null) {
         theProperties = new Properties();

         String filename = PROPERTY_FILENAME;
         LOG.debug("filename: " + filename);

         try {
            theProperties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(filename));
         } catch (IOException e) {
            throw new RuntimeException("Unable to load properties file: " + filename);
         }

         // additional local property file
         filename = "local.properties";
         InputStream resourceAsStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);

         if (resourceAsStream != null) {
            LOG.debug("Properties file found");
            try {
               theProperties.load(resourceAsStream);
            } catch (IOException e) {
               throw new RuntimeException("Unable to load properties file: " + filename);
            }
         }
      }

      Map<String, String> env = System.getenv();
       for (String envName : env.keySet()) {
                   System.out.format("%s=%s%n",
                           envName,
                           env.get(envName));
                   String value=env.get(envName);
                   theProperties.setProperty(envName,value);
       }
       Set<String> systemPropNames=System.getProperties().stringPropertyNames();
       for(String spname:systemPropNames){
           String value=System.getProperty(spname);
           System.out.format("%s=%s%n",spname,value);
           theProperties.setProperty(spname,value);
       }


   }

   public static Properties getProperties() throws RuntimeException {


      return theProperties;
   }

   public static String getProperty(String key) {
      return getProperties().getProperty(key);
   }

   public static void addProperty(String key,String value){
      theProperties.setProperty(key,value);
   }
}
