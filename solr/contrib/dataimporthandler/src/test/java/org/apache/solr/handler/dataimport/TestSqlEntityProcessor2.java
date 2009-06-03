/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.dataimport;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Test for SqlEntityProcessor which checks full and delta imports using the
 * test harness
 * </p>
 *
 * @version $Id: TestSqlEntityProcessor2.java 723824 2008-12-05 19:14:11Z shalin $
 * @since solr 1.3
 */
public class TestSqlEntityProcessor2 extends AbstractDataImportHandlerTest {
  @Override
  public String getSchemaFile() {
    return "dataimport-schema.xml";
  }

  @Override
  public String getSolrConfigFile() {
    return "dataimport-solrconfig.xml";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCompositePk_FullImport() throws Exception {
    List parentRow = new ArrayList();
    parentRow.add(createMap("id", "1"));
    MockDataSource.setIterator("select * from x", parentRow.iterator());

    List childRow = new ArrayList();
    childRow.add(createMap("desc", "hello"));

    MockDataSource.setIterator("select * from y where y.A=1", childRow
            .iterator());

    super.runFullImport(dataConfig);

    assertQ(req("id:1"), "//*[@numFound='1']");
    assertQ(req("desc:hello"), "//*[@numFound='1']");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCompositePk_DeltaImport() throws Exception {
    List deltaRow = new ArrayList();
    deltaRow.add(createMap("id", "5"));
    MockDataSource.setIterator("select id from x where last_modified > NOW",
            deltaRow.iterator());

    List parentRow = new ArrayList();
    parentRow.add(createMap("id", "5"));
    MockDataSource.setIterator("select * from x where id = '5'", parentRow
            .iterator());

    List childRow = new ArrayList();
    childRow.add(createMap("desc", "hello"));
    MockDataSource.setIterator("select * from y where y.A=5", childRow
            .iterator());

    super.runDeltaImport(dataConfig);

    assertQ(req("id:5"), "//*[@numFound='1']");
    assertQ(req("desc:hello"), "//*[@numFound='1']");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCompositePk_DeltaImport_DeletedPkQuery() throws Exception {
    List parentRow = new ArrayList();
    parentRow.add(createMap("id", "11"));
    MockDataSource.setIterator("select * from x", parentRow.iterator());

    List childRow = new ArrayList();
    childRow.add(createMap("desc", "hello"));

    MockDataSource.setIterator("select * from y where y.A=11", childRow
            .iterator());

    super.runFullImport(dataConfig);

    assertQ(req("id:11"), "//*[@numFound='1']");



    List deltaRow = new ArrayList();
    deltaRow.add(createMap("id", "15"));
    deltaRow.add(createMap("id", "17"));
    MockDataSource.setIterator("select id from x where last_modified > NOW",
            deltaRow.iterator());

    List deltaDeleteRow = new ArrayList();
    deltaDeleteRow.add(createMap("id", "11"));
    deltaDeleteRow.add(createMap("id", "17"));
    MockDataSource.setIterator("select id from x where last_modified > NOW AND deleted='true'",
            deltaDeleteRow.iterator());

    parentRow = new ArrayList();
    parentRow.add(createMap("id", "15"));
    MockDataSource.setIterator("select * from x where id = '15'", parentRow
            .iterator());

    parentRow = new ArrayList();
    parentRow.add(createMap("id", "17"));
    MockDataSource.setIterator("select * from x where id = '17'", parentRow
            .iterator());

    super.runDeltaImport(dataConfig);

    assertQ(req("id:15"), "//*[@numFound='1']");
    assertQ(req("id:11"), "//*[@numFound='0']");
    assertQ(req("id:17"), "//*[@numFound='0']");


  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCompositePk_DeltaImport_DeltaImportQuery() throws Exception {
    List deltaRow = new ArrayList();
    deltaRow.add(createMap("id", "5"));
    MockDataSource.setIterator("select id from x where last_modified > NOW",
            deltaRow.iterator());

    List parentRow = new ArrayList();
    parentRow.add(createMap("id", "5"));
    MockDataSource.setIterator("select * from x where id=5", parentRow
            .iterator());

    List childRow = new ArrayList();
    childRow.add(createMap("desc", "hello"));
    MockDataSource.setIterator("select * from y where y.A=5", childRow
            .iterator());

    super.runDeltaImport(dataConfig_deltaimportquery);

    assertQ(req("id:5"), "//*[@numFound='1']");
    assertQ(req("desc:hello"), "//*[@numFound='1']");
  }

  private static String dataConfig = "<dataConfig>\n"
          + "       <document>\n"
          + "               <entity name=\"x\" pk=\"id\" query=\"select * from x\" deletedPkQuery=\"select id from x where last_modified > NOW AND deleted='true'\" deltaQuery=\"select id from x where last_modified > NOW\">\n"
          + "                       <field column=\"id\" />\n"
          + "                       <entity name=\"y\" query=\"select * from y where y.A=${x.id}\">\n"
          + "                               <field column=\"desc\" />\n"
          + "                       </entity>\n" + "               </entity>\n"
          + "       </document>\n" + "</dataConfig>\n";

  private static String dataConfig_deltaimportquery = "<dataConfig>\n"
          + "       <document>\n"
          + "               <entity name=\"x\" deltaImportQuery=\"select * from x where id=${dataimporter.delta.id}\" deltaQuery=\"select id from x where last_modified > NOW\">\n"
          + "                       <field column=\"id\" />\n"
          + "                       <entity name=\"y\" query=\"select * from y where y.A=${x.id}\">\n"
          + "                               <field column=\"desc\" />\n"
          + "                       </entity>\n" + "               </entity>\n"
          + "       </document>\n" + "</dataConfig>\n";
}
