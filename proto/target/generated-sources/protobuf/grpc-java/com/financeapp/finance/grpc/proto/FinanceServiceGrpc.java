package com.financeapp.finance.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.66.0)",
    comments = "Source: finance.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class FinanceServiceGrpc {

  private FinanceServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "finance.FinanceService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.financeapp.finance.grpc.proto.CreateJournalEntryRequest,
      com.financeapp.finance.grpc.proto.CreateJournalEntryResponse> getCreateJournalEntryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateJournalEntry",
      requestType = com.financeapp.finance.grpc.proto.CreateJournalEntryRequest.class,
      responseType = com.financeapp.finance.grpc.proto.CreateJournalEntryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.financeapp.finance.grpc.proto.CreateJournalEntryRequest,
      com.financeapp.finance.grpc.proto.CreateJournalEntryResponse> getCreateJournalEntryMethod() {
    io.grpc.MethodDescriptor<com.financeapp.finance.grpc.proto.CreateJournalEntryRequest, com.financeapp.finance.grpc.proto.CreateJournalEntryResponse> getCreateJournalEntryMethod;
    if ((getCreateJournalEntryMethod = FinanceServiceGrpc.getCreateJournalEntryMethod) == null) {
      synchronized (FinanceServiceGrpc.class) {
        if ((getCreateJournalEntryMethod = FinanceServiceGrpc.getCreateJournalEntryMethod) == null) {
          FinanceServiceGrpc.getCreateJournalEntryMethod = getCreateJournalEntryMethod =
              io.grpc.MethodDescriptor.<com.financeapp.finance.grpc.proto.CreateJournalEntryRequest, com.financeapp.finance.grpc.proto.CreateJournalEntryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateJournalEntry"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.finance.grpc.proto.CreateJournalEntryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.finance.grpc.proto.CreateJournalEntryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FinanceServiceMethodDescriptorSupplier("CreateJournalEntry"))
              .build();
        }
      }
    }
    return getCreateJournalEntryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.financeapp.finance.grpc.proto.HealthCheckRequest,
      com.financeapp.finance.grpc.proto.HealthCheckResponse> getCheckHealthMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CheckHealth",
      requestType = com.financeapp.finance.grpc.proto.HealthCheckRequest.class,
      responseType = com.financeapp.finance.grpc.proto.HealthCheckResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.financeapp.finance.grpc.proto.HealthCheckRequest,
      com.financeapp.finance.grpc.proto.HealthCheckResponse> getCheckHealthMethod() {
    io.grpc.MethodDescriptor<com.financeapp.finance.grpc.proto.HealthCheckRequest, com.financeapp.finance.grpc.proto.HealthCheckResponse> getCheckHealthMethod;
    if ((getCheckHealthMethod = FinanceServiceGrpc.getCheckHealthMethod) == null) {
      synchronized (FinanceServiceGrpc.class) {
        if ((getCheckHealthMethod = FinanceServiceGrpc.getCheckHealthMethod) == null) {
          FinanceServiceGrpc.getCheckHealthMethod = getCheckHealthMethod =
              io.grpc.MethodDescriptor.<com.financeapp.finance.grpc.proto.HealthCheckRequest, com.financeapp.finance.grpc.proto.HealthCheckResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CheckHealth"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.finance.grpc.proto.HealthCheckRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.finance.grpc.proto.HealthCheckResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FinanceServiceMethodDescriptorSupplier("CheckHealth"))
              .build();
        }
      }
    }
    return getCheckHealthMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FinanceServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FinanceServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FinanceServiceStub>() {
        @java.lang.Override
        public FinanceServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FinanceServiceStub(channel, callOptions);
        }
      };
    return FinanceServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FinanceServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FinanceServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FinanceServiceBlockingStub>() {
        @java.lang.Override
        public FinanceServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FinanceServiceBlockingStub(channel, callOptions);
        }
      };
    return FinanceServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FinanceServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FinanceServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FinanceServiceFutureStub>() {
        @java.lang.Override
        public FinanceServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FinanceServiceFutureStub(channel, callOptions);
        }
      };
    return FinanceServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Create a double-entry journal entry in the Finance ledger.
     * Idempotent: calling with the same reference returns the existing entry.
     * </pre>
     */
    default void createJournalEntry(com.financeapp.finance.grpc.proto.CreateJournalEntryRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.finance.grpc.proto.CreateJournalEntryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateJournalEntryMethod(), responseObserver);
    }

    /**
     * <pre>
     * Health check for circuit-breaker / monitoring purposes
     * </pre>
     */
    default void checkHealth(com.financeapp.finance.grpc.proto.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.finance.grpc.proto.HealthCheckResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckHealthMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service FinanceService.
   */
  public static abstract class FinanceServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return FinanceServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service FinanceService.
   */
  public static final class FinanceServiceStub
      extends io.grpc.stub.AbstractAsyncStub<FinanceServiceStub> {
    private FinanceServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FinanceServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FinanceServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Create a double-entry journal entry in the Finance ledger.
     * Idempotent: calling with the same reference returns the existing entry.
     * </pre>
     */
    public void createJournalEntry(com.financeapp.finance.grpc.proto.CreateJournalEntryRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.finance.grpc.proto.CreateJournalEntryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateJournalEntryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Health check for circuit-breaker / monitoring purposes
     * </pre>
     */
    public void checkHealth(com.financeapp.finance.grpc.proto.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.finance.grpc.proto.HealthCheckResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckHealthMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service FinanceService.
   */
  public static final class FinanceServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<FinanceServiceBlockingStub> {
    private FinanceServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FinanceServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FinanceServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Create a double-entry journal entry in the Finance ledger.
     * Idempotent: calling with the same reference returns the existing entry.
     * </pre>
     */
    public com.financeapp.finance.grpc.proto.CreateJournalEntryResponse createJournalEntry(com.financeapp.finance.grpc.proto.CreateJournalEntryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateJournalEntryMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Health check for circuit-breaker / monitoring purposes
     * </pre>
     */
    public com.financeapp.finance.grpc.proto.HealthCheckResponse checkHealth(com.financeapp.finance.grpc.proto.HealthCheckRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckHealthMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service FinanceService.
   */
  public static final class FinanceServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<FinanceServiceFutureStub> {
    private FinanceServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FinanceServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FinanceServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Create a double-entry journal entry in the Finance ledger.
     * Idempotent: calling with the same reference returns the existing entry.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.financeapp.finance.grpc.proto.CreateJournalEntryResponse> createJournalEntry(
        com.financeapp.finance.grpc.proto.CreateJournalEntryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateJournalEntryMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Health check for circuit-breaker / monitoring purposes
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.financeapp.finance.grpc.proto.HealthCheckResponse> checkHealth(
        com.financeapp.finance.grpc.proto.HealthCheckRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckHealthMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_JOURNAL_ENTRY = 0;
  private static final int METHODID_CHECK_HEALTH = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_JOURNAL_ENTRY:
          serviceImpl.createJournalEntry((com.financeapp.finance.grpc.proto.CreateJournalEntryRequest) request,
              (io.grpc.stub.StreamObserver<com.financeapp.finance.grpc.proto.CreateJournalEntryResponse>) responseObserver);
          break;
        case METHODID_CHECK_HEALTH:
          serviceImpl.checkHealth((com.financeapp.finance.grpc.proto.HealthCheckRequest) request,
              (io.grpc.stub.StreamObserver<com.financeapp.finance.grpc.proto.HealthCheckResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCreateJournalEntryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.financeapp.finance.grpc.proto.CreateJournalEntryRequest,
              com.financeapp.finance.grpc.proto.CreateJournalEntryResponse>(
                service, METHODID_CREATE_JOURNAL_ENTRY)))
        .addMethod(
          getCheckHealthMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.financeapp.finance.grpc.proto.HealthCheckRequest,
              com.financeapp.finance.grpc.proto.HealthCheckResponse>(
                service, METHODID_CHECK_HEALTH)))
        .build();
  }

  private static abstract class FinanceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FinanceServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.financeapp.finance.grpc.proto.FinanceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("FinanceService");
    }
  }

  private static final class FinanceServiceFileDescriptorSupplier
      extends FinanceServiceBaseDescriptorSupplier {
    FinanceServiceFileDescriptorSupplier() {}
  }

  private static final class FinanceServiceMethodDescriptorSupplier
      extends FinanceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    FinanceServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (FinanceServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FinanceServiceFileDescriptorSupplier())
              .addMethod(getCreateJournalEntryMethod())
              .addMethod(getCheckHealthMethod())
              .build();
        }
      }
    }
    return result;
  }
}
