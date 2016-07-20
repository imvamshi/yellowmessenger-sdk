Yellow Messenger SDK
=======================

### Configuration

Root level gradle file
```gradle
allprojects {
    repositories {
        jcenter()
        // Add these two lines 
        maven { url "https://jitpack.io" }
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    }
}
```
App level gradle file
```gradle
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    // Add this line 
	compile 'com.github.yellowmessenger:yellowmessenger-sdk:04d9ad13ed'
}
```

Android Application class
```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HashMap<String,String> props = new HashMap<>();
        props.put("account","<Account provided by yellowmessenger>");
        props.put("country","IN");
        YellowMessenger.init(this.getApplicationContext(),props);
    }

    @Override
    public void onTerminate() {
        YellowMessenger.terminate();
        super.onTerminate();
    }
}
```

