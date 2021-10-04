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
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ProtoService.proto

package com.alipay.sofa.rpc.protobuf;

public final class ProtoServiceOuterClass {
    static final com.google.protobuf.Descriptors.Descriptor internal_static_com_alipay_sofa_rpc_protobuf_EchoRequest_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_com_alipay_sofa_rpc_protobuf_EchoRequest_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_com_alipay_sofa_rpc_protobuf_EchoResponse_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_com_alipay_sofa_rpc_protobuf_EchoResponse_fieldAccessorTable;
    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

    static {
        java.lang.String[] descriptorData = {
                "\n\022ProtoService.proto\022\034com.alipay.sofa.rp" +
                        "c.protobuf\"O\n\013EchoRequest\022\014\n\004name\030\001 \001(\t\022" +
                        "2\n\005group\030\002 \001(\0162#.com.alipay.sofa.rpc.pro" +
                        "tobuf.Group\"-\n\014EchoResponse\022\014\n\004code\030\001 \001(" +
                        "\005\022\017\n\007message\030\002 \001(\t*\025\n\005Group\022\005\n\001A\020\000\022\005\n\001B\020" +
                        "\0012r\n\014ProtoService\022b\n\007echoObj\022).com.alipa" +
                        "y.sofa.rpc.protobuf.EchoRequest\032*.com.al" +
                        "ipay.sofa.rpc.protobuf.EchoResponse\"\000B\002P" +
                        "\001b\006proto3"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
        internal_static_com_alipay_sofa_rpc_protobuf_EchoRequest_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_com_alipay_sofa_rpc_protobuf_EchoRequest_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_com_alipay_sofa_rpc_protobuf_EchoRequest_descriptor,
                new java.lang.String[]{"Name", "Group",});
        internal_static_com_alipay_sofa_rpc_protobuf_EchoResponse_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_com_alipay_sofa_rpc_protobuf_EchoResponse_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_com_alipay_sofa_rpc_protobuf_EchoResponse_descriptor,
                new java.lang.String[]{"Code", "Message",});
    }

    private ProtoServiceOuterClass() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    // @@protoc_insertion_point(outer_class_scope)
}
