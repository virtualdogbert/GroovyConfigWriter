/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.virtualdogbert

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.lang.model.SourceVersion

/**
 * A writer for the Groovy Config Slurper format.
 */
@Slf4j
@CompileStatic
class GroovyConfigWriter {

    /**
     * A variable to keep track of the indent level.
     */
    private Integer indentLevel = 0

    /**
     * Spacer used for indenting the output.
     */
    private String indentSpacer = '    '

    /**
     * The shared writer for a groovyConfigWriter instance.
     */
    private BufferedWriter output = null

    /**
     * Values that should be quoted.
     */
    private List<String> quoteValues = ['default']

    /**
     * Constructor for creating a new GroovyConfigWriter.
     *
     * @param fileName Optional file name to write the output to. By default is not specified the output will be written to System.out.
     * @param config An Optional parameter to pass in config to be written. You can also directly call the writeToGroovy, which could be useful
     * for writing in multiple chunks. This might be useful if your config comes from a yml file, which has multiple documents.
     * @param indentSpacer Optional parameter the spacer to be used for indenting, by default four spaces is used.
     * @param quoteValues
     */
    GroovyConfigWriter(String fileName = null, Map config = null, String indentSpacer = '    ', List<String> quoteValues = ['default']) {
        this.indentSpacer = indentSpacer
        this.quoteValues = quoteValues
        Writer writer = null

        if (fileName) {
            writer = new PrintWriter(fileName, "UTF-8")
        } else {
            writer = System.out.newPrintWriter()
        }

        output = new BufferedWriter(writer)

        if (config) {
            writeToGroovy(config)
        }

    }

    /**
     * Closes the writer and flushes the output.
     */
    void close() {
        try {
            output.flush()
            output.close()
        } catch (Exception e) {
            log.error("Failed to close config output.", e)
        }
    }


    /**
     * Writes a config map, to the writer. You can use this directly to write more than one chucks on configuration out.
     * This maybe useful when writing config that comes from yml, that has multiple documents.
     *
     * @param config The config to write in the form of a map.
     */
    void writeToGroovy(Map config) {
        if (config) {
            config.each { Object key, Object value ->

                if (value instanceof Map) {
                    if ((key as String) in quoteValues) {
                        writeIndent()
                        output.write("'$key' {\n")
                    } else {
                        writeIndent()
                        output.write("$key {\n")
                    }

                    if (keyNameIsJavaName((value as Map<String, Object>).keySet())) {
                        ++indentLevel
                        writeToGroovy(value as Map)
                        --indentLevel
                    } else {
                        ++indentLevel

                        //Hack to work around keys that are not valid java names, like a name that had dashes in it.
                        (value as Map<Object, Object>).each { Object subKey, Object subValue ->
                            writeIndent()
                            output.write("$key['$subKey'] = ")
                            writePrimitiveORString(subValue)
                        }

                        --indentLevel
                    }

                    writeIndent()

                    if (!indentLevel) {
                        output.write('}\n\n')
                    } else {
                        output.write('}\n')
                    }

                } else if (value instanceof List) {
                    writeIndent()
                    output.write("$key = ")
                    writeList(value as List)
                } else {
                    writeIndent()
                    output.write("$key = ")
                    writePrimitiveORString(value)
                }
            }
        }
    }

    /**
     * Writes a list out to the config writer.
     *
     * @param list the list to write out to the config writer.
     */
    void writeList(List list) {
        int endIndex = list.size() - 1
        output.write('[\n')
        ++indentLevel

        list.eachWithIndex { Object item, int index ->

            if (item instanceof Map) {
                writeMapInList(item as Map)
            } else if (item instanceof List) {
                writeList(item as List)
            } else {
                writeIndent()
                writePrimitiveORString(item, '')
            }

            if (index != endIndex) {
                output.write(',\n')
            }
        }

        --indentLevel
        output.write('\n')
        writeIndent()
        output.write(']\n')
    }

    /**
     * Special handling for maps that happen within lists, creating a groovy map structure [:] rather than a closure based structure.
     *
     * @param map The Map to write.
     */
    private void writeMapInList(Map map) {
        int mapIndex = 0
        int endIndex = map.size() - 1
        output.write('[')
        ++indentLevel

        map.each { Object key, Object value ->

            if (value instanceof Map) {
                writeIndent()
                output.write('$key: ')
                writeMapInList(value as Map)
            } else if (value instanceof List) {
                writeIndent()
                output.write('$key: ')
                writeList(value as List)

            } else {
                writeIndent()
                output.write("$key: ")
                writePrimitiveORString(value, '')
            }

            if (mapIndex != endIndex) {
                writeIndent()
                output.write(',\n')
            }

            ++mapIndex
        }

        --indentLevel
        writeIndent()
        output.write(']\n')
    }

    /**
     * Handles writing out values, quoting strings, and writing primitives.
     *
     * @param value the value to write out.
     * @param delimiter a delimiter that goes after the value, defaulted to \n.
     */
    private void writePrimitiveORString(Object value, String delimiter = '\n') {

        if (value instanceof String) {
            output.write("'$value'$delimiter")
        } else if (instanceOfPrimitive(value)) {
            output.write("$value$delimiter")
        } else {
            throw new Exception("Value ${value.toString()}, with class ${value.getClass().name} is not valid for the GroovyConfigWriter")
        }

    }

    /**
     * Checks key names to see of they are valid java names for java 7.
     *
     * @param keys a set of keys to check
     *
     * @return true if all keys are valid, and false otherwise.
     */
    private boolean keyNameIsJavaName(Set<String> keys) {

        for (String key : keys) {

            if (!SourceVersion.RELEASE_7.isName(key) && !(quoteValues.contains(key))) {
                return false
            }

        }

        return true
    }

    /**
     * Just a helper method to write out the indents.
     */
    private void writeIndent() {
        indentLevel.times {
            output.write(indentSpacer)
        }
    }

    /**
     * Checks to see if the value is a primitive, excluding byte, which generally isn't use in config files.
     *
     * @param value The value to check.
     *
     * @return true if the value is an integer, long, double, float, short, boolean, or null.
     */
    private boolean instanceOfPrimitive(Object value) {
        value instanceof Integer ||
                value instanceof Long ||
                value instanceof Double ||
                value instanceof Float ||
                value instanceof Short ||
                value instanceof Boolean ||
                value == null
    }
}
