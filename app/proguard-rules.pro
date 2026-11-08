# Android default rules
-keep @androidx.annotation.Keep
-keep @androidx.lifecycle.ViewModel
-keep class androidx.lifecycle.* { *; }
-keep class * extends androidx.lifecycle.ViewModel

# Room Database
-keep class androidx.room.* { *; }
-keep class * extends androidx.room.Database { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }

# Keep all activities, services and receivers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Keep all ViewModels
-keep class com.example.betternalarm.viewmodel.* { *; }

# Keep all models
-keep class com.example.betternalarm.model.* { *; }

# Keep all DAOs
-keep class com.example.betternalarm.dao.* { *; }

# Keep all repositories
-keep class com.example.betternalarm.repository.* { *; }

# Keep all services
-keep class com.example.betternalarm.service.* { *; }

# Keep all UI components
-keep class com.example.betternalarm.ui.** { *; }

# Keep R classes
-keep class **.R$* { *; }

# Keep data binding classes
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep for parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
