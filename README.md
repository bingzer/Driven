[![Build Status](https://travis-ci.org/bingzer/Driven.svg?branch=master)](https://travis-ci.org/bingzer/Driven)

Driven
======
`Driven` is an effort to unify API calls for different cloud storage providers (i.e: Google Drive, Dropbox, - many more to come). Depending on the providers, the remote storage have different file system and the concept of `Remote File` is different as well. However, there are lots of similarities.

At first Driven was used for GoogleDrive API calls but as requirements grow other providers will be implemented as well.

Sample Code:
===========

Listing files via GoogleDrive:
``` java
StorageProvider provider = new GoogleDrive();
provider.authenticate(credentials);

// list all files in the root
provider.listAsync(new Task<List<DrivenFile>>(){
  public void onComplete(List<DrivenFile> files){
    ...
  }
});

```

Listing files via Dropbox:
``` java
StorageProvider provider = new Dropbox();
provider.authenticate(credentials);

// list all files in the root
provider.listAsync(new Task<List<DrivenFile>>(){
  public void onComplete(List<DrivenFile> files){
    ...
  }
});
```
Same interface, same method calls!
 
Download
==================

```groovy

dependencies {  
    // main library
    compile 'com.bingzer.android.driven:driven:1.0.0'
    
    // Google Drive
    compile 'com.bingzer.android.driven:driven-gdrive:1.0.0'
    
    // Dropbox
    compile 'com.bingzer.android.driven:driven-dropbox:1.0.0'
    
    // External Drive
    compile 'com.bingzer.android.driven:driven-ext-drive:1.0.0'
}

```

LICENSE
=======
``` java
Copyright 2014 Ricky Tobing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```
