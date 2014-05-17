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
package com.bingzer.android.driven.api;

import com.bingzer.android.driven.DrivenFile;

public final class Path {

    public static final String ROOT = "/";

    public static String combine(DrivenFile drivenFile, String title){
        if(drivenFile != null)
            return combine(drivenFile.getId(), title);
        else
            return combine("", title);
    }

    public static String combine(String path1, String path2){
        String p1 = emptyIfNull(path1);
        String p2 = emptyIfNull(path2);

        if(p1.endsWith("/") || p2.startsWith("/"))
            return p1 + p2;
        else
            return p1 + "/" + p2;
    }

    public static String clean(String path){
        // make sure that path has "/"
        if(path != null && !path.startsWith("/")) return "/" + path;
        return path;
    }

    public static String clean(DrivenFile drivenFile){
        if(drivenFile != null) return clean(drivenFile.getId());
        return null;
    }

    private static String emptyIfNull(String str){
        if(str == null) return "";
        return str;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private Path(){
        // nothing
    }
}
