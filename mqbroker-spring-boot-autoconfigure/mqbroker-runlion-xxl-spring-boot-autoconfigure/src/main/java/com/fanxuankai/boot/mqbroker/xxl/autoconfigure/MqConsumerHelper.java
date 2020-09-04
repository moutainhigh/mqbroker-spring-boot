package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.fanxuankai.boot.mqbroker.model.ListenerMetadata;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.springframework.util.StringUtils;

/**
 * @author fanxuankai
 */
public class MqConsumerHelper {

    /**
     * 动态生成 class, 并且加上 @MqConsumer 注解
     *
     * @param listenerMetadata the listener metadata
     * @param template         the mq consumer template class
     * @return new class
     */
    public static Class<?> newClass(ListenerMetadata listenerMetadata, Class<?> template) {
        try {
            ClassPool pool = ClassPool.getDefault();
            String templateClassname = template.getName();
            pool.insertClassPath(new ClassClassPath(template));
            String topic = listenerMetadata.getTopic();
            CtClass clazz = pool.makeClass(templateClassname + "@" + topic,
                    pool.getCtClass(templateClassname));
            ClassFile classFile = clazz.getClassFile();
            ConstPool constPool = classFile.getConstPool();
            Annotation classAnnotation = new Annotation(MqConsumer.class.getName(), constPool);
            String group = listenerMetadata.getGroup();
            if (StringUtils.hasText(group)) {
                classAnnotation.addMemberValue("group", new StringMemberValue(group, constPool));
            } else {
                classAnnotation.addMemberValue("group", new StringMemberValue("default", constPool));
            }
            classAnnotation.addMemberValue("topic", new StringMemberValue(topic, constPool));
            classAnnotation.addMemberValue("waitRateSeconds", new IntegerMemberValue(constPool,
                    listenerMetadata.getWaitRateSeconds()));
            classAnnotation.addMemberValue("waitMaxSeconds", new IntegerMemberValue(constPool,
                    listenerMetadata.getWaitMaxSeconds()));
            AnnotationsAttribute classAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            classAttribute.addAnnotation(classAnnotation);
            clazz.getClassFile().addAttribute(classAttribute);
            return clazz.toClass();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}