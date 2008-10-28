package org.gridlab.gat.engine;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexFile;

public class ApkClassLoader extends ClassLoader {

    private DexFile dexFile;

    public ApkClassLoader(File apkFile, ClassLoader parent) throws IOException {
        super(parent);
        this.dexFile = new DexFile(apkFile);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> result;
        try {
            result = getParent().loadClass(className);
        } catch (ClassNotFoundException e) {
            result = null;
        }
        if (result == null) {
            result = dexFile.loadClass(className.replace(".", "/"), this);
        }
        return result;
    }
}
