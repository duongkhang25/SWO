# Using a technical user for Jenkins:

_By using this option you will get static credentials (access key and secret key) that you will use to access your CRP 2.0 cluster. The advantage of this method is that it will work with other services than CRP 2.0 as well. The disadvantage is that you cannot create or manage the user yourself, but by requesting support from the SCC+ team_

_You don't need to create a policy, a role is enough for this use-case._

### Prerequire

**Configure the automation role of the cluster**

Update the automation_role parameter in the IaC template of your cluster. This parameter takes the name of an IAM Role that must exist in the target account the cluster is deployed to. If you e.g. provide a role name my-automation-role for a cluster deployed to account 123456789012, the Role ARN arn:aws:iam::123456789012:role/my-automation-role will be mapped in the cluster. The role will always be mapped to system:masters Group and therefore have full administrative privileges on the cluster.

dedicated-eks-cluster.yaml

```apiVersion: composite.adp.allianz.io/v1alpha1
kind: EksCluster
metadata:
  name: my-eks-cluster  # Specify the name of the EKS cluster Kubernetes resources
  namespace: customer  # This namespace is fixed
spec:
  # If you want to use a dedicated team for this cluster, you need to specify the name of the team.
  # team:
  #   name: team-123
  cluster:
    name: my-cluster-name  # Specify the name of the EKS cluster
    deploymentLocation: aws_ec1  # Specify the name of the EKS cluster deployment location
    serviceAdminEmail: admin@allianz.de  # Specify the service admin email
    serviceParameters:
      awsAccountId: "123456789012"  # Specify the AWS account ID
      version: "8.0.0"  # Specify CRP2.0 release version - If not specify, default version is given
    .....
    automationRole: my-automation-role  # Specify the name of existing IAM role name from target AWS account to be used for automation. Support only CRP2.0 release 8.0.0 and above - Optional
    advancedParameters:  # Specify advance service parameter
      bringYourOwnAccount: "true"  # Specify true if Bring your own account is required - Optional
      assumeRoleFromAwsAccountId: "826598240899"  # Specify AWS account id for assume role. Required if bring_your_own_account is set to true
      ....
```
**Test connectivity**

1. Create Jenkins credentials for your TU. In this example we will use a plain Kubernetes secret:

```apiVersion: v1
kind: Secret
metadata:
  # this is the jenkins credentials id.
  name: "tu-aws-credentials"
  labels:
    "jenkins.io/credentials-type": "aws"
  annotations:
    "jenkins.io/credentials-description": "Technical user to access CRP 2.0"
type: Opaque
stringData:
  accessKey: myTUAWSAccessKey
  secretKey: myTUAWSSecretKey
```
2. Create a Jenkins pipeline to access your cluster:

> Using the test_credential.groovy file