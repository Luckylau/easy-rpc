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
package com.alipay.sofa.rpc.server.rest;

import com.alipay.sofa.rpc.log.LogCodes;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import com.alipay.sofa.rpc.tracer.sofatracer.RestTracerAdapter;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author <a href="mailto:lw111072@alibaba-inc.com">liangen</a>
 */
@Provider
@Priority(100)
public class TraceRequestFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TraceRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        try {
            RestTracerAdapter.serverFilter(requestContext);
        } catch (Exception e) {
            logger.error(LogCodes.getLog(LogCodes.ERROR_TRACER_UNKNOWN_EXP, "filter", "rest", "server"), e);
        }

    }
}
