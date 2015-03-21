#!/usr/bin/env groovy

/**
 * findPropertiesInJars.groovy
 *
 * findPropertiesInJars.groovy -d <<root_directories>> -p <<properties_to_search_for>>
 *
 * Script that looks for provided properties (assumed to be in files with
 * .properties extension) in JAR files (assumed to have .jar extensions) in the
 * provided directory and all of its subdirectories.
 */

def cli = new CliBuilder(
   usage: 'findPropertiesInJars.groovy -d <root_directories> -p <property_names_to_search_for>',
   header: '\nAvailable options (use -h for help):\n',
   footer: '\nInformation provided via above options is used to generate printed string.\n')
import org.apache.commons.cli.Option
cli.with
{
   h(longOpt: 'help', 'Help', args: 0, required: false)
   d(longOpt: 'directories', 'Directories to be searched', args: Option.UNLIMITED_VALUES, valueSeparator: ',', required: true)
   p(longOpt: 'properties', 'Property names to search for in JARs', args: Option.UNLIMITED_VALUES, valueSeparator: ',', required: true)
}
def opt = cli.parse(args)
if (!opt) return
if (opt.h) cli.usage()

def directories = opt.ds
def propertiesToSearchFor = opt.ps

import java.util.zip.ZipFile
import java.util.zip.ZipException

def matches = new TreeMap<String, Set<String>>()
directories.each
{ directory ->
   def dir = new File(directory)
   propertiesToSearchFor.each
   { propertyToFind ->
      dir.eachFileRecurse
      { file->
         if (file.isFile() && file.name.endsWith("jar"))
         {
            try
            {
               zip = new ZipFile(file)
               entries = zip.entries()
               entries.each
               { entry->
                  def entryName = entry.name
                  if (entryName.contains(".properties"))
                  {
                     def fullEntryName = file.canonicalPath + "!/" + entryName
                     def properties = new Properties()
                     try
                     {
                        def url = new URL("jar:file:" + File.separator + fullEntryName)
                        def jarConnection = (JarURLConnection) url.openConnection()
                        properties.load(jarConnection.inputStream)
                     }
                     catch (Exception exception)
                     {
                        println "Unable to load properties from ${fullEntryName} - ${exception}"
                     }
                     if (properties.get(propertyToFind) != null)
                     {
                        def pathPlusMatch = "${file.canonicalPath}\n\t\t${entryName}\n\t\t${propertyToFind}=${properties.get(propertyToFind)}"
                        if (matches.get(propertyToFind))
                        {
                           matches.get(propertyToFind).add(pathPlusMatch)
                        }
                        else
                        {
                           def containingJars = new TreeSet<String>()
                           containingJars.add(pathPlusMatch)
                           matches.put(propertyToFind, containingJars)
                        }
                     }
                  }
               }
            }
            catch (ZipException zipEx)
            {
               println "Unable to open JAR file ${file.name}"
            }
         }
      }
   }
}

matches.each
{ propertyName, containingJarNames ->
   println "\nProperty '${propertyName}' Found:"
   containingJarNames.each
   { containingJarName ->
      println "\t${containingJarName}"
   }
}
