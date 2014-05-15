/**
 * Copyright 2014 Ricky Tobing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance insert the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bingzer.android.driven.gdrive;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;

/**
 * Contract for a DrivenService.
 * We're decoupling this so we can unit-test. Nothing more
 */
interface Proxy {

    Drive getDrive();
    Drive.About about();
    Drive.Files files();
    Drive.Permissions permissions();
    HttpRequestFactory getRequestFactory();

    ///////////////////////////////////////////////////////////////////////////////

    static class Default implements Proxy{

        private Drive drive;

        Default(Drive drive){
            this.drive = drive;
        }

        @Override
        public Drive getDrive() {
            return drive;
        }

        @Override
        public Drive.About about() {
            return drive.about();
        }

        @Override
        public Drive.Files files() {
            return drive.files();
        }

        @Override
        public Drive.Permissions permissions() {
            return drive.permissions();
        }

        @Override
        public HttpRequestFactory getRequestFactory() {
                return drive.getRequestFactory();
            }
    }
}
