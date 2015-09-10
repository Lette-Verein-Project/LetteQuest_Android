/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mia.lette.com.museum.zxing.client.result;

import mia.lette.com.museum.zxing.Result;

/**
 * <p>Abstract class representing the result of decoding a barcode, as more than
 * a String -- as some type of structured data. This might be a subclass which represents
 * a URL, or an e-mail address. {@link #parseResult(Result)} will turn a raw
 * decoded string into the most appropriate type of structured representation.</p>
 *
 * <p>Thanks to Jeff Griffin for proposing rewrite of these classes that relies less
 * on exception-based mechanisms during parsing.</p>
 *
 * @author Sean Owen
 */
public abstract class ResultParser {

  /**
   * Attempts to parse the raw {@link Result}'s contents as a particular type
   * of information (email, URL, etc.) and return a {@link ParsedResult} encapsulating
   * the result of parsing.
   */
  public abstract ParsedResult parse(Result theResult);

  public static ParsedResult parseResult(Result theResult) {
    return new TextParsedResult(theResult.getText(), null);
  }
}
