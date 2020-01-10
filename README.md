# skywalking-aliyunRocketMQ-plugin

提供追踪 aliyunRocketMQ 能力的 skywalking 插件, skywalking 版本 6.6.0.

使用方法  

1. 将整个plugin文件夹放入 /apm-sniffer/apm-sdk-plugin 目录  

2. 以module的形式导入项目 

3. 在/apm-protocol/apm-network/org.apache.skywalking.apm.network.trace.component.ComponentsDefine
加入常量      

    public static final OfficialComponent ALIYUN_ROCKET_MQ_PRODUCER = new OfficialComponent(81, "aliyun-rocketMQ-producer");  
    public static final OfficialComponent ALIYUN_ROCKET_MQ_CONSUMER = new OfficialComponent(82, "aliyun-rocketMQ-consumer");

3. make build.agent  
