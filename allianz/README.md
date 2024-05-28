The Structure of the HelmChart:

allianz/​
|-- templates/​
|    |-- configmap.yaml​
|    |-- deployment.yaml​
|    |-- hpa.yaml​
|    |-- ingress.yaml​
|    |-- service.yaml​
|-- chart.yaml​
|-- values.yaml


the ${IMAGE_TAG} will get from CI successfull flow

'Deploy to QA'
        sh 'helm upgrade --install allianz ./allianz --namespace imobill --set global.imageTag=${IMAGE_TAG} --set global.environment=qa'

'Deploy to PROD'
        sh 'helm upgrade --install java-microservices-prod ./allianz --namespace imobill --set global.imageTag=${IMAGE_TAG} --set global.environment=prod'



# Set up AWS Load Balancer Controller

## Prerequisite
## Steps

### 1. set up Ingress Controller (AWS Load Balancer Controller)
### 2. config Ingress Resource for Java backend và React frontend
### 3. config services and deployments

1. **set up AWS Load Balancer Controller**
Create IAM policy:

```curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json

aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json
```

Create IAM Role and Policy attach

```eksctl create iamserviceaccount \
  --cluster <your-cluster-name> \
  --namespace kube-system \
  --name aws-load-balancer-controller \
  --attach-policy-arn arn:aws:iam::<your-account-id>:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve
```

Setting AWS Load Balancer Controller

```helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm upgrade --install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<your-cluster-name> \
  --set serviceAccount.create=false \
  --set region=<your-region> \
  --set vpcId=<your-vpc-id> \
  --set serviceAccount.name=aws-load-balancer-controller
```

Upgrade the helmChart:

helm upgrade --install allianz ./allianz --namespace imobill

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
