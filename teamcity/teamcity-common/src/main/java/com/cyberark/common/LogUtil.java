package com.cyberark.common;
// import com.intellij.openapi.diagnostic.Logger;

public class LogUtil {
    private final boolean verbose;
    public LogUtil(boolean verbose){
        this.verbose = verbose;
    }

    private String getClassName(Object object) {
        return object.getClass().getName();
    }

//    public void write(Object object, String message){
//        Logger.getInstance(getClassName(object)).info(message);
//    }
//
//    public void writeVerbose(Object object, String message){
//        if (this.verbose){
//            Logger.getInstance(getClassName(object)).debug(message);
//        }
//    }
//
//    public void writeError(Object object, String message){
//        Logger.getInstance(getClassName(object)).error(message);
//    }
//
//    public void writeWarn(Object object, String message){
//        Logger.getInstance(getClassName(object)).warn(message);
//    }
}
