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
package com.alipay.sofa.rpc.client;

import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.common.RpcConfigs;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.RpcOptions;
import com.alipay.sofa.rpc.common.struct.ConcurrentHashSet;
import com.alipay.sofa.rpc.common.struct.ListDifference;
import com.alipay.sofa.rpc.common.struct.NamedThreadFactory;
import com.alipay.sofa.rpc.common.struct.ScheduledService;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.ExceptionUtils;
import com.alipay.sofa.rpc.common.utils.NetUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.context.AsyncRuntime;
import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.listener.ConsumerStateListener;
import com.alipay.sofa.rpc.log.LogCodes;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import com.alipay.sofa.rpc.transport.ClientTransport;
import com.alipay.sofa.rpc.transport.ClientTransportConfig;
import com.alipay.sofa.rpc.transport.ClientTransportFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ?????????????????????????????????????????????
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
@Extension("all")
public class AllConnectConnectionHolder extends ConnectionHolder {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AllConnectConnectionHolder.class);

    /**
     * ?????????????????????
     */
    protected ConsumerConfig consumerConfig;

    /**
     * switch
     */
    protected boolean connectionValidate = RpcConfigs
            .getBooleanValue(RpcOptions.CONNNECTION_VALIDATE_SLEEP);

    /***
     * ????????????????????????????????????????????????
     */
    protected boolean createConnWhenAbsent = RpcConfigs
            .getBooleanValue(RpcOptions.RPC_CREATE_CONN_WHEN_ABSENT);
    /**
     * ??????????????????????????????????????????lazy=true???
     */
    protected ConcurrentMap<ProviderInfo, ClientTransport> uninitializedConnections = new ConcurrentHashMap<ProviderInfo, ClientTransport>();
    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    protected ConcurrentMap<ProviderInfo, ClientTransport> aliveConnections = new ConcurrentHashMap<ProviderInfo, ClientTransport>();
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    protected ConcurrentMap<ProviderInfo, ClientTransport> subHealthConnections = new ConcurrentHashMap<ProviderInfo, ClientTransport>();
    /**
     * ?????????????????????????????????????????????????????????
     */
    protected ConcurrentMap<ProviderInfo, ClientTransport> retryConnections = new ConcurrentHashMap<ProviderInfo, ClientTransport>();
    /**
     * last address for registry pushed
     */
    protected Set<ProviderInfo> lastAddresses = new HashSet<ProviderInfo>();
    /**
     * ???????????????provider??????
     */
    protected Lock providerLock = new ReentrantLock();
    /**
     * ????????????
     */
    private volatile ScheduledService reconThread;
    /**
     * ?????????????????????????????????Provider????????????
     */
    private AtomicInteger reconnectFlag = new AtomicInteger();

    /**
     * ????????????
     *
     * @param consumerBootstrap ?????????????????????
     */
    protected AllConnectConnectionHolder(ConsumerBootstrap consumerBootstrap) {
        super(consumerBootstrap);
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    /**
     * Gets retry connections.
     *
     * @return the retry connections
     */
    public ConcurrentMap<ProviderInfo, ClientTransport> getRetryConnections() {
        return retryConnections;
    }

    /**
     * Add alive.
     *
     * @param providerInfo the provider
     * @param transport    the transport
     */
    protected void addAlive(ProviderInfo providerInfo, ClientTransport transport) {
        if (checkState(providerInfo, transport)) {
            aliveConnections.put(providerInfo, transport);
        }
    }

    /**
     * Add retry.
     *
     * @param providerInfo the provider
     * @param transport    the transport
     */
    protected void addRetry(ProviderInfo providerInfo, ClientTransport transport) {
        retryConnections.put(providerInfo, transport);
    }

    /**
     * ???????????????????????????
     *
     * @param providerInfo Provider
     * @param transport    ??????
     */
    protected void aliveToRetry(ProviderInfo providerInfo, ClientTransport transport) {
        providerLock.lock();
        try {
            if (aliveConnections.remove(providerInfo) != null) {
                retryConnections.put(providerInfo, transport);
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ???????????????????????????
     *
     * @param providerInfo Provider
     * @param transport    ??????
     */
    protected void retryToAlive(ProviderInfo providerInfo, ClientTransport transport) {
        providerLock.lock();
        try {
            if (retryConnections.remove(providerInfo) != null) {
                if (checkState(providerInfo, transport)) {
                    aliveConnections.put(providerInfo, transport);
                }
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ????????????????????????
     *
     * @param providerInfo    ?????????????????????
     * @param clientTransport ??????????????????
     * @return ??????????????????
     */
    protected boolean checkState(ProviderInfo providerInfo, ClientTransport clientTransport) {
        //        Protocol protocol = ProtocolFactory.getProtocol(providerInfo.getProtocolType());
        //        ProtocolNegotiator negotiator = protocol.negotiator();
        //        if (negotiator != null) {
        //            return negotiator.handshake(providerInfo, clientTransport);
        //        } else {
        return true;
        //        }
    }

    /**
     * ??????????????????????????????
     *
     * @param providerInfo Provider
     * @param transport    ??????
     */
    protected void aliveToSubHealth(ProviderInfo providerInfo, ClientTransport transport) {
        providerLock.lock();
        try {
            if (aliveConnections.remove(providerInfo) != null) {
                subHealthConnections.put(providerInfo, transport);
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param providerInfo Provider
     * @param transport    ??????
     */
    protected void subHealthToAlive(ProviderInfo providerInfo, ClientTransport transport) {
        providerLock.lock();
        try {
            if (subHealthConnections.remove(providerInfo) != null) {
                if (checkState(providerInfo, transport)) {
                    aliveConnections.put(providerInfo, transport);
                }
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param providerInfo Provider
     * @param transport    ??????
     */
    protected void subHealthToRetry(ProviderInfo providerInfo, ClientTransport transport) {
        providerLock.lock();
        try {
            if (subHealthConnections.remove(providerInfo) != null) {
                retryConnections.put(providerInfo, transport);
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ??????provider
     *
     * @param providerInfo the provider
     * @return ???????????????????????? ?????????ClientTransport
     */
    protected ClientTransport remove(ProviderInfo providerInfo) {
        providerLock.lock();
        try {
            ClientTransport transport = uninitializedConnections.remove(providerInfo);
            if (transport == null) {
                transport = aliveConnections.remove(providerInfo);
                if (transport == null) {
                    transport = subHealthConnections.remove(providerInfo);
                    if (transport == null) {
                        transport = retryConnections.remove(providerInfo);
                    }
                }
            }
            return transport;
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ???????????????????????????,????????????<br>
     * 1.??????????????????????????????????????????????????????<br>
     * 2.????????????????????????+???????????????????????????????????????
     */
    public void notifyStateChangeToUnavailable() {
        final List<ConsumerStateListener> onAvailable = consumerConfig.getOnAvailable();
        if (onAvailable != null) {
            AsyncRuntime.getAsyncThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    // ???????????????????????????
                    final Object proxyIns = consumerConfig.getConsumerBootstrap().getProxyIns();
                    for (ConsumerStateListener listener : onAvailable) {
                        try {
                            listener.onUnavailable(proxyIns);
                        } catch (Exception e) {
                            LOGGER.errorWithApp(consumerConfig.getAppName(),
                                    LogCodes.getLog(LogCodes.ERROR_NOTIFY_CONSUMER_STATE_UN, proxyIns.getClass().getName()));
                        }
                    }
                }
            });
        }
    }

    /**
     * ????????????????????????,????????????<br>
     * 1.???????????????????????????<br>
     * 2.???????????????????????????????????????????????????<br>
     * 3.???????????????????????????????????????????????????????????????
     */
    public void notifyStateChangeToAvailable() {
        final List<ConsumerStateListener> onAvailable = consumerConfig.getOnAvailable();
        if (onAvailable != null) {
            AsyncRuntime.getAsyncThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    // ???????????????????????????
                    final Object proxyIns = consumerConfig.getConsumerBootstrap().getProxyIns();
                    for (ConsumerStateListener listener : onAvailable) {
                        try {
                            listener.onAvailable(proxyIns);
                        } catch (Exception e) {
                            LOGGER.warnWithApp(consumerConfig.getAppName(),
                                    LogCodes.getLog(LogCodes.WARN_NOTIFY_CONSUMER_STATE, proxyIns.getClass().getName()));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void init() {
        if (reconThread == null) {
            startReconnectThread();
        }
    }

    @Override
    public void addProvider(ProviderGroup providerGroup) {
        // ?????????tags??????
        if (!ProviderHelper.isEmpty(providerGroup)) {
            addNode(providerGroup.getProviderInfos());
        }
    }

    @Override
    public void removeProvider(ProviderGroup providerGroup) {
        // ?????????tags??????
        if (!ProviderHelper.isEmpty(providerGroup)) {
            removeNode(providerGroup.getProviderInfos());
        }
    }

    @Override
    public void updateProviders(ProviderGroup providerGroup) {
        try {
            if (ProviderHelper.isEmpty(providerGroup)) {
                if (CommonUtils.isNotEmpty(currentProviderList())) {
                    if (LOGGER.isInfoEnabled(consumerConfig.getAppName())) {
                        LOGGER.infoWithApp(consumerConfig.getAppName(),
                                "Clear all providers, may be this consumer has been add to blacklist");
                    }
                    closeAllClientTransports(null);
                }
            } else {
                Collection<ProviderInfo> nowall = currentProviderList();
                List<ProviderInfo> oldAllP = providerGroup.getProviderInfos();
                List<ProviderInfo> nowAllP = new ArrayList<ProviderInfo>(nowall);// ????????????

                // ???????????????????????????
                ListDifference<ProviderInfo> diff = new ListDifference<ProviderInfo>(oldAllP, nowAllP);
                List<ProviderInfo> needAdd = diff.getOnlyOnLeft(); // ????????????
                List<ProviderInfo> needDelete = diff.getOnlyOnRight(); // ????????????
                if (!needAdd.isEmpty()) {
                    addNode(needAdd);
                }
                if (!needDelete.isEmpty()) {
                    removeNode(needDelete);
                }
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled(consumerConfig.getAppName())) {
                LOGGER
                        .errorWithApp(
                                consumerConfig.getAppName(),
                                LogCodes.getLog(LogCodes.ERROR_UPDATE_PROVIDERS, consumerConfig.getInterfaceId(), providerGroup),
                                e);
            }
        }
    }

    @Override
    public void updateAllProviders(List<ProviderGroup> providerGroups) {
        List<ProviderInfo> mergePs = new ArrayList<ProviderInfo>();
        if (CommonUtils.isNotEmpty(providerGroups)) {
            for (ProviderGroup providerGroup : providerGroups) {
                if (!ProviderHelper.isEmpty(providerGroup)) {
                    mergePs.addAll(providerGroup.getProviderInfos());
                }
            }
        }
        updateProviders(new ProviderGroup().addAll(mergePs));
    }

    protected void addNode(List<ProviderInfo> providerInfoList) {

        //first update last all providers
        lastAddresses.addAll(providerInfoList);

        final String interfaceId = consumerConfig.getInterfaceId();
        int providerSize = providerInfoList.size();
        String appName = consumerConfig.getAppName();
        if (LOGGER.isInfoEnabled(appName)) {
            LOGGER.infoWithApp(appName, "Add provider of {}, size is : {}", interfaceId, providerSize);
        }
        if (providerSize > 0) {
            // ?????????????????????
            int threads = Math.min(10, providerSize); // ??????10???
            final CountDownLatch latch = new CountDownLatch(providerSize);
            ThreadPoolExecutor initPool = new ThreadPoolExecutor(threads, threads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(providerInfoList.size()),
                    new NamedThreadFactory("CLI-CONN-" + interfaceId, true));
            int connectTimeout = consumerConfig.getConnectTimeout();
            for (final ProviderInfo providerInfo : providerInfoList) {
                initClientRunnable(initPool, latch, providerInfo);
            }

            try {
                int totalTimeout = ((providerSize % threads == 0) ? (providerSize / threads) : ((providerSize /
                        threads) + 1)) * connectTimeout + 500;
                latch.await(totalTimeout, TimeUnit.MILLISECONDS); // ??????????????????????????????
            } catch (InterruptedException e) {
                LOGGER.errorWithApp(appName,
                        LogCodes.getLog(LogCodes.ERROR_UPDATE_PROVIDERS, consumerConfig.getInterfaceId(), ""), e);
            } finally {
                initPool.shutdown(); // ???????????????
            }
        }
    }

    /**
     * ????????????????????????
     */
    protected void initClientRunnable(ThreadPoolExecutor initPool, final CountDownLatch latch,
                                      final ProviderInfo providerInfo) {
        final ClientTransportConfig config = providerToClientConfig(providerInfo);
        initPool.execute(new Runnable() {
            @Override
            public void run() {
                ClientTransport transport = ClientTransportFactory.getClientTransport(config);
                if (consumerConfig.isLazy()) {
                    uninitializedConnections.put(providerInfo, transport);
                    latch.countDown();
                } else {
                    try {
                        initClientTransport(consumerConfig.getInterfaceId(), providerInfo, transport);
                    } finally {
                        latch.countDown(); // ?????????????????????
                    }
                }
            }
        });
    }

    protected void initClientTransport(String interfaceId, ProviderInfo providerInfo, ClientTransport transport) {
        try {
            transport.connect();
            if (doubleCheck(interfaceId, providerInfo, transport)) {
                printSuccess(interfaceId, providerInfo, transport);
                addAlive(providerInfo, transport);
            } else {
                printFailure(interfaceId, providerInfo, transport);
                addRetry(providerInfo, transport);
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled(consumerConfig.getAppName())) {
                LOGGER.debugWithApp(consumerConfig.getAppName(), "Failed to connect " + providerInfo, e);
            }
            printDead(interfaceId, providerInfo, transport, e);
            addRetry(providerInfo, transport);
        }
    }

    public void removeNode(List<ProviderInfo> providerInfos) {

        //first update last all providers
        lastAddresses.removeAll(providerInfos);

        String interfaceId = consumerConfig.getInterfaceId();
        String appName = consumerConfig.getAppName();
        if (LOGGER.isInfoEnabled(appName)) {
            LOGGER.infoWithApp(appName, "Remove provider of {}, size is : {}", interfaceId, providerInfos.size());
        }
        for (ProviderInfo providerInfo : providerInfos) {
            try {
                // ????????????????????????????????????
                //  ????????????????????? ??????????????????
                ClientTransport transport = remove(providerInfo);
                if (LOGGER.isInfoEnabled(appName)) {
                    LOGGER.infoWithApp(appName, "Remove provider of {}: {} from list success !", interfaceId,
                            providerInfo);
                }
                if (transport != null) {
                    ClientTransportFactory.releaseTransport(transport, consumerConfig.getDisconnectTimeout());
                }
            } catch (Exception e) {
                LOGGER.errorWithApp(appName,
                        LogCodes.getLog(LogCodes.ERROR_DELETE_PROVIDERS, consumerConfig.getInterfaceId(), providerInfo), e);
            }
        }
    }

    @Override
    public ConcurrentMap<ProviderInfo, ClientTransport> getAvailableConnections() {
        return aliveConnections.isEmpty() ? subHealthConnections : aliveConnections;
    }

    @Override
    public List<ProviderInfo> getAvailableProviders() {
        // ???????????????????????????????????????
        ConcurrentMap<ProviderInfo, ClientTransport> map =
                aliveConnections.isEmpty() ? subHealthConnections : aliveConnections;
        return new ArrayList<ProviderInfo>(map.keySet());
    }

    @Override
    public ClientTransport getAvailableClientTransport(ProviderInfo providerInfo) {
        // ??????????????????
        ClientTransport transport = aliveConnections.get(providerInfo);
        if (transport != null) {
            return transport;
        }
        // ?????????????????????
        transport = subHealthConnections.get(providerInfo);
        if (transport != null) {
            return transport;
        }
        // ?????????????????????????????????????????????
        transport = uninitializedConnections.get(providerInfo);
        if (transport != null) {
            // ????????????????????????
            synchronized (this) {
                transport = uninitializedConnections.get(providerInfo);
                if (transport != null) {
                    initClientTransport(consumerConfig.getInterfaceId(), providerInfo, transport);
                    uninitializedConnections.remove(providerInfo);
                }
                return getAvailableClientTransport(providerInfo);
            }
        }

        if (createConnWhenAbsent) {
            RpcInternalContext context = RpcInternalContext.peekContext();
            String targetIP = (context == null) ? null : (String) context
                    .getAttachment(RpcConstants.HIDDEN_KEY_PINPOINT);
            /**
             * RpcInvokeContext.getContext().setTargetUrl() ???????????????????????????tcp??????
             */
            if (StringUtils.isNotBlank(targetIP)) {
                ClientTransportConfig transportConfig = providerToClientConfig(providerInfo);
                transport = ClientTransportFactory.getClientTransport(transportConfig);
                initClientTransport(consumerConfig.getInterfaceId(), providerInfo, transport);
            }
        }

        return transport;
    }

    @Override
    public boolean isAvailableEmpty() {
        return aliveConnections.isEmpty() && subHealthConnections.isEmpty();
    }

    /**
     * Provider???????????? ClientTransportConfig
     *
     * @param providerInfo Provider
     * @return ClientTransportConfig
     */
    protected ClientTransportConfig providerToClientConfig(ProviderInfo providerInfo) {
        return new ClientTransportConfig()
                .setConsumerConfig(consumerConfig)
                .setProviderInfo(providerInfo)
                .setContainer(consumerConfig.getProtocol())
                .setConnectTimeout(consumerConfig.getConnectTimeout())
                .setInvokeTimeout(consumerConfig.getTimeout())
                .setDisconnectTimeout(consumerConfig.getDisconnectTimeout())
                .setConnectionNum(consumerConfig.getConnectionNum())
                .setChannelListeners(consumerConfig.getOnConnect());
    }

    /**
     * ???????????????Provider???????????????????????????????????????
     *
     * @return ?????????Provider?????? set
     */
    @Override
    public Set<ProviderInfo> currentProviderList() {
        providerLock.lock();
        try {
            ConcurrentHashSet<ProviderInfo> providerInfos = new ConcurrentHashSet<ProviderInfo>();
            providerInfos.addAll(lastAddresses);
            return providerInfos;
        } finally {
            providerLock.unlock();
        }
    }

    @Override
    public void setUnavailable(ProviderInfo providerInfo, ClientTransport transport) {
        providerLock.lock();
        try {
            boolean first = isAvailableEmpty();
            if (aliveConnections.remove(providerInfo) != null) {
                retryConnections.put(providerInfo, transport);
                if (!first && isAvailableEmpty()) { // ????????????????????????
                    notifyStateChangeToUnavailable();
                }
            }
        } finally {
            providerLock.unlock();
        }
    }

    @Override
    public void destroy() {
        destroy(null);
    }

    @Override
    public void destroy(DestroyHook destroyHook) {
        // ??????????????????
        shutdownReconnectThread();
        // ?????????????????????
        closeAllClientTransports(destroyHook);
    }

    /**
     * ??????????????????
     *
     * @return ????????????????????????
     */
    protected Map<ProviderInfo, ClientTransport> clearProviders() {
        providerLock.lock();
        try {
            // ????????????+?????????
            HashMap<ProviderInfo, ClientTransport> all = new HashMap<ProviderInfo, ClientTransport>(aliveConnections);
            all.putAll(subHealthConnections);
            all.putAll(retryConnections);
            all.putAll(uninitializedConnections);
            subHealthConnections.clear();
            aliveConnections.clear();
            retryConnections.clear();
            uninitializedConnections.clear();
            lastAddresses.clear();
            return all;
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ??????????????????
     *
     * @param destroyHook ????????????
     */
    @Override
    public void closeAllClientTransports(DestroyHook destroyHook) {

        // ??????????????????,???????????????
        Map<ProviderInfo, ClientTransport> all = clearProviders();
        if (destroyHook != null) {
            try {
                destroyHook.preDestroy();
            } catch (Exception e) {
                if (LOGGER.isWarnEnabled(consumerConfig.getAppName())) {
                    LOGGER.warnWithApp(consumerConfig.getAppName(), e.getMessage(), e);
                }
            }
        }
        // ????????????????????????????????????
        int providerSize = all.size();
        if (providerSize > 0) {
            int timeout = consumerConfig.getDisconnectTimeout();
            int threads = Math.min(10, providerSize); // ??????10???
            final CountDownLatch latch = new CountDownLatch(providerSize);
            ThreadPoolExecutor closePool = new ThreadPoolExecutor(threads, threads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(providerSize),
                    new NamedThreadFactory("CLI-DISCONN-" + consumerConfig.getInterfaceId(), true));
            for (Map.Entry<ProviderInfo, ClientTransport> entry : all.entrySet()) {
                final ProviderInfo providerInfo = entry.getKey();
                final ClientTransport transport = entry.getValue();
                closePool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ClientTransportFactory.releaseTransport(transport, 0);
                        } catch (Exception e) {
                            if (LOGGER.isWarnEnabled(consumerConfig.getAppName())) {
                                LOGGER.warnWithApp(consumerConfig.getAppName(),
                                        "catch exception but ignore it when close alive client : {}", providerInfo);
                            }
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            try {
                int totalTimeout = ((providerSize % threads == 0) ? (providerSize / threads) : ((providerSize /
                        threads) + 1)) * timeout + 500;
                latch.await(totalTimeout, TimeUnit.MILLISECONDS); // ????????????
            } catch (InterruptedException e) {
                LOGGER.errorWithApp(consumerConfig.getAppName(), "Exception when close transport", e);
            } finally {
                closePool.shutdown();
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param interfaceId  ????????????
     * @param providerInfo ?????????
     * @param transport    ??????
     */
    protected void printSuccess(String interfaceId, ProviderInfo providerInfo, ClientTransport transport) {
        if (LOGGER.isInfoEnabled(consumerConfig.getAppName())) {
            LOGGER.infoWithApp(consumerConfig.getAppName(), "Connect to {} provider:{} success ! The connection is "
                            + NetUtils.connectToString(transport.remoteAddress(), transport.localAddress())
                    , interfaceId, providerInfo);
        }
    }

    /**
     * ????????????????????????
     *
     * @param interfaceId  ????????????
     * @param providerInfo ?????????
     * @param transport    ??????
     */
    protected void printFailure(String interfaceId, ProviderInfo providerInfo, ClientTransport transport) {
        if (LOGGER.isInfoEnabled(consumerConfig.getAppName())) {
            LOGGER.infoWithApp(consumerConfig.getAppName(), "Connect to {} provider:{} failure !", interfaceId,
                    providerInfo);
        }
    }

    /**
     * ?????????????????????
     *
     * @param interfaceId  ????????????
     * @param providerInfo ?????????
     * @param transport    ??????
     * @param e            ??????
     */
    protected void printDead(String interfaceId, ProviderInfo providerInfo, ClientTransport transport, Exception e) {
        Throwable cause = e.getCause();
        if (LOGGER.isWarnEnabled(consumerConfig.getAppName())) {
            LOGGER.warnWithApp(consumerConfig.getAppName(),
                    "Connect to {} provider:{} failure !! The exception is " + ExceptionUtils.toShortString(e, 1)
                            + (cause != null ? ", cause by " + cause.getMessage() + "." : "."),
                    interfaceId, providerInfo);
        }
    }

    /**
     * ???????????????Provider???????????????????????????????????????
     *
     * @return ?????????Provider?????? set
     */
    public Map<String, Set<ProviderInfo>> currentProviderMap() {
        providerLock.lock();
        try {
            Map<String, Set<ProviderInfo>> tmp = new LinkedHashMap<String, Set<ProviderInfo>>();
            tmp.put("alive", new HashSet<ProviderInfo>(aliveConnections.keySet()));
            tmp.put("subHealth", new HashSet<ProviderInfo>(subHealthConnections.keySet()));
            tmp.put("retry", new HashSet<ProviderInfo>(retryConnections.keySet()));
            tmp.put("uninitialized", new HashSet<ProviderInfo>(uninitializedConnections.keySet()));
            tmp.put("all", new HashSet<ProviderInfo>(lastAddresses));
            return tmp;
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * ??????????????????ClientTransport????????????
     *
     * @param interfaceId ??????
     * @param transport   ClientTransport??????
     * @return ????????????
     */
    protected boolean doubleCheck(String interfaceId, ProviderInfo providerInfo, ClientTransport transport) {
        if (transport.isAvailable()) {
            if (!connectionValidate) {
                return true;
            } else {
                try { // ???????????? ???????????????????????????????????????
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore
                }
                if (transport.isAvailable()) { // double check
                    return true;
                } else { // ?????????????????????????????????????????????
                    if (LOGGER.isWarnEnabled(consumerConfig.getAppName())) {
                        LOGGER.warnWithApp(consumerConfig.getAppName(),
                                "Connection has been closed after connected (in last 100ms)!" +
                                        " Maybe connectionNum of provider has been reached limit," +
                                        " or your host is in the blacklist of provider {}/{}",
                                interfaceId, transport.getConfig().getProviderInfo());
                    }
                    providerInfo.setDynamicAttr(ProviderInfoAttrs.ATTR_RC_PERIOD_COEFFICIENT, 5);
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    /**
     * ????????????+????????????
     */
    protected void startReconnectThread() {
        final String interfaceId = consumerConfig.getInterfaceId();
        // ???????????????
        // ????????????10?????????
        int reconnect = consumerConfig.getReconnectPeriod();
        if (reconnect > 0) {
            reconnect = Math.max(reconnect, 2000); // ??????2000
            reconThread = new ScheduledService("CLI-RC-" + interfaceId, ScheduledService.MODE_FIXEDDELAY, new
                    Runnable() {
                        @Override
                        public void run() {
                            try {
                                doReconnect();
                            } catch (Throwable e) {
                                LOGGER.warnWithApp(consumerConfig.getAppName(),
                                        "Exception when retry connect to provider", e);
                            }
                        }
                    }, reconnect, reconnect, TimeUnit.MILLISECONDS).start();
        }
    }

    /**
     * ??????????????????????????????
     */
    private void doReconnect() {
        String interfaceId = consumerConfig.getInterfaceId();
        String appName = consumerConfig.getAppName();
        int thisTime = reconnectFlag.incrementAndGet();
        boolean print = thisTime % 6 == 0; //????????????error,???6???????????????
        boolean isAliveEmptyFirst = isAvailableEmpty();
        // ??????????????????  todo subHealth
        for (Map.Entry<ProviderInfo, ClientTransport> alive : aliveConnections.entrySet()) {
            ClientTransport connection = alive.getValue();
            if (connection != null && !connection.isAvailable()) {
                aliveToRetry(alive.getKey(), connection);
            }
        }
        for (Map.Entry<ProviderInfo, ClientTransport> entry : getRetryConnections()
                .entrySet()) {
            ProviderInfo providerInfo = entry.getKey();
            int providerPeriodCoefficient = CommonUtils.parseNum((Integer)
                    providerInfo.getDynamicAttr(ProviderInfoAttrs.ATTR_RC_PERIOD_COEFFICIENT), 1);
            if (thisTime % providerPeriodCoefficient != 0) {
                continue; // ??????????????????????????????????????????
            }
            ClientTransport transport = entry.getValue();
            if (LOGGER.isDebugEnabled(appName)) {
                LOGGER.debugWithApp(appName, "Retry connect to {} provider:{} ...", interfaceId, providerInfo);
            }
            try {
                transport.connect();
                if (doubleCheck(interfaceId, providerInfo, transport)) {
                    providerInfo.setDynamicAttr(ProviderInfoAttrs.ATTR_RC_PERIOD_COEFFICIENT, 1);
                    retryToAlive(providerInfo, transport);
                }
            } catch (Exception e) {
                if (print) {
                    if (LOGGER.isWarnEnabled(appName)) {
                        LOGGER.warnWithApp(appName, "Retry connect to {} provider:{} error ! The exception is " + e
                                .getMessage(), interfaceId, providerInfo);
                    }
                } else {
                    if (LOGGER.isDebugEnabled(appName)) {
                        LOGGER.debugWithApp(appName, "Retry connect to {} provider:{} error ! The exception is " + e
                                .getMessage(), interfaceId, providerInfo);
                    }
                }
            }
        }
        if (isAliveEmptyFirst && !isAvailableEmpty()) { // ????????????????????????
            notifyStateChangeToAvailable();
        }
    }

    /**
     * ????????????
     */
    protected void shutdownReconnectThread() {
        if (reconThread != null) {
            reconThread.shutdown();
            reconThread = null;
        }
    }
}
