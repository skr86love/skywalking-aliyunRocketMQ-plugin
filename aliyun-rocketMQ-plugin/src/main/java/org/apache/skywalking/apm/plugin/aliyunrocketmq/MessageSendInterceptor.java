package org.apache.skywalking.apm.plugin.aliyunrocketmq;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageDecoder;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.protocol.header.SendMessageRequestHeader;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.aliyunrocketmq.define.SendCallBackEnhanceInfo;
import org.apache.skywalking.apm.util.StringUtil;

import java.lang.reflect.Method;

/**
 * @author : yangzhonghao.
 * @description :
 */
public class MessageSendInterceptor implements InstanceMethodsAroundInterceptor {

    public static final String ASYNC_SEND_OPERATION_NAME_PREFIX = "AliyunRocketMQ/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
//        System.out.println("[producer:]进来了");
        Message message = (Message) allArguments[2];
        ContextCarrier contextCarrier = new ContextCarrier();
        String namingServiceAddress = String.valueOf(objInst.getSkyWalkingDynamicField());
        AbstractSpan span = ContextManager.createExitSpan(buildOperationName(message.getTopic(), message.getTags()), contextCarrier, namingServiceAddress);
        span.setComponent(ComponentsDefine.ALIYUN_ROCKET_MQ_PRODUCER);
        Tags.MQ_BROKER.set(span, (String) allArguments[0]);
        Tags.MQ_TOPIC.set(span, message.getTopic());
        SpanLayer.asMQ(span);
//        System.out.println("[producer:运行到42行]");

        SendMessageRequestHeader requestHeader = (SendMessageRequestHeader) allArguments[3];
        StringBuilder properties = new StringBuilder(requestHeader.getProperties());
        CarrierItem next = contextCarrier.items();
//        System.out.println("[producer:运行到46行]");
        while (next.hasNext()) {
            next = next.next();
            if (!StringUtil.isEmpty(next.getHeadValue())) {
                properties.append(next.getHeadKey());
                properties.append(MessageDecoder.NAME_VALUE_SEPARATOR);
                properties.append(next.getHeadValue());
                properties.append(MessageDecoder.PROPERTY_SEPARATOR);
            }
        }
        requestHeader.setProperties(properties.toString());
//        System.out.println("[producer:]" + properties.toString());

//        for (Object arg : allArguments) {
//            System.out.print(arg.getClass().getName() + ", ");
//        }
//        System.out.println("\n[producer:]运行到这里0," + method.getName() + ",args:" + allArguments[6]);
        if (allArguments[6] != null) {
//            System.out.println("[producer:]运行到这里1," + allArguments[6]);
            ((EnhancedInstance)allArguments[6]).setSkyWalkingDynamicField(new SendCallBackEnhanceInfo(message.getTopic(), message.getTags(), ContextManager.capture()));
//            System.out.println("[producer:]运行到这里2");
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }

    private String buildOperationName(String topicName, String tag) {
        return ASYNC_SEND_OPERATION_NAME_PREFIX + topicName + "/" + tag + "/Producer";
    }
}
