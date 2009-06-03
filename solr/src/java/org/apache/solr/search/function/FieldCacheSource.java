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

package org.apache.solr.search.function;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.ExtendedFieldCache;

/**
 * A base class for ValueSource implementations that retrieve values for
 * a single field from the {@link org.apache.lucene.search.FieldCache}.
 *
 * @version $Id: FieldCacheSource.java 629334 2008-02-20 03:36:49Z gsingers $
 */
public abstract class FieldCacheSource extends ValueSource {
  protected String field;
  protected FieldCache cache = ExtendedFieldCache.EXT_DEFAULT;

  public FieldCacheSource(String field) {
    this.field=field;
  }

  /**
   * If you are using longs or doubles, this needs to be a {@link org.apache.lucene.search.ExtendedFieldCache}.
   *
   * @param cache The {@link org.apache.lucene.search.FieldCache}
   */
  public void setFieldCache(FieldCache cache) {
    this.cache = cache;
  }



  public FieldCache getFieldCache() {
    return cache;
  }

  public String description() {
    return field;
  }

  public boolean equals(Object o) {
    if (!(o instanceof FieldCacheSource)) return false;
    FieldCacheSource other = (FieldCacheSource)o;
    return this.field.equals(other.field)
           && this.cache == other.cache;
  }

  public int hashCode() {
    return cache.hashCode() + field.hashCode();
  };

}
