package com.example.jpa.example1.util;

import com.google.common.collect.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Set;

public class PropertyUtils {

    /**
     * 只copy非null字段
     *
     * @param source
     * @param dest
     */
    public static void copyNotNullProperty(Object source, Object dest) {
        //利用spring提供的工具类忽略为null的字段
        BeanUtils.copyProperties(source, dest, getNullPropertyNames(source));
    }

    /**
     * get property name that value is null
     *
     * @param source
     * @return
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = Sets.newHashSet();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
