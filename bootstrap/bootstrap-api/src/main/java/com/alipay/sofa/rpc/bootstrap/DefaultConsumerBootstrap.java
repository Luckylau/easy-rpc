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
 */
package com.alipay.sofa.rpc.bootstrap;

import com.alipay.sofa.rpc.client.*;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.SofaConfigs;
import com.alipay.sofa.rpc.common.SofaOptions;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.alipay.sofa.rpc.context.RpcRuntimeContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.dynamic.DynamicConfigKeys;
import com.alipay.sofa.rpc.dynamic.DynamicConfigManager;
import com.alipay.sofa.rpc.dynamic.DynamicConfigManagerFactory;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.invoke.Invoker;
import com.alipay.sofa.rpc.listener.ConfigListener;
import com.alipay.sofa.rpc.listener.ProviderInfoListener;
import com.alipay.sofa.rpc.log.LogCodes;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import com.alipay.sofa.rpc.proxy.ProxyFactory;
import com.alipay.sofa.rpc.registry.Registry;
import com.alipay.sofa.rpc.registry.RegistryFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default consumer bootstrap.
 *
 * @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a>
 */
@Extension("sofa")
public class DefaultConsumerBootstrap<T> extends ConsumerBootstrap<T> {

    /**
     * ??????????????????????????????????????????
     */
    protected final static ConcurrentMap<String, AtomicInteger> REFERRED_KEYS = new ConcurrentHashMap<String, AtomicInteger>();
    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultConsumerBootstrap.class);
    /**
     * ???????????????
     */
    protected transient volatile T proxyIns;

    /**
     * ?????????Invoker??????
     */
    protected transient volatile Invoker proxyInvoker;

    /**
     * ?????????
     */
    protected transient volatile Cluster cluster;

    /**
     * ?????????
     */
    protected transient volatile CountDownLatch respondRegistries;

    /**
     * ????????????
     *
     * @param consumerConfig ?????????????????????
     */
    protected DefaultConsumerBootstrap(ConsumerConfig<T> consumerConfig) {
        super(consumerConfig);
    }

    @Override
    public T refer() {
        if (proxyIns != null) {
            return proxyIns;
        }
        synchronized (this) {
            if (proxyIns != null) {
                return proxyIns;
            }
            String key = consumerConfig.buildKey();
            String appName = consumerConfig.getAppName();
            // ????????????
            checkParameters();
            // ?????????????????????
            if (LOGGER.isInfoEnabled(appName)) {
                LOGGER.infoWithApp(appName, "Refer consumer config : {} with bean id {}", key, consumerConfig.getId());
            }

            // ????????????interface?????????tags?????????protocol??????
            AtomicInteger cnt = REFERRED_KEYS.get(key); // ?????????
            if (cnt == null) { // ???????????????
                cnt = CommonUtils.putToConcurrentMap(REFERRED_KEYS, key, new AtomicInteger(0));
            }
            int c = cnt.incrementAndGet();
            int maxProxyCount = consumerConfig.getRepeatedReferLimit();
            if (maxProxyCount > 0) {
                if (c > maxProxyCount) {
                    cnt.decrementAndGet();
                    // ???????????????????????????????????????
                    throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_DUPLICATE_CONSUMER_CONFIG, key,
                            maxProxyCount));
                } else if (c > 1) {
                    if (LOGGER.isInfoEnabled(appName)) {
                        LOGGER.infoWithApp(appName, "Duplicate consumer config with key {} has been referred!"
                                + " Maybe it's wrong config, please check it."
                                + " Ignore this if you did that on purpose!", key);
                    }
                }
            }

            try {
                // build cluster
                cluster = ClusterFactory.getCluster(this);
                // build listeners
                consumerConfig.setConfigListener(buildConfigListener(this));
                consumerConfig.setProviderInfoListener(buildProviderInfoListener(this));
                // init cluster
                cluster.init();
                // ??????Invoker?????????????????????
                proxyInvoker = buildClientProxyInvoker(this);
                // ???????????????
                proxyIns = (T) ProxyFactory.buildProxy(consumerConfig.getProxy(), consumerConfig.getProxyClass(),
                        proxyInvoker);

                //????????????
                final String dynamicAlias = consumerConfig.getParameter(DynamicConfigKeys.DYNAMIC_ALIAS);
                if (StringUtils.isNotBlank(dynamicAlias)) {
                    final DynamicConfigManager dynamicManager = DynamicConfigManagerFactory.getDynamicManager(
                            consumerConfig.getAppName(), dynamicAlias);
                    dynamicManager.initServiceConfiguration(consumerConfig.getInterfaceId());
                }
            } catch (Exception e) {
                if (cluster != null) {
                    cluster.destroy();
                    cluster = null;
                }
                consumerConfig.setConfigListener(null);
                consumerConfig.setProviderInfoListener(null);
                cnt.decrementAndGet(); // ?????????????????????
                if (e instanceof SofaRpcRuntimeException) {
                    throw (SofaRpcRuntimeException) e;
                } else {
                    throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_BUILD_CONSUMER_PROXY), e);
                }
            }
            if (consumerConfig.getOnAvailable() != null && cluster != null) {
                cluster.checkStateChange(false); // ???????????????????????????
            }
            RpcRuntimeContext.cacheConsumerConfig(this);
            return proxyIns;
        }
    }

    /**
     * for check fields and parameters of consumer config
     */
    protected void checkParameters() {

    }

    /**
     * Build ConfigListener for consumer bootstrap.
     *
     * @param bootstrap ConsumerBootstrap
     * @return ConfigListener
     */
    protected ConfigListener buildConfigListener(ConsumerBootstrap bootstrap) {
        return new ConsumerAttributeListener();
    }

    /**
     * Build ProviderInfoListener for consumer bootstrap.
     *
     * @param bootstrap ConsumerBootstrap
     * @return ProviderInfoListener
     */
    protected ProviderInfoListener buildProviderInfoListener(ConsumerBootstrap bootstrap) {
        return new ClusterProviderInfoListener(bootstrap.getCluster());
    }

    /**
     * Build ClientProxyInvoker for consumer bootstrap.
     *
     * @param bootstrap ConsumerBootstrap
     * @return ClientProxyInvoker
     */
    protected ClientProxyInvoker buildClientProxyInvoker(ConsumerBootstrap bootstrap) {
        return new DefaultClientProxyInvoker(bootstrap);
    }

    @Override
    public void unRefer() {
        if (proxyIns == null) {
            return;
        }
        String key = consumerConfig.buildKey();
        String appName = consumerConfig.getAppName();
        if (LOGGER.isInfoEnabled(appName)) {
            LOGGER.infoWithApp(appName, "UnRefer consumer config : {} with bean id {}", key, consumerConfig.getId());
        }
        try {
            cluster.destroy();
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled(appName)) {
                LOGGER.warnWithApp(appName, "Catch exception when unrefer consumer config : " + key
                        + ", but you can ignore if it's called by JVM shutdown hook", e);
            }
        }
        // ??????????????????
        AtomicInteger cnt = REFERRED_KEYS.get(key);
        if (cnt != null && cnt.decrementAndGet() <= 0) {
            REFERRED_KEYS.remove(key);
        }
        consumerConfig.setConfigListener(null);
        consumerConfig.setProviderInfoListener(null);
        RpcRuntimeContext.invalidateConsumerConfig(this);
        proxyIns = null;

        // ???????????????????????????
        unSubscribe();
    }

    @Override
    public List<ProviderGroup> subscribe() {
        List<ProviderGroup> result = null;
        String directUrl = consumerConfig.getDirectUrl();
        if (StringUtils.isNotEmpty(directUrl)) {
            // ???????????????
            result = subscribeFromDirectUrl(directUrl);
        } else {
            // ????????????url??????
            List<RegistryConfig> registryConfigs = consumerConfig.getRegistry();
            if (CommonUtils.isNotEmpty(registryConfigs)) {
                // ???????????????????????????????????????
                result = subscribeFromRegistries();
            }
        }
        return result;
    }

    @Override
    public boolean isSubscribed() {
        return respondRegistries == null || respondRegistries.getCount() <= 0;
    }

    /**
     * Subscribe provider list from direct url
     *
     * @param directUrl direct url of consume config
     * @return Provider group list
     */
    protected List<ProviderGroup> subscribeFromDirectUrl(String directUrl) {
        List<ProviderGroup> result = new ArrayList<ProviderGroup>();
        List<ProviderInfo> tmpProviderInfoList = new ArrayList<ProviderInfo>();
        String[] providerStrs = StringUtils.splitWithCommaOrSemicolon(directUrl);
        for (String providerStr : providerStrs) {
            ProviderInfo providerInfo = convertToProviderInfo(providerStr);
            if (providerInfo.getStaticAttr(ProviderInfoAttrs.ATTR_SOURCE) == null) {
                providerInfo.setStaticAttr(ProviderInfoAttrs.ATTR_SOURCE, "direct");
            }
            tmpProviderInfoList.add(providerInfo);
        }

        result.add(new ProviderGroup(RpcConstants.ADDRESS_DIRECT_GROUP, tmpProviderInfoList));
        return result;
    }

    /**
     * Convert provider string to provider info
     *
     * @param providerStr provider url
     * @return ProviderInfo
     */
    protected ProviderInfo convertToProviderInfo(String providerStr) {
        return ProviderHelper.toProviderInfo(providerStr);
    }

    /**
     * Subscribe provider list from all registries, the providers will be merged.
     *
     * @return Provider group list
     */
    protected List<ProviderGroup> subscribeFromRegistries() {
        List<ProviderGroup> result = new ArrayList<ProviderGroup>();
        List<RegistryConfig> registryConfigs = consumerConfig.getRegistry();
        if (CommonUtils.isEmpty(registryConfigs)) {
            return result;
        }
        // ??????????????????
        int addressWaitTime = consumerConfig.getAddressWait();
        int maxAddressWaitTime = SofaConfigs.getIntegerValue(consumerConfig.getAppName(),
                SofaOptions.CONFIG_MAX_ADDRESS_WAIT_TIME, SofaOptions.MAX_ADDRESS_WAIT_TIME);
        addressWaitTime = addressWaitTime < 0 ? maxAddressWaitTime : Math.min(addressWaitTime, maxAddressWaitTime);

        ProviderInfoListener listener = consumerConfig.getProviderInfoListener();
        respondRegistries = addressWaitTime == 0 ? null : new CountDownLatch(registryConfigs.size());

        // ????????????????????? {groupName: ProviderGroup}
        Map<String, ProviderGroup> tmpProviderInfoList = new HashMap<String, ProviderGroup>();
        for (RegistryConfig registryConfig : registryConfigs) {
            Registry registry = RegistryFactory.getRegistry(registryConfig);
            registry.init();
            registry.start();

            try {
                List<ProviderGroup> current;
                try {
                    if (respondRegistries != null) {
                        consumerConfig.setProviderInfoListener(new WrapperClusterProviderInfoListener(listener,
                                respondRegistries));
                    }
                    current = registry.subscribe(consumerConfig);
                } finally {
                    if (respondRegistries != null) {
                        consumerConfig.setProviderInfoListener(listener);
                    }
                }
                if (current == null) {
                    continue; // ?????????????????????
                } else {
                    if (respondRegistries != null) {
                        respondRegistries.countDown();
                    }
                }
                for (ProviderGroup group : current) { //  ?????????????????????
                    String groupName = group.getName();
                    if (!group.isEmpty()) {
                        ProviderGroup oldGroup = tmpProviderInfoList.get(groupName);
                        if (oldGroup != null) {
                            oldGroup.addAll(group.getProviderInfos());
                        } else {
                            tmpProviderInfoList.put(groupName, group);
                        }
                    }
                }
            } catch (SofaRpcRuntimeException e) {
                throw e;
            } catch (Throwable e) {
                String appName = consumerConfig.getAppName();
                if (LOGGER.isWarnEnabled(appName)) {
                    LOGGER.warnWithApp(appName,
                            LogCodes.getLog(LogCodes.ERROR_SUBSCRIBE_FROM_REGISTRY, registryConfig.getId()), e);
                }
            }
        }
        if (respondRegistries != null) {
            try {
                respondRegistries.await(addressWaitTime, TimeUnit.MILLISECONDS);
            } catch (Exception ignore) { // NOPMD
            }
        }
        return new ArrayList<ProviderGroup>(tmpProviderInfoList.values());
    }

    /**
     * ????????????????????????
     */
    public void unSubscribe() {
        if (StringUtils.isEmpty(consumerConfig.getDirectUrl()) && consumerConfig.isSubscribe()) {
            List<RegistryConfig> registryConfigs = consumerConfig.getRegistry();
            if (registryConfigs != null) {
                for (RegistryConfig registryConfig : registryConfigs) {
                    Registry registry = RegistryFactory.getRegistry(registryConfig);
                    try {
                        registry.unSubscribe(consumerConfig);
                    } catch (Exception e) {
                        String appName = consumerConfig.getAppName();
                        if (LOGGER.isWarnEnabled(appName)) {
                            LOGGER.warnWithApp(appName,
                                    "Catch exception when unSubscribe from registry: " + registryConfig.getId()
                                            + ", but you can ignore if it's called by JVM shutdown hook", e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public T getProxyIns() {
        return proxyIns;
    }

    /**
     * ?????????????????????Invoker
     *
     * @return ???????????????Invoker proxy invoker
     */
    public Invoker getProxyInvoker() {
        return proxyInvoker;
    }

    /**
     * Wrapper provider info listener to record the respond status of registry.
     */
    class WrapperClusterProviderInfoListener implements ProviderInfoListener {

        /**
         * Origin provider info listener
         */
        private ProviderInfoListener providerInfoListener;
        /**
         * CountDownLatch of respond registries.
         */
        private CountDownLatch respondRegistries;
        /**
         * Has been respond
         */
        private AtomicBoolean hasRespond = new AtomicBoolean(false);

        public WrapperClusterProviderInfoListener(ProviderInfoListener providerInfoListener,
                                                  CountDownLatch respondRegistries) {
            this.providerInfoListener = providerInfoListener;
            this.respondRegistries = respondRegistries;
        }

        private void doCountDown() {
            if (respondRegistries != null && hasRespond.compareAndSet(false, true)) {
                respondRegistries.countDown();
                respondRegistries = null;
            }
        }

        @Override
        public void addProvider(ProviderGroup group) {
            providerInfoListener.addProvider(group);
            doCountDown();
        }

        @Override
        public void removeProvider(ProviderGroup group) {
            providerInfoListener.removeProvider(group);
        }

        @Override
        public void updateProviders(ProviderGroup group) {
            providerInfoListener.updateProviders(group);
            doCountDown();
        }

        @Override
        public void updateAllProviders(List<ProviderGroup> groups) {
            providerInfoListener.updateAllProviders(groups);
            doCountDown();
        }
    }

    /**
     * Consumer???????????????????????????
     */
    private class ConsumerAttributeListener implements ConfigListener {

        @Override
        public void configChanged(Map newValue) {

        }

        @Override
        public synchronized void attrUpdated(Map newValueMap) {
            String appName = consumerConfig.getAppName();
            // ????????? proxyIns?????????????????????cluster????????????
            // ???????????????tags cluster(loadblance) timeout, retries???
            Map<String, String> newValues = (Map<String, String>) newValueMap;
            Map<String, String> oldValues = new HashMap<String, String>();
            boolean rerefer = false;
            try { // ?????????????????????
                // ????????????map?
                for (Map.Entry<String, String> entry : newValues.entrySet()) {
                    String newValue = entry.getValue();
                    String oldValue = consumerConfig.queryAttribute(entry.getKey());
                    boolean changed = oldValue == null ? newValue != null : !oldValue.equals(newValue);
                    if (changed) { // ???????????????
                        oldValues.put(entry.getKey(), oldValue);
                    }
                    rerefer = rerefer || changed;
                }
            } catch (Exception e) {
                LOGGER.errorWithApp(appName, LogCodes.getLog(LogCodes.ERROR_CONSUMER_ATTRIBUTE_COMPARING), e);
                return;
            }
            if (rerefer) {
                try {
                    unSubscribe();// ??????????????????
                    for (Map.Entry<String, String> entry : newValues.entrySet()) { // change attrs
                        consumerConfig.updateAttribute(entry.getKey(), entry.getValue(), true);
                    }
                    // ??????????????????
                    if (LOGGER.isInfoEnabled(appName)) {
                        LOGGER.infoWithApp(appName, "Rerefer consumer {}", consumerConfig.buildKey());
                    }
                } catch (Exception e) { // ????????????????????????
                    LOGGER.errorWithApp(appName, LogCodes.getLog(LogCodes.ERROR_CONSUMER_ATTRIBUTE_CHANGE), e);
                    for (Map.Entry<String, String> entry : oldValues.entrySet()) { //rollback old attrs
                        consumerConfig.updateAttribute(entry.getKey(), entry.getValue(), true);
                    }
                    subscribe(); // ??????????????????????????????
                    return;
                }
                try {
                    switchCluster();
                } catch (Exception e) { //???????????????????????????
                    LOGGER.errorWithApp(appName, LogCodes.getLog(LogCodes.ERROR_CONSUMER_REFER_AFTER_CHANGE), e);
                    unSubscribe(); // ??????????????????
                    for (Map.Entry<String, String> entry : oldValues.entrySet()) { //rollback old attrs
                        consumerConfig.updateAttribute(entry.getKey(), entry.getValue(), true);
                    }
                    subscribe(); // ??????????????????????????????
                }
            }
        }

        /**
         * Switch cluster.
         *
         * @throws Exception the exception
         */
        private void switchCluster() throws Exception {
            Cluster newCluster = null;
            Cluster oldCluster;
            try { // ????????????
                newCluster = ClusterFactory.getCluster(DefaultConsumerBootstrap.this); //???????????? ??????????????????
                newCluster.init();
                oldCluster = ((ClientProxyInvoker) proxyInvoker).setCluster(newCluster);
            } catch (Exception e) {
                if (newCluster != null) {
                    newCluster.destroy();
                }
                if (e instanceof SofaRpcRuntimeException) {
                    throw e;
                } else {
                    throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_SWITCH_CLUSTER_NEW), e);
                }
            }
            try { // ??????
                cluster = newCluster;
                if (oldCluster != null) {
                    oldCluster.destroy(); // ????????????
                }
            } catch (Exception e) {
                String appName = consumerConfig.getAppName();
                if (LOGGER.isWarnEnabled(appName)) {
                    LOGGER.warnWithApp(appName, LogCodes.getLog(LogCodes.WARN_SWITCH_CLUSTER_DESTROY), e);
                }
            }
        }
    }
}
