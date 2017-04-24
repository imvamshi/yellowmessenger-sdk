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
	compile 'com.github.yellowmessenger:yellowmessenger-sdk:v0.2.13'
}
```

Android Application class
```java
import com.yellowmessenger.sdk.service.YellowMessenger;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HashMap<String,String> props = new HashMap<>();
        props.put("account","<Account provided by yellowmessenger>");
        props.put("country","IN");
        props.put("name","<Name>");
        props.put("authorizationToken","<Authorization Token Provided by Yellow Messenger>");
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

Add this for getting a chat button for your app
```XML
<com.yellowmessenger.sdk.views.ChatButton
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        app:backgroundTint="@color/primary_color"
        android:src="@drawable/ic_home_chat" />
```
