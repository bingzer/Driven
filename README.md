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
driven.listAsync(new Task<List<DrivenFile>>(){
  public void onComplete(List<DrivenFile> files){
    ...
  }
});
```
Same interface, same method calls!
 
Download snapshots
==================
**IMPORTANT** 
This library is still under heavy development and yet scheduled for v.1 release.

```groovy

repositories {
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots/"
    }
}

dependencies {
    compile (group:'com.bingzer.android.dbv', name: 'dbquery', version:'0.3.0-SNAPSHOT', changing: true)
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
