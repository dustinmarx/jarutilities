#!/usr/bin/env groovy

/**
 * manifestDiff.groovy
 *
 * manifestDiff.groovy <first_jar_file> <second_jar_file>
 *
 * Script that compares the MANIFEST.MF files of two JAR files.
 */

if (args.length < 2)
{
   println "\nUSAGE: manifestDiff.groovy <first_jar_file> <second_jar_file>\n"
   System.exit(-1)
}

TOTAL_WIDTH = 180
COLUMN_WIDTH = TOTAL_WIDTH / 2 - 3
ROW_SEPARATOR = "-".multiply(TOTAL_WIDTH)

import java.util.jar.JarFile

def file1Name = args[0]
def jar1File = new JarFile(file1Name)
def num1Attrs = jar1File.manifest.mainAttributes.size()
def file2Name = args[1]
def jar2File = new JarFile(file2Name)
def num2Attrs = jar2File.manifest.mainAttributes.size()

println ROW_SEPARATOR
println "| ${file1Name.center(COLUMN_WIDTH)}| ${file2Name.center(COLUMN_WIDTH)} |"
print "| ${(Integer.toString(num1Attrs) + (num1Attrs != 1 ? " attributes" : " attribute")).center(COLUMN_WIDTH)}|"
println " ${(Integer.toString(num2Attrs) + (num2Attrs != 1 ? " atttributes" : " attribute")).center(COLUMN_WIDTH)} |"
println ROW_SEPARATOR

if (jar1File.manifest != jar2File.manifest)
{
   def manifest1Attrs = jar1File.manifest.mainAttributes
   def manifest2Attrs = jar2File.manifest.mainAttributes
   def attrsIn1ButNot2 = manifest1Attrs.keySet() - manifest2Attrs.keySet()
   def attrsIn2ButNot1 = manifest2Attrs.keySet() - manifest1Attrs.keySet()
   attrsIn1ButNot2.each
   {
      def attr1onlyStr = "${it}=${manifest1Attrs.get(it)}" 
      print "| ${attr1onlyStr.center(COLUMN_WIDTH)}| "
      println "${" ".center(attr1onlyStr.size() > COLUMN_WIDTH ? 2 * COLUMN_WIDTH - attr1onlyStr.size() : COLUMN_WIDTH)} |"
   }
   println ROW_SEPARATOR
   attrsIn2ButNot1.each
   {
      def attr2onlyStr = "${it}=${manifest2Attrs.get(it)}"
      print "| ${" ".center(attr2onlyStr.size() > COLUMN_WIDTH ? 2 * COLUMN_WIDTH - attr2onlyStr.size() : COLUMN_WIDTH)}|"
      println " ${attr2onlyStr.center(COLUMN_WIDTH)} |"
   }
   println ROW_SEPARATOR
   manifest1Attrs.each
   {
      def key = it.key
      if (it.value != manifest2Attrs.get(key) && !attrsIn1ButNot2.contains(it.key))
      {
         def attr1Str = "${key}=${manifest1Attrs.get(key)}"
         print "| ${attr1Str.center(COLUMN_WIDTH)}"
         def attr2Str = "${key}=${manifest2Attrs.get(key)}"
         println "| ${attr2Str.center(COLUMN_WIDTH)} |"
      }
   }
   println ROW_SEPARATOR
}
else
{
   println "Manifests deemed identical."
}
