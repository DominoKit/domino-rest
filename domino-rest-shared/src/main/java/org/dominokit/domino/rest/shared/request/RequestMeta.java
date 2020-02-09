package org.dominokit.domino.rest.shared.request;

public class RequestMeta {

    private Class<?> serviceClass;
    private String methodName;
    private Class<?> requestClass;
    private Class<?> responseClass;
    private String[] consume;
    private String[] produce;

    public RequestMeta(Class<?> serviceClass, String methodName, Class<?> requestClass, Class<?> responseClass) {
        this.serviceClass = serviceClass;
        this.methodName = methodName;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?> getRequestClass() {
        return requestClass;
    }

    public Class<?> getResponseClass() {
        return responseClass;
    }

    void setConsume(String[] consume) {
        this.consume = consume;
    }

    void setProduce(String[] produce) {
        this.produce = produce;
    }

    public String[] getConsume() {
        return consume;
    }

    public String[] getProduce() {
        return produce;
    }

    @Override
    public String toString() {
        return "RequestMeta{" +
                "serviceClass=" + serviceClass +
                ", methodName='" + methodName + '\'' +
                ", requestClass=" + requestClass +
                ", responseClass=" + responseClass +
                '}';
    }
}
