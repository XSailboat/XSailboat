/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package team.sailboat.commons.fan.collection;

import java.util.Collection;

/**
 * 来自Apache Commons Collections 4.1
 * 
 * Defines a collection that is bounded in size.
 * <p>
 * The size of the collection can vary, but it can never exceed a preset
 * maximum number of elements. This interface allows the querying of details
 * associated with the maximum number of elements.
 *
 * @see CollectionUtils#isFull
 * @see CollectionUtils#maxSize
 *
 * @since 3.0
 * @version $Id: BoundedCollection.java 1477779 2013-04-30 18:55:24Z tn $
 */
public interface BoundedCollection<E> extends Collection<E> {

    /**
     * Returns true if this collection is full and no new elements can be added.
     *
     * @return <code>true</code> if the collection is full
     */
    boolean isFull();

    /**
     * Gets the maximum size of the collection (the bound).
     *
     * @return the maximum number of elements the collection can hold
     */
    int maxSize();

}
