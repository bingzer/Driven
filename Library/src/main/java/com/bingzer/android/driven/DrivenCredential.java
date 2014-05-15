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
package com.bingzer.android.driven;

import android.content.Context;

public class DrivenCredential {

    private Context context;
    private String accountName;
    private Token token;

    public DrivenCredential(Context context){
        this(context, (String) null);
    }

    public DrivenCredential(Context context, String accountName){
        this(context, accountName, null);
    }

    public DrivenCredential(Context context, Token token){
        this(context, null, token);
    }

    public DrivenCredential(Context context, String accountName, Token token){
        this.context = context;
        this.accountName = accountName;
        this.token = token;
    }

    public Context getContext() {
        return context;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static class Token {
        private String applicationKey;
        private String applicationSecret;
        private String accessToken;

        public Token(String applicationKey, String applicationSecret){
            this.applicationKey = applicationKey;
            this.applicationSecret = applicationSecret;
        }

        public String getApplicationKey() {
            return applicationKey;
        }

        public void setApplicationKey(String applicationKey) {
            this.applicationKey = applicationKey;
        }

        public String getApplicationSecret() {
            return applicationSecret;
        }

        public void setApplicationSecret(String applicationSecret) {
            this.applicationSecret = applicationSecret;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
