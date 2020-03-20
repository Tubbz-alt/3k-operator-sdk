## Minikube with Audit Enabled
```text
minikube start \
    --extra-config=apiserver.audit-dynamic-configuration=true \
    --feature-gates=DynamicAuditing=true \
    --extra-config=apiserver.runtime-config=auditregistration.k8s.io/v1alpha1 \
    --extra-config=apiserver.audit-log-format=json \
    --kubernetes-version v1.15.6
```
## Operator Install

Install Operator Lifecycle Manager (OLM), a tool to help manage the Operators running on your cluster.
```text
curl -sL https://github.com/operator-framework/operator-lifecycle-manager/releases/download/0.14.1/install.sh | bash -s 0.14.1
```

Install the Falco operator by running the following command:
```text
kubectl create -f https://operatorhub.io/install/falco.yaml
```

## Set the Configuration

POST : http://localhost:8080/controller/config/namespace/operator-test/name/brvith-selfservice

```json
{
	"vnfName" : "sample-vnf",
	"vnfId" : "vnf-123",
	"size" : 3
}
```
## Watch Logs
```text
2020-03-18 15:17:24.523 [pool-2-thread-1] INFO  controller.KKKCrdController - Watch Updated CRD : apiVersion: app.brvith.com/v1alpha1
kind: KKKCrd
metadata:
  creationTimestamp: '2020-03-18T15:03:47.000-04:00'
  generation: 2
  name: brvith-selfservice
  namespace: operator-test
  resourceVersion: '217930'
  selfLink: /apis/app.brvith.com/v1alpha1/namespaces/operator-test/kkkcrds/brvith-selfservice
  uid: fbb0d243-fdb6-4660-bafe-05b921faad8e
spec:
  size: 3
  vnfId: vnf-123
  vnfName: sample-vnf
```
## Reconciler Logs
```text
2020-03-18 15:17:24.523 [kkkcrd-controller-1] DEBUG i.k.c.e.controller.DefaultController - Controller kkkcrd-controller start reconciling Request{name='brvith-selfservice', namespace='operator-test'}..
2020-03-18 15:17:24.524 [kkkcrd-controller-1] INFO  controller.KKKCrdReconciler - ######## Reconciler triggered : apiVersion: app.brvith.com/v1alpha1
kind: KKKCrd
metadata:
  creationTimestamp: '2020-03-18T15:03:47.000-04:00'
  generation: 2
  name: brvith-selfservice
  namespace: operator-test
  resourceVersion: '217930'
  selfLink: /apis/app.brvith.com/v1alpha1/namespaces/operator-test/kkkcrds/brvith-selfservice
  uid: fbb0d243-fdb6-4660-bafe-05b921faad8e
spec:
  size: 3
  vnfId: vnf-123
  vnfName: sample-vnf

2020-03-18 15:17:24.524 [kkkcrd-controller-1] DEBUG i.k.c.e.controller.DefaultController - Controller kkkcrd-controller finished reconciling Request{name='brvith-selfservice', namespace='operator-test'}..
```