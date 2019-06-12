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

import java.net.URL;
import java.net.URLClassLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author TODAY <br>
 *         2019-06-12 10:03
 */
@Slf4j
public class DefaultReloadClassLoader extends URLClassLoader {

    private final ClassLoader parent;
    private final DefaultClassResolver hotSwapResolver;

    static {
        registerAsParallelCapable();
    }

    public DefaultReloadClassLoader(URL[] urls, ClassLoader parent, DefaultClassResolver hotSwapResolver) {
        super(urls, parent);
        this.parent = parent;
        this.hotSwapResolver = hotSwapResolver;
    }

    /**
     * 全程避免使用 super.loadClass(...)，以免被 parent 加载到不该加载的类
     */
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        synchronized (getClassLoadingLock(name)) {

            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }

            if (hotSwapResolver.isHotSwapClass(name)) {
                log.trace("Hot Swap Class: [{}]", name);
                /**
                 * 使用 "本 ClassLoader" 加载类文件 注意：super.loadClass(...) 会触发 parent 加载，绝对不能使用
                 */
                c = super.findClass(name);
                if (c != null) {
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            }
            return parent.loadClass(name);
        }
    }

}
