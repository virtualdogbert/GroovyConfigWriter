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

package virtualdogbert

import com.virtualdogbert.GroovyConfigWriter
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

class GroovyConfigWriterTest extends Specification {

    def setup() {
    }

    void "test convert Grails yml closure"() {
        when:
            List<String> docs = applicationYml.split('---\n')
            GroovyConfigWriter configWriter = new GroovyConfigWriter()
            configWriter.output = new BufferedWriter(new StringWriter())
            Yaml yaml = new Yaml()

            docs.findResults { String document ->
                configWriter.writeToGroovy(yaml.load(document) as Map)
            }

            configWriter.output.flush()
            println configWriter.output.out.toString()
        then:
            configWriter.output.out.toString() == groovyOutput
    }


    void "test convert Grails yml map"() {
            when:
                List<String> docs = applicationYml.split('---\n')
                GroovyConfigWriter configWriter = new GroovyConfigWriter(indentSpacer:'  ', quoteValues:['default'], asClosure:false )
                configWriter.output = new BufferedWriter(new StringWriter())
                Yaml yaml = new Yaml()

                docs.findResults { String document ->
                    configWriter.writeToGroovy(yaml.load(document) as Map)
                }

                configWriter.output.flush()
            then:
                configWriter.output.out.toString() == groovyMapOutput
        }

    String applicationYml = """
---
grails:
    profile: web
    codegen:
        defaultPackage: test.command
info:
    app:
        name: '@info.app.name@'
        version: '@info.app.version@'
        grailsVersion: '@info.app.grailsVersion@'
spring:
    groovy:
        template:
            check-template-location: false
---
command:
    response:
        code: 405
---
grails:
    mime:
        disable:
            accept:
                header:
                    userAgents:
                        - Gecko
                        - WebKit
                        - Presto
                        - Trident
        types:
            all: '*/*'
            atom: application/atom+xml
            css: text/css
            csv: text/csv
            form: application/x-www-form-urlencoded
            html:
              - text/html
              - application/xhtml+xml
            js: text/javascript
            json:
              - application/json
              - text/json
            multipartForm: multipart/form-data
            pdf: application/pdf
            rss: application/rss+xml
            text: text/plain
            hal:
              - application/hal+json
              - application/hal+xml
            xml:
              - text/xml
              - application/xml
    urlmapping:
        cache:
            maxsize: 1000
    controllers:
        defaultScope: singleton
    converters:
        encoding: UTF-8
    views:
        default:
            codec: html
        gsp:
            encoding: UTF-8
            htmlcodec: xml
            codecs:
                expression: html
                scriptlets: html
                taglib: none
                staticparts: none
---

hibernate:
    cache:
        queries: false
        use_second_level_cache: true
        use_query_cache: false
        region.factory_class: 'org.hibernate.cache.ehcache.EhCacheRegionFactory'

endpoints:
    jmx:
        unique-names: true

dataSource:
    pooled: true
    jmxExport: true
    driverClassName: org.h2.Driver
    username: sa
    password:

environments:
    development:
        dataSource:
            dbCreate: create-drop
            url: jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE
    test:
        dataSource:
            dbCreate: update
            url: jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE
    production:
        dataSource:
            dbCreate: update
            url: jdbc:h2:./prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE
            properties:
                jmxEnabled: true
                initialSize: 5
                maxActive: 50
                minIdle: 5
                maxIdle: 25
                maxWait: 10000
                maxAge: 600000
                timeBetweenEvictionRunsMillis: 5000
                minEvictableIdleTimeMillis: 60000
                validationQuery: SELECT 1
                validationQueryTimeout: 3
                validationInterval: 15000
                testOnBorrow: true
                testWhileIdle: true
                testOnReturn: false
                jdbcInterceptors: ConnectionState
                defaultTransactionIsolation: 2 # TRANSACTION_READ_COMMITTED
"""

    String groovyOutput =
"""grails {
    profile = 'web'
    codegen {
        defaultPackage = 'test.command'
    }
}

info {
    app {
        name = '@info.app.name@'
        version = '@info.app.version@'
        grailsVersion = '@info.app.grailsVersion@'
    }
}

spring {
    groovy {
        template {
            template['check-template-location'] = false
        }
    }
}

command {
    response {
        code = 405
    }
}

grails {
    mime {
        disable {
            accept {
                header {
                    userAgents = [
                        'Gecko',
                        'WebKit',
                        'Presto',
                        'Trident'
                    ]
                }
            }
        }
        types {
            all = '*/*'
            atom = 'application/atom+xml'
            css = 'text/css'
            csv = 'text/csv'
            form = 'application/x-www-form-urlencoded'
            html = [
                'text/html',
                'application/xhtml+xml'
            ]
            js = 'text/javascript'
            json = [
                'application/json',
                'text/json'
            ]
            multipartForm = 'multipart/form-data'
            pdf = 'application/pdf'
            rss = 'application/rss+xml'
            text = 'text/plain'
            hal = [
                'application/hal+json',
                'application/hal+xml'
            ]
            xml = [
                'text/xml',
                'application/xml'
            ]
        }
    }
    urlmapping {
        cache {
            maxsize = 1000
        }
    }
    controllers {
        defaultScope = 'singleton'
    }
    converters {
        encoding = 'UTF-8'
    }
    views {
        'default' {
            codec = 'html'
        }
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml'
            codecs {
                expression = 'html'
                scriptlets = 'html'
                taglib = 'none'
                staticparts = 'none'
            }
        }
    }
}

hibernate {
    cache {
        queries = false
        use_second_level_cache = true
        use_query_cache = false
        region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
    }
}

endpoints {
    jmx {
        jmx['unique-names'] = true
    }
}

dataSource {
    pooled = true
    jmxExport = true
    driverClassName = 'org.h2.Driver'
    username = 'sa'
    password = null
}

environments {
    development {
        dataSource {
            dbCreate = 'create-drop'
            url = 'jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
        }
    }
    test {
        dataSource {
            dbCreate = 'update'
            url = 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
        }
    }
    production {
        dataSource {
            dbCreate = 'update'
            url = 'jdbc:h2:./prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
            properties {
                jmxEnabled = true
                initialSize = 5
                maxActive = 50
                minIdle = 5
                maxIdle = 25
                maxWait = 10000
                maxAge = 600000
                timeBetweenEvictionRunsMillis = 5000
                minEvictableIdleTimeMillis = 60000
                validationQuery = 'SELECT 1'
                validationQueryTimeout = 3
                validationInterval = 15000
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = false
                jdbcInterceptors = 'ConnectionState'
                defaultTransactionIsolation = 2
            }
        }
    }
}

"""

    String groovyMapOutput =
"""grails = [
  profile: 'web',
  codegen: [
    defaultPackage: 'test.command'
  ]
]

info = [
  app: [
    name: '@info.app.name@',
    version: '@info.app.version@',
    grailsVersion: '@info.app.grailsVersion@'
  ]
]

spring = [
  groovy: [
    template: [
      'check-template-location': false
    ]
  ]
]

command = [
  response: [
    code: 405
  ]
]

grails = [
  mime: [
    disable: [
      accept: [
        header: [
          userAgents: [
            'Gecko',
            'WebKit',
            'Presto',
            'Trident'
          ]
        ]
      ]
    ],
    types: [
      all: '*/*',
      atom: 'application/atom+xml',
      css: 'text/css',
      csv: 'text/csv',
      form: 'application/x-www-form-urlencoded',
      html: [
        'text/html',
        'application/xhtml+xml'
      ],
      js: 'text/javascript',
      json: [
        'application/json',
        'text/json'
      ],
      multipartForm: 'multipart/form-data',
      pdf: 'application/pdf',
      rss: 'application/rss+xml',
      text: 'text/plain',
      hal: [
        'application/hal+json',
        'application/hal+xml'
      ],
      xml: [
        'text/xml',
        'application/xml'
      ]
    ]
  ],
  urlmapping: [
    cache: [
      maxsize: 1000
    ]
  ],
  controllers: [
    defaultScope: 'singleton'
  ],
  converters: [
    encoding: 'UTF-8'
  ],
  views: [
    default: [
      codec: 'html'
    ],
    gsp: [
      encoding: 'UTF-8',
      htmlcodec: 'xml',
      codecs: [
        expression: 'html',
        scriptlets: 'html',
        taglib: 'none',
        staticparts: 'none'
      ]
    ]
  ]
]

hibernate = [
  cache: [
    queries: false,
    use_second_level_cache: true,
    use_query_cache: false,
    region: [factory_class:'org.hibernate.cache.ehcache.EhCacheRegionFactory']
  ]
]

endpoints = [
  jmx: [
    'unique-names': true
  ]
]

dataSource = [
  pooled: true,
  jmxExport: true,
  driverClassName: 'org.h2.Driver',
  username: 'sa',
  password: null
]

environments = [
  development: [
    dataSource: [
      dbCreate: 'create-drop',
      url: 'jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
    ]
  ],
  test: [
    dataSource: [
      dbCreate: 'update',
      url: 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
    ]
  ],
  production: [
    dataSource: [
      dbCreate: 'update',
      url: 'jdbc:h2:./prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE',
      properties: [
        jmxEnabled: true,
        initialSize: 5,
        maxActive: 50,
        minIdle: 5,
        maxIdle: 25,
        maxWait: 10000,
        maxAge: 600000,
        timeBetweenEvictionRunsMillis: 5000,
        minEvictableIdleTimeMillis: 60000,
        validationQuery: 'SELECT 1',
        validationQueryTimeout: 3,
        validationInterval: 15000,
        testOnBorrow: true,
        testWhileIdle: true,
        testOnReturn: false,
        jdbcInterceptors: 'ConnectionState',
        defaultTransactionIsolation: 2
      ]
    ]
  ]
]

"""
}
