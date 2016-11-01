package com.yogapay.couriertsi.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapConverter {
	
			/**
			     * 将一个 JavaBean 对象转化为一个  Map
			     * @param bean 要转化的JavaBean 对象
			     * @return 转化出来的  Map 对象
			     * @throws IntrospectionException 如果分析类属性失败
			     * @throws IllegalAccessException 如果实例化 JavaBean 失败
			     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
			     */ 
			    @SuppressWarnings({ "rawtypes", "unchecked" }) 
			    public static Map convertBean(Object bean) 
			            throws IntrospectionException, IllegalAccessException, InvocationTargetException { 
			        Class type = bean.getClass(); 
			        Map returnMap = new HashMap(); 
			        BeanInfo beanInfo = Introspector.getBeanInfo(type); 
			 
			        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors(); 
			        for (int i = 0; i< propertyDescriptors.length; i++) { 
			            PropertyDescriptor descriptor = propertyDescriptors[i]; 
			            String propertyName = descriptor.getName(); 
			            if (!propertyName.equals("class")) { 
			                Method readMethod = descriptor.getReadMethod(); 
			                Object result = readMethod.invoke(bean, new Object[0]); 
			                if (result != null) { 
			                    returnMap.put(propertyName, result); 
			                } else { 
			                    returnMap.put(propertyName, ""); 
			                } 
			            } 
			        } 
			        return returnMap; 
			    } 
			    
			    
			    		/**
			    		     * 将一个 Map 对象转化为一个 JavaBean
			    		     * @param type 要转化的类型
			    		     * @param map 包含属性值的 map
			    		     * @return 转化出来的 JavaBean 对象
			    		     * @throws IntrospectionException 如果分析类属性失败
			    		     * @throws IllegalAccessException 如果实例化 JavaBean 失败
			    		     * @throws InstantiationException 如果实例化 JavaBean 失败
			    		     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
			    		     */ 
			    		    @SuppressWarnings("rawtypes") 
			    		    public static Object convertMap(Class type, Map map) 
			    		            throws IntrospectionException, IllegalAccessException, 
			    		            InstantiationException, InvocationTargetException { 
			    		        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 获取类属性 
			    		        Object obj = type.newInstance(); // 创建 JavaBean 对象 
			    		 
			    		        // 给 JavaBean 对象的属性赋值 
			    		        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors(); 
			    		        for (int i = 0; i< propertyDescriptors.length; i++) { 
			    		            PropertyDescriptor descriptor = propertyDescriptors[i]; 
			    		            String propertyName = descriptor.getName(); 
			    		          
			    		 
			    		            if (map.containsKey(propertyName)&&!StringUtil.isEmptyWithTrim(map.get(propertyName).toString())) { 
			    		                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。 
			    		                Object value = map.get(propertyName); 
			    		                if (int.class ==  descriptor.getPropertyType()||Integer.class ==  descriptor.getPropertyType()) {
			    		                	value = Integer.parseInt(map.get(propertyName).toString()) ;
										  }
			    		                if (float.class==descriptor.getPropertyType() || Float.class ==  descriptor.getPropertyType()) {
			    		            		 value = Float.valueOf(map.get(propertyName).toString()) ;	
										  }
			    		                if (double.class==descriptor.getPropertyType() ||Double.class ==  descriptor.getPropertyType()) {
			    		                	 value = Double.valueOf(map.get(propertyName).toString()) ;		
										  }
			    		                if (Date.class==descriptor.getPropertyType() ||Timestamp.class ==  descriptor.getPropertyType()) {
			    		                	 value = DateUtils.parseDate(map.get(propertyName).toString()) ;
										  }
			    		 
			    		                Object[] args = new Object[1]; 
			    		                args[0] = value; 
			    		                descriptor.getWriteMethod().invoke(obj, args); 
			    		            } 
			    		        } 
			    		        return obj; 
			    		    } 	    
	

}
