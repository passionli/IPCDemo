# IPCDemo
Android IPC Demo

# Feature
- IInterface Binder接口
- Stub 接口在服务端的实现
- Proxy 接口在客户端的实现
- transact 运行在调用者线程
- onTransact 运行在Binder线程池
- DeathRecipient Binder死亡通知，自动重连服务端
- RemoteCallbackList 反序列化后是不同的对象，回调客户端
- BinderPool 一个Service多个Binder
- Permission 调用者权限
