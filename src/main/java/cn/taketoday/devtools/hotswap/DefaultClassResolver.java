/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.devtools.hotswap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author TODAY <br>
 *         2019-06-12 10:03
 */
public class DefaultClassResolver {

    private final String[] classPathDirs;

    private String[] hotSwapClassPrefix = { //

    };
    private boolean scanAllDir = true;

    public DefaultClassResolver() {
        this.classPathDirs = buildClassPathDirs();
    }

    private static String[] buildClassPathDirs() {
        List<String> list = new ArrayList<>();
        String[] classPathArray = System.getProperty("java.class.path").split(File.pathSeparator);
        for (String classPath : classPathArray) {
            classPath = classPath.trim();

            if (classPath.startsWith("./")) {
                classPath = classPath.substring(2);
            }

            File file = new File(classPath);
            if (file.exists() && file.isDirectory()) {
                // if (!classPath.endsWith("/") && !classPath.endsWith("\\")) {
                if (!classPath.endsWith(File.separator)) {
                    classPath = classPath + File.separator; // append postfix char "/"
                }

                list.add(classPath);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 判断是否为热加载类文件，热加载类文件无条件使用 HotSwapClassLoader 加载
     * 
     * 热加载类文件满足两个条件： 1：通过 hotSwapClassPrefix 指定的类文件 2：在 class path 目录下能找到的 .class 文件
     */
    public boolean isHotSwapClass(String className) {

        for (String s : hotSwapClassPrefix) {
            if (className.startsWith(s)) {
                return true;
            }
        }

        if (isScanAllDir()) {
            return findClassInClassPathDirs(className);
        }

        return false;
    }

    protected boolean findClassInClassPathDirs(String className) {

        String fileName = className.replace('.', '/').concat(".class");

        if (classPathDirs.length == 1) {
            if (findFile(classPathDirs[0], fileName)) {
                return true;
            }
        }
        else {
            for (String dir : classPathDirs) {
                if (findFile(dir, fileName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getPath(String className) {
        String fileName = className.replace('.', '/').concat(".class");
        for (String dir : classPathDirs) {
            File file = new File(dir, fileName);
            if (file.exists()) {
//				return file.getAbsolutePath();
                return file.getName();
            }
        }
        return null;
    }

    protected boolean findFile(String filePath, String fileName) {
        return new File(filePath, fileName).isFile();
    }

    public synchronized void addHotSwapClassPrefix(String... prefix) {

        List<String> list = new ArrayList<>();

        list.addAll(Arrays.asList(prefix));
        list.addAll(Arrays.asList(hotSwapClassPrefix));

        hotSwapClassPrefix = list.toArray(new String[list.size()]);
    }

    public boolean isScanAllDir() {
        return scanAllDir;
    }

    public void setScanAllDir(boolean scanAllDir) {
        this.scanAllDir = scanAllDir;
    }
}
