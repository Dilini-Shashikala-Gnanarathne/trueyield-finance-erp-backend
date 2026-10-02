package com.financeapp.marketplace.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.66.0)",
    comments = "Source: marketplace.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MarketplaceServiceGrpc {

  private MarketplaceServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "marketplace.MarketplaceService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.GetListingRequest,
      com.financeapp.marketplace.grpc.proto.GetListingResponse> getGetListingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetListing",
      requestType = com.financeapp.marketplace.grpc.proto.GetListingRequest.class,
      responseType = com.financeapp.marketplace.grpc.proto.GetListingResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.GetListingRequest,
      com.financeapp.marketplace.grpc.proto.GetListingResponse> getGetListingMethod() {
    io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.GetListingRequest, com.financeapp.marketplace.grpc.proto.GetListingResponse> getGetListingMethod;
    if ((getGetListingMethod = MarketplaceServiceGrpc.getGetListingMethod) == null) {
      synchronized (MarketplaceServiceGrpc.class) {
        if ((getGetListingMethod = MarketplaceServiceGrpc.getGetListingMethod) == null) {
          MarketplaceServiceGrpc.getGetListingMethod = getGetListingMethod =
              io.grpc.MethodDescriptor.<com.financeapp.marketplace.grpc.proto.GetListingRequest, com.financeapp.marketplace.grpc.proto.GetListingResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetListing"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.GetListingRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.GetListingResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MarketplaceServiceMethodDescriptorSupplier("GetListing"))
              .build();
        }
      }
    }
    return getGetListingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.ReserveStockRequest,
      com.financeapp.marketplace.grpc.proto.ReserveStockResponse> getReserveStockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReserveStock",
      requestType = com.financeapp.marketplace.grpc.proto.ReserveStockRequest.class,
      responseType = com.financeapp.marketplace.grpc.proto.ReserveStockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.ReserveStockRequest,
      com.financeapp.marketplace.grpc.proto.ReserveStockResponse> getReserveStockMethod() {
    io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.ReserveStockRequest, com.financeapp.marketplace.grpc.proto.ReserveStockResponse> getReserveStockMethod;
    if ((getReserveStockMethod = MarketplaceServiceGrpc.getReserveStockMethod) == null) {
      synchronized (MarketplaceServiceGrpc.class) {
        if ((getReserveStockMethod = MarketplaceServiceGrpc.getReserveStockMethod) == null) {
          MarketplaceServiceGrpc.getReserveStockMethod = getReserveStockMethod =
              io.grpc.MethodDescriptor.<com.financeapp.marketplace.grpc.proto.ReserveStockRequest, com.financeapp.marketplace.grpc.proto.ReserveStockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReserveStock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.ReserveStockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.ReserveStockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MarketplaceServiceMethodDescriptorSupplier("ReserveStock"))
              .build();
        }
      }
    }
    return getReserveStockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.ReleaseStockRequest,
      com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> getReleaseStockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReleaseStock",
      requestType = com.financeapp.marketplace.grpc.proto.ReleaseStockRequest.class,
      responseType = com.financeapp.marketplace.grpc.proto.ReleaseStockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.ReleaseStockRequest,
      com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> getReleaseStockMethod() {
    io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.ReleaseStockRequest, com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> getReleaseStockMethod;
    if ((getReleaseStockMethod = MarketplaceServiceGrpc.getReleaseStockMethod) == null) {
      synchronized (MarketplaceServiceGrpc.class) {
        if ((getReleaseStockMethod = MarketplaceServiceGrpc.getReleaseStockMethod) == null) {
          MarketplaceServiceGrpc.getReleaseStockMethod = getReleaseStockMethod =
              io.grpc.MethodDescriptor.<com.financeapp.marketplace.grpc.proto.ReleaseStockRequest, com.financeapp.marketplace.grpc.proto.ReleaseStockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReleaseStock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.ReleaseStockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.ReleaseStockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MarketplaceServiceMethodDescriptorSupplier("ReleaseStock"))
              .build();
        }
      }
    }
    return getReleaseStockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest,
      com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse> getCheckHealthMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CheckHealth",
      requestType = com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest.class,
      responseType = com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest,
      com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse> getCheckHealthMethod() {
    io.grpc.MethodDescriptor<com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest, com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse> getCheckHealthMethod;
    if ((getCheckHealthMethod = MarketplaceServiceGrpc.getCheckHealthMethod) == null) {
      synchronized (MarketplaceServiceGrpc.class) {
        if ((getCheckHealthMethod = MarketplaceServiceGrpc.getCheckHealthMethod) == null) {
          MarketplaceServiceGrpc.getCheckHealthMethod = getCheckHealthMethod =
              io.grpc.MethodDescriptor.<com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest, com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CheckHealth"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MarketplaceServiceMethodDescriptorSupplier("CheckHealth"))
              .build();
        }
      }
    }
    return getCheckHealthMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MarketplaceServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MarketplaceServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MarketplaceServiceStub>() {
        @java.lang.Override
        public MarketplaceServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MarketplaceServiceStub(channel, callOptions);
        }
      };
    return MarketplaceServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MarketplaceServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MarketplaceServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MarketplaceServiceBlockingStub>() {
        @java.lang.Override
        public MarketplaceServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MarketplaceServiceBlockingStub(channel, callOptions);
        }
      };
    return MarketplaceServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MarketplaceServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MarketplaceServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MarketplaceServiceFutureStub>() {
        @java.lang.Override
        public MarketplaceServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MarketplaceServiceFutureStub(channel, callOptions);
        }
      };
    return MarketplaceServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Fetch listing details, availability, and price for a given listing.
     * </pre>
     */
    default void getListing(com.financeapp.marketplace.grpc.proto.GetListingRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.GetListingResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetListingMethod(), responseObserver);
    }

    /**
     * <pre>
     * Atomically reserve stock for an order. Idempotent on orderId.
     * </pre>
     */
    default void reserveStock(com.financeapp.marketplace.grpc.proto.ReserveStockRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.ReserveStockResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReserveStockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Release previously reserved stock (on order rejection/cancellation).
     * </pre>
     */
    default void releaseStock(com.financeapp.marketplace.grpc.proto.ReleaseStockRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReleaseStockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Health check
     * </pre>
     */
    default void checkHealth(com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckHealthMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MarketplaceService.
   */
  public static abstract class MarketplaceServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MarketplaceServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MarketplaceService.
   */
  public static final class MarketplaceServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MarketplaceServiceStub> {
    private MarketplaceServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MarketplaceServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MarketplaceServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch listing details, availability, and price for a given listing.
     * </pre>
     */
    public void getListing(com.financeapp.marketplace.grpc.proto.GetListingRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.GetListingResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetListingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Atomically reserve stock for an order. Idempotent on orderId.
     * </pre>
     */
    public void reserveStock(com.financeapp.marketplace.grpc.proto.ReserveStockRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.ReserveStockResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReserveStockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Release previously reserved stock (on order rejection/cancellation).
     * </pre>
     */
    public void releaseStock(com.financeapp.marketplace.grpc.proto.ReleaseStockRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReleaseStockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Health check
     * </pre>
     */
    public void checkHealth(com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest request,
        io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckHealthMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MarketplaceService.
   */
  public static final class MarketplaceServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MarketplaceServiceBlockingStub> {
    private MarketplaceServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MarketplaceServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MarketplaceServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch listing details, availability, and price for a given listing.
     * </pre>
     */
    public com.financeapp.marketplace.grpc.proto.GetListingResponse getListing(com.financeapp.marketplace.grpc.proto.GetListingRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetListingMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Atomically reserve stock for an order. Idempotent on orderId.
     * </pre>
     */
    public com.financeapp.marketplace.grpc.proto.ReserveStockResponse reserveStock(com.financeapp.marketplace.grpc.proto.ReserveStockRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReserveStockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Release previously reserved stock (on order rejection/cancellation).
     * </pre>
     */
    public com.financeapp.marketplace.grpc.proto.ReleaseStockResponse releaseStock(com.financeapp.marketplace.grpc.proto.ReleaseStockRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReleaseStockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Health check
     * </pre>
     */
    public com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse checkHealth(com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckHealthMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MarketplaceService.
   */
  public static final class MarketplaceServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MarketplaceServiceFutureStub> {
    private MarketplaceServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MarketplaceServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MarketplaceServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch listing details, availability, and price for a given listing.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.financeapp.marketplace.grpc.proto.GetListingResponse> getListing(
        com.financeapp.marketplace.grpc.proto.GetListingRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetListingMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Atomically reserve stock for an order. Idempotent on orderId.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.financeapp.marketplace.grpc.proto.ReserveStockResponse> reserveStock(
        com.financeapp.marketplace.grpc.proto.ReserveStockRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReserveStockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Release previously reserved stock (on order rejection/cancellation).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> releaseStock(
        com.financeapp.marketplace.grpc.proto.ReleaseStockRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReleaseStockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Health check
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse> checkHealth(
        com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckHealthMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_LISTING = 0;
  private static final int METHODID_RESERVE_STOCK = 1;
  private static final int METHODID_RELEASE_STOCK = 2;
  private static final int METHODID_CHECK_HEALTH = 3;

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
        case METHODID_GET_LISTING:
          serviceImpl.getListing((com.financeapp.marketplace.grpc.proto.GetListingRequest) request,
              (io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.GetListingResponse>) responseObserver);
          break;
        case METHODID_RESERVE_STOCK:
          serviceImpl.reserveStock((com.financeapp.marketplace.grpc.proto.ReserveStockRequest) request,
              (io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.ReserveStockResponse>) responseObserver);
          break;
        case METHODID_RELEASE_STOCK:
          serviceImpl.releaseStock((com.financeapp.marketplace.grpc.proto.ReleaseStockRequest) request,
              (io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.ReleaseStockResponse>) responseObserver);
          break;
        case METHODID_CHECK_HEALTH:
          serviceImpl.checkHealth((com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest) request,
              (io.grpc.stub.StreamObserver<com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse>) responseObserver);
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
          getGetListingMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.financeapp.marketplace.grpc.proto.GetListingRequest,
              com.financeapp.marketplace.grpc.proto.GetListingResponse>(
                service, METHODID_GET_LISTING)))
        .addMethod(
          getReserveStockMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.financeapp.marketplace.grpc.proto.ReserveStockRequest,
              com.financeapp.marketplace.grpc.proto.ReserveStockResponse>(
                service, METHODID_RESERVE_STOCK)))
        .addMethod(
          getReleaseStockMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.financeapp.marketplace.grpc.proto.ReleaseStockRequest,
              com.financeapp.marketplace.grpc.proto.ReleaseStockResponse>(
                service, METHODID_RELEASE_STOCK)))
        .addMethod(
          getCheckHealthMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.financeapp.marketplace.grpc.proto.MarketplaceHealthRequest,
              com.financeapp.marketplace.grpc.proto.MarketplaceHealthResponse>(
                service, METHODID_CHECK_HEALTH)))
        .build();
  }

  private static abstract class MarketplaceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MarketplaceServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.financeapp.marketplace.grpc.proto.MarketplaceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MarketplaceService");
    }
  }

  private static final class MarketplaceServiceFileDescriptorSupplier
      extends MarketplaceServiceBaseDescriptorSupplier {
    MarketplaceServiceFileDescriptorSupplier() {}
  }

  private static final class MarketplaceServiceMethodDescriptorSupplier
      extends MarketplaceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MarketplaceServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MarketplaceServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MarketplaceServiceFileDescriptorSupplier())
              .addMethod(getGetListingMethod())
              .addMethod(getReserveStockMethod())
              .addMethod(getReleaseStockMethod())
              .addMethod(getCheckHealthMethod())
              .build();
        }
      }
    }
    return result;
  }
}
