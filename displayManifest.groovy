#!/usr/bin/env groovy

/**
 * displayManifest.groovy
 *
 * displayManifest <<path_to_jar_file_with_desired_manifest>>
 *
 * This script displays the manifest file of the specified JAR file.
 *
 * This script is written to be very simplistic. It doesn't check to ensure that
 * the provided file is a JAR or ZIP file and, in fact, doesn't even check that
 * a file is passed on the command-line.
 */
new java.util.jar.JarFile(args[0]).manifest.mainAttributes.entrySet().each
{
   println "${it.key}: ${it.value}"
}
