package com.cliqz.jsengine;

import android.content.Context;

import com.cliqz.jsengine.v8.JSApiException;
import com.cliqz.jsengine.v8.JSConsole;
import com.cliqz.jsengine.v8.Timers;
import com.cliqz.jsengine.v8.V8Engine;
import com.cliqz.jsengine.v8.api.Crypto;
import com.cliqz.jsengine.v8.api.FileIO;
import com.cliqz.jsengine.v8.api.HttpHandler;
import com.cliqz.jsengine.v8.api.HttpRequestPolicy;
import com.cliqz.jsengine.v8.api.SystemLoader;
import com.cliqz.jsengine.v8.api.WebRequest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Engine {

    final V8Engine jsengine;
    private final Context context;
    private final Set<Object> jsApis = new HashSet<>();
    final SystemLoader system;
    private final WebRequest webRequest;

    private static final String BUILD_PATH = "build";

    public Engine(final Context context) throws JSApiException {
        this.context = context.getApplicationContext();
        jsengine = new V8Engine();
        // load js APIs

        jsApis.add(new JSConsole(jsengine));
        jsApis.add(new Timers(jsengine));
        jsApis.add(new FileIO(jsengine, this.context));
        jsApis.add(new HttpHandler(jsengine, HttpRequestPolicy.ALWAYS_ALLOWED));
        jsApis.add(new Crypto(jsengine));
        webRequest = new WebRequest(jsengine, this.context);
        system = new SystemLoader(jsengine, this.context, BUILD_PATH + "/modules");
    }

    public void startup() throws ExecutionException {
        try {
            // load config
            String config = system.readSourceFile(BUILD_PATH + "/config/cliqz.json");
            jsengine.executeScript("var __CONFIG__ = JSON.parse(\"" + config.replace("\"", "\\\"").replace("\n", "") + "\");");
            system.callFunctionOnModule("platform/startup", "default");
        } catch(IOException e) {
            throw new ExecutionException(e);
        }
    }

    public void setPref(String prefName, Object value) throws ExecutionException {
        system.callFunctionOnModuleDefault("core/utils", "setPref", prefName, value);
    }

    public Object getPref(String prefName) throws ExecutionException {
        return system.callFunctionOnModuleDefault("core/utils", "getPref");
    }

}