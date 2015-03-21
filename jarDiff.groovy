#!/usr/bin/env groovy

/**
 * jarDiff.groovy
 *
 * jarDiff.groovy <first_jar_file> <second_jar_file>
 *
 * Script that compares two JAR files, reporting basic characteristics of each
 * along with differences between the two JARs.
 */

if (args.length < 2)
{
   println "\nUSAGE: jarDiff.groovy <first_jar_file> <second_jar_file>\n"
   System.exit(-1)
}

TOTAL_WIDTH = 180
COLUMN_WIDTH = TOTAL_WIDTH / 2 - 3
ROW_SEPARATOR = "-".multiply(TOTAL_WIDTH)

import java.util.jar.JarFile

def file1Name = args[0]
def jar1File = new JarFile(file1Name)
def jar1 = extractJarContents(jar1File)
def file2Name = args[1]
def jar2File = new JarFile(file2Name)
def jar2 = extractJarContents(jar2File)

def entriesInJar1ButNotInJar2 = jar1.keySet() - jar2.keySet()
def entriesInJar2ButNotInJar1 = jar2.keySet() - jar1.keySet()

println ROW_SEPARATOR
println "| ${file1Name.center(COLUMN_WIDTH)} |${file2Name.center(COLUMN_WIDTH)} |"
print "| ${(Integer.toString(jar1File.size()) + " bytes").center(COLUMN_WIDTH)} |"
println "${(Integer.toString(jar2File.size()) + " bytes").center(COLUMN_WIDTH)} |"
println ROW_SEPARATOR

if (jar1File.manifest != jar2File.manifest)
{
   def manifestPreStr = "# Manifest Entries: "
   def manifest1Str = manifestPreStr + Integer.toString(jar1File.manifest.mainAttributes.size())
   print "| ${manifest1Str.center(COLUMN_WIDTH)} |"
   def manifest2Str = manifestPreStr + Integer.toString(jar2File.manifest.mainAttributes.size())
   println "${manifest2Str.center(COLUMN_WIDTH)} |"
   println ROW_SEPARATOR
}

entriesInJar1ButNotInJar2.each
{ entry1 ->
   print "| ${entry1.center(COLUMN_WIDTH)} |"
   println "${" ".center(entry1.size() > COLUMN_WIDTH ? 2 * COLUMN_WIDTH - entry1.size() : COLUMN_WIDTH)} |"
   println ROW_SEPARATOR
}
entriesInJar2ButNotInJar1.each
{ entry2 ->
   print "| ${" ".center(entry2.size() > COLUMN_WIDTH ? 2 * COLUMN_WIDTH - entry2.size() : COLUMN_WIDTH)}"
   println "| ${entry2.center(COLUMN_WIDTH)} |"
   println ROW_SEPARATOR
}

jar1.each 
{ key, value ->
   if (!entriesInJar1ButNotInJar2.contains(key))
   {
      def jar2Entry = jar2.get(key)
      if (value != jar2Entry)
      {
         println "| ${key.center(COLUMN_WIDTH)} |${jar2Entry.name.center(COLUMN_WIDTH)} |"
         if (value.crc != jar2Entry.crc)
         {
            def crc1Str = "CRC: ${value.crc}"
            def crc2Str = "CRC: ${jar2Entry.crc}"
            print "| ${crc1Str.center(COLUMN_WIDTH)} |"
            println "${crc2Str.center(COLUMN_WIDTH)} |"
         }
         if (value.size != jar2Entry.size)
         {
            def size1Str = "${value.size} bytes"
            def size2Str = "${jar2Entry.size} bytes"
            print "| ${size1Str.center(COLUMN_WIDTH)} |"
            println "${size2Str.center(COLUMN_WIDTH)} |"
         }
         if (value.time != jar2Entry.time)
         {
            def time1Str = "${new Date(value.time)}"
            def time2Str = "${new Date(jar2Entry.time)}"
            print "| ${time1Str.center(COLUMN_WIDTH)} |"
            println "${time2Str.center(COLUMN_WIDTH)} |"
         }
         println ROW_SEPARATOR
      }
   }
}

/**
 * Provide mapping of JAR entry names to characteristics about that JAR entry
 * for the JAR indicated by the provided JAR file name.
 *
 * @param jarFile JAR file from which to extract contents.
 * @return JAR entries and thir characteristics.
 */
def TreeMap<String, JarCharacteristics> extractJarContents(JarFile jarFile)
{
   def jarContents = new TreeMap<String, JarCharacteristics>()
   entries = jarFile.entries()
   entries.each
   { entry->
      jarContents.put(entry.name, new JarCharacteristics(entry.name, entry.crc, entry.size, entry.time));
   }
   return jarContents
}
