/*
 * Copyright (C) 2013 Infobank corporation
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

package net.ib.ota;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

import net.ib.ota.util.Log;

/**
 * Created by ohjongin on 13. 7. 8.
 */
public class IbOtaApplication extends Application {
    private static final String APPLICATION_ID = ""; // APPLICATION_ID VALUE FROM PARSE.COM
    private static final String CLIENT_KEY = ""; // CLIENT_KEY VALUE FORM PARSE.COM

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);


        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        Log.setLogTag("IbOta");
        Log.setDebugMode(true);
    }

}