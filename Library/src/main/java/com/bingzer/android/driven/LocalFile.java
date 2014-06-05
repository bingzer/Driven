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

import java.io.File;

/**
 * Represents a local file
 */
public class LocalFile {

    private File file;
    private String type;
    private String name;

    /**
     * Creates an instance of {@linkplain com.bingzer.android.driven.LocalFile}
     * Because this constructor didn't specify the MIME type, {@link #getType()}
     * will return null
     * @param file the File (type of {@link java.io.File})
     */
    public LocalFile(File file){
        this(file, null);
    }

    /**
     * Instantiate an instance of {@linkplain com.bingzer.android.driven.LocalFile}
     * @param type the MIME type of this file.
     * @param file the File (type of {@link java.io.File})
     */
    public LocalFile(File file, String type){
        this(file, type, null);
    }

    /**
     * Creates an instance of {@linkplain com.bingzer.android.driven.LocalFile}
     * with specific type and name
     * @param file the File (type of {@link java.io.File})
     * @param type the MIME type of this file.
     * @param name the custom name. If {@code name is null}, name will be taken from
     *             {@link java.io.File#getName()}
     */
    public LocalFile(File file, String type, String name){
        this.file = file;
        this.type = type;
        this.name = name;
        if(name == null)
            this.name = file.getName();
    }

    /**
     * Returns the local file
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets local file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the MIME type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the MIME type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the name of this local file
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of this local file
     */
    public void setName(String name) {
        if(name == null)
            name = file.getName();
        this.name = name;
    }
}
