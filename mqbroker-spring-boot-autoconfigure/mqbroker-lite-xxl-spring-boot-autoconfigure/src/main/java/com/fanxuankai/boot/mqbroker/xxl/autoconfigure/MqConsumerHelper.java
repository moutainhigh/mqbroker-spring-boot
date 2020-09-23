package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.xxl.mq.client.consumer.annotation.MqConsumer;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * @author fanxuankai
 */
public class MqConsumerHelper {

    /**
     * 动态生成 class, 并且加上 @MqConsumer 注解
     *
     * @param name     the name
     * @param group    the group
     * @param topic    the topic
     * @param template the mq consumer template class
     * @return new class
     */
    public static Class<?> newClass(String name, @Nullable String group, String topic, Class<?> template) {
        try {
            ClassPool pool = ClassPool.getDefault();
            String templateClassname = template.getName();
            pool.insertClassPath(new ClassClassPath(template));
            CtClass clazz = pool.makeClass(templateClassname + "@" + topic, pool.getCtClass(templateClassname));
            ClassFile classFile = clazz.getClassFile();
            ConstPool constPool = classFile.getConstPool();
            Annotation classAnnotation = new Annotation(MqConsumer.class.getName(), constPool);
            if (StringUtils.hasText(group)) {
                classAnnotation.addMemberValue("group", new StringMemberValue(group, constPool));
            } else {
                classAnnotation.addMemberValue("group", new StringMemberValue(MqConsumer.DEFAULT_GROUP, constPool));
            }
            classAnnotation.addMemberValue("name", new StringMemberValue(name, constPool));
            classAnnotation.addMemberValue("topic", new StringMemberValue(topic, constPool));
            AnnotationsAttribute classAttribute = new AnnotationsAttribute(constPool,
                    AnnotationsAttribute.visibleTag);
            classAttribute.addAnnotation(classAnnotation);
            clazz.getClassFile().addAttribute(classAttribute);
            return clazz.toClass();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}