# 3k-operator-sdk
Kubernetes Operator SDK using Kotlin and Ktor

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Overview

An open source toolkit to manage Kubernetes native applications, called Operators, in an effective, automated, and scalable way. Read more in the [Kubernetes Operators][of-blog].

[Operators][operator_link] make it easy to manage complex stateful applications on top of Kubernetes. However writing an operator today can be difficult because of challenges such as using low level APIs, writing boilerplate, and a lack of modularity which leads to duplication.

The 3K Operator SDK is a framework that uses the [kubernetes-client-java][kubernetes_client_java_link] library to make writing operators easier by providing:
- High level APIs and abstractions to write the operational logic more intuitively
- Tools for scaffolding and code generation to bootstrap a new project fast
- Extensions to cover common operator use cases

## Workflow

The SDK provides workflows to develop operators in Kotlin.

The following workflow is for a new **Kotlin** operator:
1. Create a new operator project using the SDK Command Line Interface(CLI) : Pending
2. Define new resource APIs by adding Custom Resource Definitions(CRD)
3. Define Controllers to watch and reconcile resources
4. Write the reconciling logic for your Controller using the SDK and ktor APIs
5. Use the SDK CLI to build and generate the operator deployment manifests : Pending

## Prerequisites

- [git][git_tool]
- [docker][docker_tool] version 17.03+.
  - Alternatively [podman][podman_tool] `v1.2.0+` or [buildah][buildah_tool] `v1.7+`
- Access to a Kubernetes v1.12.0+ cluster.

## Quick Start

TODO

## Samples

To explore any operator samples built using the operator-sdk, see the [3k-operator-sdk-samples][samples].

## FAQ

For common Operator SDK related questions, see the [FAQ][faq].

## Contributing

See [CONTRIBUTING][contrib] for details on submitting patches and the contribution workflow.

See the [proposal docs][proposals_docs] and issues for ongoing or planned work.

## Reporting bugs

See [reporting bugs][bug_guide] for details about reporting any issues.

## License

Operator SDK is under Apache 2.0 license. See the [LICENSE][license_file] file for details.

[install_guide]: ./doc/user/install-operator-sdk.md
[operator_link]: https://coreos.com/operators/
[kubernetes_client_java_link]: https://github.com/kubernetes-client/java
[proposals_docs]: ./doc/proposals
[sdk_cli_ref]: ./doc/sdk-cli-reference.md
[guide]: ./doc/user-guide.md
[samples]: https://github.com/brvith/3k-operator-sdk/tree/master/sample-app
[of-home]: https://github.com/operator-framework
[of-blog]: https://www.ibm.com/cloud/blog/new-builders/being-frugal-with-kubernetes-operators
[contrib]: ./CONTRIBUTING.MD
[bug_guide]:./doc/dev/reporting_bugs.md
[license_file]:./LICENSE
[git_tool]:https://git-scm.com/downloads
[go_tool]:https://golang.org/dl/
[mercurial_tool]:https://www.mercurial-scm.org/downloads
[docker_tool]:https://docs.docker.com/install/
[podman_tool]:https://github.com/containers/libpod/blob/master/install.md
[buildah_tool]:https://github.com/containers/buildah/blob/master/install.md
[kubectl_tool]:https://kubernetes.io/docs/tasks/tools/install-kubectl/
[faq]: ./doc/faq.md
[getting_started]: https://github.com/operator-framework/getting-started/blob/master/README.md
