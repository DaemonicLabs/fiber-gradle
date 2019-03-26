package fiber;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ExtractInfo {
    public static void main(String[] args) {
        for(String clazzName : args) {
            try {
                System.out.println(clazzName);
                Class<?> objectClass = Class.forName(clazzName);

                Object instance = objectClass.newInstance();
                Field[] fields = objectClass.getDeclaredFields();
                for(Field field : fields) {
                    Class<?> clazz = field.getType();
                    field.setAccessible(true);
                    if(field.isAccessible()) {
                        Object value = field.get(instance);
                        if(clazz.isArray()) {
                            String stringValue;
                            Class<?> componentType = clazz.getComponentType();
                            if(componentType.isPrimitive()) {
                                if(componentType == Boolean.TYPE) {
                                    stringValue = Arrays.toString((boolean[]) value);
                                } else if(componentType == Byte.TYPE) {
                                    stringValue = Arrays.toString((byte[]) value);
                                } else if (componentType == Short.TYPE) {
                                    stringValue = Arrays.toString((short[]) value);
                                } else if (componentType == Integer.TYPE) {
                                    stringValue = Arrays.toString((int[]) value);
                                } else if (componentType == Long.TYPE) {
                                    stringValue = Arrays.toString((long[]) value);
                                } else if (componentType == Float.TYPE) {
                                    stringValue = Arrays.toString((float[]) value);
                                } else if (componentType == Double.TYPE) {
                                    stringValue = Arrays.toString((double[]) value);
                                } else if (componentType == Character.TYPE) {
                                    stringValue = Arrays.toString((char[]) value);
                                } else {
                                    stringValue = "some other primitive ?";
                                }
                            }else {
                                stringValue = Arrays.toString((Object[]) value);
                            }
                            System.out.printf("%s=%s%n", field.getName(), stringValue);
                        } else if(clazz.isEnum()) {
                            System.out.printf("%s=%s%n", field.getName(), value);
                        } else {
                            System.out.printf("%s=%s%n", field.getName(), value);
                        }
                    } else {
                        System.err.printf("field %s is inaccessible%n", field.getName());
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
