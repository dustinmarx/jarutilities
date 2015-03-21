#!/usr/bin/env groovy

/**
 * findClassInJar.groovy
 *
 * findClassInJar <<root_directory>> <<string_to_search_for>>
 *
 * Script that looks for provided String in JAR files (assumed to have .jar
 * extensions) in the provided directory and all of its subdirectories.
 */

import java.util.zip.ZipFile
import java.util.zip.ZipException

rootDir = args ? args[0] : "."
fileToFind = args && args.length > 1 ? args[1] : "class"
numMatchingItems = 0
def dir = new File(rootDir)
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
            if (entry.name.contains(fileToFind))
            {
               println file
               println "\t${entry.name}"
               numMatchingItems++
            }
         }
      }
      catch (ZipException zipEx)
      {
         println "Unable to open file ${file.name}"
      }
   }
}
println "${numMatchingItems} matches found!"
