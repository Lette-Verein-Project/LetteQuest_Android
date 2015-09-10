/*
 * Copyright (C) 2010 ZXing authors
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

package mia.lette.com.museum.zxing.client.android.book;

import android.os.Handler;
import android.os.Message;

import mia.lette.com.museum.zxing.client.android.LocaleManager;
import mia.lette.com.museum.zxing.client.android.R;


final class NetworkWorker implements Runnable {

  private final String isbn;
  private final String query;
  private final Handler handler;

  NetworkWorker(String isbn, String query, Handler handler) {
    this.isbn = isbn;
    this.query = query;
    this.handler = handler;
  }

  @Override
  public void run() {
      String uri;
      if (LocaleManager.isBookSearchUrl(isbn)) {
        int equals = isbn.indexOf('=');
        String volumeId = isbn.substring(equals + 1);
        uri = "http://www.google.com/books?id=" + volumeId + "&jscmd=SearchWithinVolume2&q=" + query;
      } else {
        uri = "http://www.google.com/books?vid=isbn" + isbn + "&jscmd=SearchWithinVolume2&q=" + query;
      }

        Message message = Message.obtain(handler, R.id.search_book_contents_succeeded);
        message.sendToTarget();
      message.sendToTarget();
  }

}
