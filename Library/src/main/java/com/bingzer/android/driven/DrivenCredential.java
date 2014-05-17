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
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.bingzer.android.driven.utils.IOUtils.safeClose;

/**
 * Represents a user credential used to authenticate
 * the API to the provider.
 */
public class DrivenCredential {

    private static final String TAG = "DrivenCredential";

    private Context context;
    private String accountName;
    private Token token;

    ////////////////////////////////////////////////////////////////////////////////////////////

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
        this.context = context.getApplicationContext();
        this.accountName = accountName;
        this.token = token;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void save(String name) {
        FileWriter writer = null;
        try{
            writer = new FileWriter(getCredentialFile(name));
            writer.write(toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e){
            Log.e(TAG, "Failed to save credentials to file", e);
        }
        finally {
            safeClose(writer);
        }
    }

    public boolean read(String name) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(getCredentialFile(name)));
            final StringBuilder jsonString = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                jsonString.append(line);
            }

            JSONObject jsonObject = new JSONObject(jsonString.toString());
            if(jsonObject.has("accountName")){
                accountName = jsonObject.getString("accountName");
            }

            JSONObject tokenJson;
            if(jsonObject.has("token") && (tokenJson = jsonObject.getJSONObject("token")) != null){
                token = new Token(tokenJson.getString("applicationKey"), tokenJson.getString("applicationSecret"));
                token.accessToken = tokenJson.getString("accessToken");
            }

            return true;
        }
        catch (Exception e){
            Log.e(TAG, "Failed to read credentials from a file", e);
            return false;
        }
        finally {
            safeClose(reader);
        }
    }

    public boolean hasSavedCredential(String name) {
        File file = getCredentialFile(name);
        return file != null && file.exists();
    }

    public boolean clear(String name){
        File file = getCredentialFile(name);
        return file != null && file.delete();
    }

    private File getCredentialFile(String name){
        File dir = getContext().getFilesDir();
        return new File(dir, name);
    }

    @Override
    public String toString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("accountName", accountName);
            if(token != null){
                JSONObject tokenObject = new JSONObject();
                tokenObject.put("applicationKey", token.applicationKey);
                tokenObject.put("applicationSecret", token.applicationSecret);
                tokenObject.put("accessToken", token.accessToken);
                jsonObject.put("token", tokenObject);
            }
            return jsonObject.toString();
        }
        catch (Exception e){
            return "<error parsing JSON>";
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Token provides information about the authentication.
     * Most of the providers will likely use OAuth2 authentication,
     * this class will provides a wrapper for the information needed
     * to authenticate the API
     */
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
