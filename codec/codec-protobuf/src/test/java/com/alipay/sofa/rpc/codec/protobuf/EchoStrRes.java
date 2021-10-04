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
// source: com/alipay/test/ProtoService.proto

package com.alipay.sofa.rpc.codec.protobuf;

/**
 * Protobuf type {@code com.alipay.sofa.rpc.codec.protobuf.EchoStrRes}
 */
public final class EchoStrRes extends com.google.protobuf.GeneratedMessageV3 implements
        // @@protoc_insertion_point(message_implements:com.alipay.sofa.rpc.codec.protobuf.EchoStrRes)
        EchoStrResOrBuilder {
    public static final int S_FIELD_NUMBER = 2;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:com.alipay.sofa.rpc.codec.protobuf.EchoStrRes)
    private static final com.alipay.sofa.rpc.codec.protobuf.EchoStrRes DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<EchoStrRes> PARSER = new com.google.protobuf.AbstractParser<EchoStrRes>() {
        public EchoStrRes parsePartialFrom(com.google.protobuf.CodedInputStream input,
                                           com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return new EchoStrRes(
                    input,
                    extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new com.alipay.sofa.rpc.codec.protobuf.EchoStrRes();
    }

    private volatile Object s_;
    private byte memoizedIsInitialized = -1;

    // Use EchoStrRes.newBuilder() to construct.
    private EchoStrRes(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private EchoStrRes() {
        s_ = "";
    }

    private EchoStrRes(com.google.protobuf.CodedInputStream input,
                       com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        this();
        int mutable_bitField0_ = 0;
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        done = true;
                        break;
                    default: {
                        if (!input.skipField(tag)) {
                            done = true;
                        }
                        break;
                    }
                    case 18: {
                        String s = input.readStringRequireUtf8();

                        s_ = s;
                        break;
                    }
                }
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(e)
                    .setUnfinishedMessage(this);
        } finally {
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        return com.alipay.sofa.rpc.codec.protobuf.ProtoServiceModels.internal_static_com_alipay_sofa_rpc_remoting_test_EchoStrRes_descriptor;
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(com.google.protobuf.ByteString data,
                                                                          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(byte[] data,
                                                                          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(java.io.InputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(java.io.InputStream input,
                                                                          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input,
                extensionRegistry);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseDelimitedFrom(java.io.InputStream input,
                                                                                   com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input,
                extensionRegistry);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parseFrom(com.google.protobuf.CodedInputStream input,
                                                                          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input,
                extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(com.alipay.sofa.rpc.codec.protobuf.EchoStrRes prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static com.alipay.sofa.rpc.codec.protobuf.EchoStrRes getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<EchoStrRes> parser() {
        return PARSER;
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }

    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return com.alipay.sofa.rpc.codec.protobuf.ProtoServiceModels.internal_static_com_alipay_sofa_rpc_remoting_test_EchoStrRes_fieldAccessorTable
                .ensureFieldAccessorsInitialized(com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.class,
                        com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.Builder.class);
    }

    /**
     * <code>optional string s = 2;</code>
     */
    public String getS() {
        Object ref = s_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            s_ = s;
            return s;
        }
    }

    /**
     * <code>optional string s = 2;</code>
     */
    public com.google.protobuf.ByteString getSBytes() {
        Object ref = s_;
        if (ref instanceof String) {
            com.google.protobuf.ByteString b = com.google.protobuf.ByteString
                    .copyFromUtf8((String) ref);
            s_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1)
            return true;
        if (isInitialized == 0)
            return false;

        memoizedIsInitialized = 1;
        return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
        if (!getSBytes().isEmpty()) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 2, s_);
        }
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1)
            return size;

        size = 0;
        if (!getSBytes().isEmpty()) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, s_);
        }
        memoizedSize = size;
        return size;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof com.alipay.sofa.rpc.codec.protobuf.EchoStrRes)) {
            return super.equals(obj);
        }
        com.alipay.sofa.rpc.codec.protobuf.EchoStrRes other = (com.alipay.sofa.rpc.codec.protobuf.EchoStrRes) obj;

        boolean result = true;
        result = result && getS().equals(other.getS());
        return result;
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptorForType().hashCode();
        hash = (37 * hash) + S_FIELD_NUMBER;
        hash = (53 * hash) + getS().hashCode();
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public Builder newBuilderForType() {
        return newBuilder();
    }

    public Builder toBuilder() {
        return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
    }

    @Override
    public com.google.protobuf.Parser<EchoStrRes> getParserForType() {
        return PARSER;
    }

    public com.alipay.sofa.rpc.codec.protobuf.EchoStrRes getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Protobuf type {@code com.alipay.sofa.rpc.codec.protobuf.EchoStrRes}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessageV3.Builder<Builder>
            implements
            // @@protoc_insertion_point(builder_implements:com.alipay.sofa.rpc.codec.protobuf.EchoStrRes)
            com.alipay.sofa.rpc.codec.protobuf.EchoStrResOrBuilder {
        private Object s_ = "";

        // Construct using com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.newBuilder()
        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return com.alipay.sofa.rpc.codec.protobuf.ProtoServiceModels.internal_static_com_alipay_sofa_rpc_remoting_test_EchoStrRes_descriptor;
        }

        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return com.alipay.sofa.rpc.codec.protobuf.ProtoServiceModels.internal_static_com_alipay_sofa_rpc_remoting_test_EchoStrRes_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.class,
                            com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
            }
        }

        public Builder clear() {
            super.clear();
            s_ = "";

            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return com.alipay.sofa.rpc.codec.protobuf.ProtoServiceModels.internal_static_com_alipay_sofa_rpc_remoting_test_EchoStrRes_descriptor;
        }

        public com.alipay.sofa.rpc.codec.protobuf.EchoStrRes getDefaultInstanceForType() {
            return com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.getDefaultInstance();
        }

        public com.alipay.sofa.rpc.codec.protobuf.EchoStrRes build() {
            com.alipay.sofa.rpc.codec.protobuf.EchoStrRes result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public com.alipay.sofa.rpc.codec.protobuf.EchoStrRes buildPartial() {
            com.alipay.sofa.rpc.codec.protobuf.EchoStrRes result = new com.alipay.sofa.rpc.codec.protobuf.EchoStrRes(
                    this);
            result.s_ = s_;
            onBuilt();
            return result;
        }

        public Builder clone() {
            return (Builder) super.clone();
        }

        public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
            return (Builder) super.setField(field, value);
        }

        public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
            return (Builder) super.clearField(field);
        }

        public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
            return (Builder) super.clearOneof(oneof);
        }

        public Builder setRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field,
                                        int index, Object value) {
            return (Builder) super.setRepeatedField(field, index, value);
        }

        public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field,
                                        Object value) {
            return (Builder) super.addRepeatedField(field, value);
        }

        public Builder mergeFrom(com.google.protobuf.Message other) {
            if (other instanceof com.alipay.sofa.rpc.codec.protobuf.EchoStrRes) {
                return mergeFrom((com.alipay.sofa.rpc.codec.protobuf.EchoStrRes) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(com.alipay.sofa.rpc.codec.protobuf.EchoStrRes other) {
            if (other == com.alipay.sofa.rpc.codec.protobuf.EchoStrRes.getDefaultInstance())
                return this;
            if (!other.getS().isEmpty()) {
                s_ = other.s_;
                onChanged();
            }
            onChanged();
            return this;
        }

        public final boolean isInitialized() {
            return true;
        }

        public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
                                 com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            com.alipay.sofa.rpc.codec.protobuf.EchoStrRes parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (com.alipay.sofa.rpc.codec.protobuf.EchoStrRes) e
                        .getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        /**
         * <code>optional string s = 2;</code>
         */
        public String getS() {
            Object ref = s_;
            if (!(ref instanceof String)) {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                s_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }

        /**
         * <code>optional string s = 2;</code>
         */
        public Builder setS(String value) {
            if (value == null) {
                throw new NullPointerException();
            }

            s_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>optional string s = 2;</code>
         */
        public com.google.protobuf.ByteString getSBytes() {
            Object ref = s_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString
                        .copyFromUtf8((String) ref);
                s_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        /**
         * <code>optional string s = 2;</code>
         */
        public Builder setSBytes(com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);

            s_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>optional string s = 2;</code>
         */
        public Builder clearS() {

            s_ = getDefaultInstance().getS();
            onChanged();
            return this;
        }

        public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
            return this;
        }

        public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
            return this;
        }

        // @@protoc_insertion_point(builder_scope:com.alipay.sofa.rpc.codec.protobuf.EchoStrRes)
    }

}
