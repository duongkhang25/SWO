**The Structure of the HelmChart:**

```
einvoice-helmrepo​/​
|-- templates/​
|    |-- configmap.yaml​
|    |-- deployment.yaml​
|    |-- hpa.yaml​
|    |-- ingress.yaml​
|    |-- service.yaml​
|-- chart.yaml​
|-- values.yaml
```

Structure of application repo:

```
einvoice-service-name/
|-- target/
| |-- *.jar
|-- Certs/
| |-- cacerts.cert
|-- *.dockerfile
|-- Jenkins/
| |-- CI/
| | |-- jenkinsfile
| |-- CD/
| | |-- jenkinsfile
|-- manifest/
| |-- service-name-values-qa.yaml
| |-- service-name-values-prod.yaml
|-- configmap/
| |-- service-name-configmap-qa.yaml
| |-- service-name-configmap-prod.yaml
```

# Description

1. Developer: Pushes code changes to the Git repository.
2. Git Repository: Hosts the source code for the application and the Helm chart.
3. Jenkins Server: Jenkins server polls the Git repository for changes.
4. CI Pipeline Starts: When changes are detected, the CI pipeline starts.
5. Pull Helm Chart Repo: Jenkins pulls the Helm chart repository with the structure of helm
6. Pull Application Repos: Jenkins pulls the application repositories with the structure
7. Determine Environment (QA/PROD): Jenkins determines the target environment.
8. Replace Config Files:
  * Jenkins replaces application.yaml in the Helm chart repo by the content of -applicationrepo/config/audit-service-configmap-qa.yaml from the application repo.
9. Use Environment-specific Values File:
  * Jenkins uses manifest/service-name-values-<env>.yaml in the Helm upgrade command.
10. Deploy


# How to Deploy Specific Services

- To deploy only specific services, you can use a custom values file or the --set option with Helm.

**QA Env**
```
helm upgrade --install  einvoice-helmrepo ./einvoice-helmrepo --namespace imobill \
  --set microservices.bill-gate.enabled=true \
  -f ./einvoice-bill-gate/manifest/einvoice-bill-gate-value-qa.yaml \
  --set-file global.applicationYml=/Users/xuna/Desktop/SWO/einvoice-bill-gate/configmap/einvoice-bill-gate-configmap-qa.yml \
  --set global.imageTag=10.0.0   \
  --set global.hpa.enabled=true \
  --debug
```

**PROD Env**
```
helm template  einvoice-helmrepo ./einvoice-helmrepo --namespace imobill \
  --set microservices.bill-gate.enabled=true \
  --set microservices.einvoice-audit-service.enabled=true \
  -f ./einvoice-bill-gate/manifest/einvoice-bill-gate-value-prod.yaml \
  -f ./einvoice-helmrepo/prod-values.yaml \
  --set-file global.applicationYml=/Users/xuna/Desktop/SWO/einvoice-bill-gate/configmap/einvoice-bill-gate-configmap-prod.yml \
  --set global.imageTag=10.0.0   \
  --set global.hpa.enabled=true \
  --debug
```

# To create secret for cert and key use for ingress https forward

```
kubectl create secret tls my-tls-secret --cert=path/to/tls.crt --key=path/to/tls.key -n imobill
```

# Create the secret for image pull secret from registry ECR:

```
aws ecr get-login-password --region <region> | kubectl create secret docker-registry my-registry-key --docker-server=<aws_account_id>.dkr.ecr.<region>.amazonaws.com --docker-username=AWS --docker-password-stdin
```

# Set up AWS Load Balancer Controller

## Prerequisite
## Steps

### 1. set up Ingress Controller (AWS Load Balancer Controller)
### 2. config Ingress Resource for Java backend và React frontend
### 3. config services and deployments

1. **set up AWS Load Balancer Controller**
Create IAM policy:

```
url -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json

aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json
```

Create IAM Role and Policy attach

```
eksctl create iamserviceaccount \
  --cluster <your-cluster-name> \
  --namespace kube-system \
  --name aws-load-balancer-controller \
  --attach-policy-arn arn:aws:iam::<your-account-id>:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve
```

Setting AWS Load Balancer Controller

```
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm upgrade --install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<your-cluster-name> \
  --set serviceAccount.create=false \
  --set region=<your-region> \
  --set vpcId=<your-vpc-id> \
  --set serviceAccount.name=aws-load-balancer-controller
```

The Traffic Flow:

                                      +---------------------------+
                                      |                           |
                                      |        Internet           |
                                      |                           |
                                      +-----------+---------------+
                                                  |
                                                  |
                                      +-----------v---------------+
                                      |                           |
                                      |          AWS NLB          |
                                      |                           |
                                      +-----------+---------------+
                                                  |
                                                  |
                                      +-----------v---------------+
                                      |                           |
                                      |  AWS Load Balancer        |
                                      |      Controller           |
                                      |                           |
                                      +-----------+---------------+
                                                  |
                                  +---------------+-------------------+
                                  |                                   |
                    +-------------v-------------+       +-------------v-------------+
                    |                           |       |                           |
                    |      Ingress Resource     |       |    Ingress Resource       |
                    |   (Java Backend Services) |       |  (React Frontend Service) |
                    |                           |       |                           |
                    +-------------+-------------+       +-------------+-------------+
                                  |                                   |
                                  |                                   |
            +---------------------v---------------------+ +-----------v-----------+
            |                                           | |                       |
            |  Service: audit-service                   | |  Service: react-frontend
            |  Service: bill-gate                       | |                       |
            |  Service: category-service                | +-----------+-----------+
            |  Service: identity-service                |             |
            |  Service: increment-service               |             |
            |  Service: integration-service             |             |
            |  Service: invoice-service                 |             |
            |  Service: notification-service            |             |
            |  Service: pre-sign-service                |             |
            |  Service: receive-gate                    |             |
            |  Service: report-service                  |             |
            |  Service: searchstatusirbm-service        |             |
            |  Service: send-gate                       |             |
            |  Service: sign-service                    |             |
            |  Service: xml-service                     |             |
            |                                           |             |
            +---------------------+---------------------+             |
                                  |                                   |
                                  |                                   |
                        +---------v---------+                         |
                        |                   |                         |
                        |   ConfigMaps      |                         |
                        |  (application-qa) |                         |
                        |  (application-prod)|                        |
                        +-------------------+                         |
                                                                      |
                                                              +-------v-------+
                                                              |               |
                                                              |  ConfigMaps   |
                                                              | (frontend)    |
                                                              +---------------+

```
graph TD;
    A[Developer Pushes Code] -->|1. Code Push| B[Git Repository]
    B -->|2. Jenkins Polls Repository| C[Jenkins Server]
    C -->|3. Jenkins Pipeline Starts| D[Checkout Helm Charts]
    D --> E[Get Image Tag from CI]
    E --> F[Prepare Helm Configuration]
    F --> G[Run Helm Upgrade]
    G --> H[Helm Client]
    H -->|4. Interacts with| I[Kubernetes API Server]
    I -->|5. Applies Changes| J[EKS Cluster]
    J -->|6. Deploys/Upgrades Resources| K[Pods/Services/Ingresses]

    subgraph Helm Configuration
        F1[Override Image Tag]
        F2[Apply Environment Config (the manifest/service-name-values-qa.yaml )]
        F3[Override application.yml config]
        F4[Generate Final Config]
    end

    G --> F1
    G --> F2
    G --> F3
    G --> F4
    F1 --> F4
    F2 --> F4
    F3 --> F4
```
