//  Copyright (c) 2013, Facebook, Inc.  All rights reserved.

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.hive.orc.lazy;

import java.io.IOException;

import com.facebook.hive.orc.WriterImpl;
import org.apache.hadoop.io.LongWritable;
import com.facebook.hive.orc.lazy.OrcLazyObject.ValueNotPresentException;

class LazyLongDictionaryTreeReader extends LazyNumericDictionaryTreeReader {

  private int latestIndex = 0; //< Latest index read from reader

  LazyLongDictionaryTreeReader (int columnId, long rowIndexStride) {
    super(columnId, rowIndexStride);
  }

  @Override
  protected int getNumBytes() {
    return WriterImpl.LONG_BYTE_SIZE;
  }

  private long readLong() throws IOException {
    latestIndex = (int) reader.next();
    return (long) dictionaryValues[latestIndex];
  }

  private long latestValue() {
    return (long) dictionaryValues[latestIndex];
  }

  private LongWritable createWritable(Object previous, long v) throws IOException {
    LongWritable result = null;
    if (previous == null) {
      result = new LongWritable();
    } else {
      result = (LongWritable) previous;
    }
    result.set(v);
    return result;
  }

  @Override
  public Object createWritableFromLatest(Object previous) throws IOException {
    return createWritable(previous, latestValue());
  }

  @Override
  public long nextLong(boolean readStream) throws IOException {
    if (!readStream)
      return latestValue();
    if (!valuePresent)
      throw new ValueNotPresentException("Cannot materialize long.");
    return readLong();
  }

  @Override
  public Object next(Object previous) throws IOException {
    LongWritable result = null;
    if (valuePresent) {
      result = createWritable(previous, readLong());
    }
    return result;
  }
}
