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
	compile 'com.github.yellowmessenger:yellowmessenger-sdk:v0.1.1'
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
        props.put("name","<Name>");
        YellowMessenger.init(this.getApplicationContext(),props);
    }

    @Override
    public void onTerminate() {
        YellowMessenger.terminate();
        super.onTerminate();
    }
}
```

Showing featured bots 
```XML
<fragment
        android:id="@+id/yellow_messenger"
        android:layout_below="@+id/textView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        class="com.yellowmessenger.sdk.fragments.DiscoverFragment"/>
```
