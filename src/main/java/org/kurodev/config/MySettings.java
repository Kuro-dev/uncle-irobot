package org.kurodev.config;

import com.google.gson.annotations.Expose;
import org.kurodev.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author kuro
 **/
public class MySettings extends Properties {
    @Expose(serialize = false, deserialize = false)
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getSetting(Setting setting) {
        String response = this.getProperty(setting.getKey());
        if (response == null) {
            this.put(setting.getKey(), setting.getDefaultVal());
            logger.info("Adding missing setting to file: {} = {}", setting.getKey(), setting.getDefaultVal());
            Main.saveSettings();
            return setting.getDefaultVal();
        }
        return response;
    }

    public void restoreDefault() {
        for (Setting value : Setting.values()) {
            this.setProperty(value.getKey(), value.getDefaultVal());
        }
    }

    public boolean getSettingBool(Setting setting) {
        return Boolean.parseBoolean(getSetting(setting));
    }

    @Override
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(new TreeSet<>(super.keySet()));
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {

        Set<Map.Entry<Object, Object>> set1 = super.entrySet();
        Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<>(set1.size());

        Iterator<Map.Entry<Object, Object>> iterator = set1.stream().sorted(Comparator.comparing(o -> o.getKey().toString())).iterator();

        while (iterator.hasNext())
            set2.add(iterator.next());

        return set2;
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<>(super.keySet()));
    }
}
