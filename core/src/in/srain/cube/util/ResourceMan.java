package in.srain.cube.util;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ResourceMan {

    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static HashMap<Integer, String> getMap(Class<?> c) {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        try {
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                int key = field.getInt(field);
                String name = field.getName();
                map.put(key, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
