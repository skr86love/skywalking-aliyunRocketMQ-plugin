/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.apache.skywalking.apm.plugin.aliyunrocketmq;

import com.aliyun.openservices.ons.api.OnExceptionContext;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.aliyunrocketmq.define.SendCallBackEnhanceInfo;

import java.lang.reflect.Method;

/**
 * @author yangzhonghao
 */
public class OnExceptionInterceptor implements InstanceMethodsAroundInterceptor {

    public static final String CALLBACK_OPERATION_NAME_PREFIX = "AliyunRocketMQ/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        SendCallBackEnhanceInfo enhanceInfo = (SendCallBackEnhanceInfo) objInst.getSkyWalkingDynamicField();
        AbstractSpan activeSpan = ContextManager.createLocalSpan(CALLBACK_OPERATION_NAME_PREFIX + enhanceInfo.getTopicId()
                + "/" + enhanceInfo.getTags() + "/Producer/Callback");
        activeSpan.setComponent(ComponentsDefine.ROCKET_MQ_PRODUCER);
        activeSpan.errorOccurred().log((Throwable) allArguments[0]);
        ContextManager.continued(enhanceInfo.getContextSnapshot());
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
        ContextManager.activeSpan().log(t);
    }
}
