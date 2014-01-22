Android Crash Catcher
=====================

Android Crash Catcher is simply a mechanism for catching any exception in android SDK

<img style="position: relative; width: 768px; margin: 0;" src="http://www.sibext.com/images/products/android_crach_catcher.png"/>

---

Installation
------------

    mvn install
    
    Import to Eclipse as "Existing maven projects"

    Open crash-catcher project ( if its close)

    

Usage
-----
Add to your pom.xml following:

		<dependency>
			<groupId>com.sibext.crashcatcher</groupId>
			<artifactId>crashcatcher</artifactId>
			<version>1.2.0</version>
			<type>apklib</type>
		</dependency>

Open "Propertise" from your project:
   
    go to "Android" 

    click "Add..." and chouse Crash-catcher project.

    click "Ok"
   


Add the permissions for read logs to AndroidManifest.xml file:
	
		<uses-permission android:name="android.permission.READ_LOGS" />
		<uses-permission android:name="android.permission.WRITE_INTERNAL_STORAGE" />
		<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

Add a recipient to AndroidManifest.xml file:

		<application 
			...
        	<meta-data
	            android:name="recipient_address"
	            android:value="xxxxx@yyy.zzz" />
	        ...
	      
	    </application>

Add a reporter to AndroidManifest.xml file (optional):

		<application 
			...
        	<meta-data
	            android:name="reporter"
	            android:value="com.sibext.android.activity.EmailReportActivity" />
	        ...
	      
	    </application>        

Catcher for Activities
----------------------

Add to your base activity CrashCatcherActivity as parent:

    public class BaseActivity extends CrashCatcherActivity {
    	...
    }
    
OR get instance of CrachCatcherManager and register it in your activity:
    
        public class BaseActivity extends Activity {
		private CrashCatcherManager catcherManager;
    		...
    		@Override
		protected void onCreate(Bundle savedInstanceState) {
		    catcherManager = new CrashCatcherManager();
		    catcherManager.register(this);
		    ...        
		    super.onCreate(savedInstanceState);
		}
		    
	        @Override
		protected void onDestroy() {
	            catcherManager.unRegister();
	            super.onDestroy();
		}
    	}
    
Catcher for Services
--------------------
Add to your base service CrashCatcherService as parent:

    abstract public class AbstractService extends CrashCatcherService {
    	...
    }
    
OR get instance of CrachCatcherManager and register it in your service:
    
        public class BaseService extends Service {
		private CrashCatcherManager catcherManager;
    		...
    		@Override
		protected void onCreate() {
		    catcherManager = new CrashCatcherManager();
		    catcherManager.register(this);
		    ...        
		    super.onCreate(savedInstanceState);
		}
		    
	        @Override
		protected void onDestroy() {
	            catcherManager.unRegister();
	            super.onDestroy();
		}
    	}

Add the base activity to AndroidManifest.xml file:
	
		<activity android:name="com.sibext.android.tools.CatchActivity"></activity>

Usage Dialog
-----

Your activity should be extends CrashCathcerActivity:

Use "sendDialogReport(String description)" and "sendDialogReport(int stringResourse)"method.

It is open dialog with your message (descripton).
Buttons "Yes" is send log , "No" - cancel dialog.

Profit!

