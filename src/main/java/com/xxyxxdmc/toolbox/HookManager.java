package main.java.com.xxyxxdmc.toolbox;

// TODO : Replace JNativeHook, use JNI to catch mouse and keyboard event.
public class HookManager {
    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.loadLibrary("WindowsHook");
        } else {
            System.loadLibrary("LinuxHook");
        }
    }

    public native void startWindowsHook();
    public native void stopWindowsHook();
    public native void startLinuxHook();

}
