package core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射对象.
 *
 */
public class ReflectUtil {
	
	/**
	 * 反射得到类字段值.
	 *
	 * @param name the name
	 * @param obj the obj
	 * @return the value
	 */
public static Object getValue(String name,Object obj)
{
	Class<?> cl=obj.getClass();
	try {
		Method method= cl.getMethod("get"+StringUtile.upperInitial(name));
	    return	method.invoke(obj);
	} catch (Exception e) {
		return "";
	} 
}

/**
 * Gets the field.
 *
 * @param name the name
 * @param obj the obj
 * @return the field
 */
public static Field getField(String name,Object obj)
{
	Class<?> cl=obj.getClass();
	Field field=null;
	try {
		field= cl.getDeclaredField(name);
		field.setAccessible(true);
	} catch (SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchFieldException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return field;
}

/**
 * Gets the fields.
 *
 * @param obj the obj
 * @return the fields
 */
public static Map<String,Field> getFields(Class<?> obj)
{
	Map<String,Field> map=new HashMap<String, Field>();
    Field[] fields= obj.getDeclaredFields();  
    for (Field field : fields) {
    	field.setAccessible(true);
		map.put(field.getName(),field);
	}
    if(obj.getSuperclass()!=Object.class)
    {
    	System.out.println(obj.getName());
       map.putAll(getFields(obj.getSuperclass()));
    }
    return map;
}

/**
 * 实体转化.
 *
 * @param <T> the generic type
 * @param name the name
 * @param t the t
 * @param obj the obj
 * @return the t
 */
public static <T> T ConverObjec(String name,T t,Object obj)
{
	Class<?> cl= t.getClass();
	try {
		Method method=cl.getMethod("set"+StringUtile.upperInitial(name));
	    method.invoke(t,obj);
	    return t;
	} catch (Exception e) {
		e.printStackTrace();
	} 
	return t;
}
}