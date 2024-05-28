- Use the service account as TU for Jenkins
- Create TU Jenkins credentials

Step 1: Create a Kubernetes Service Account

    1.Create a Service Account:

    Define a YAML file for the service account (jenkins-service-account.yaml):

    ```apiVersion: v1
    kind: ServiceAccount
    metadata:
    name: jenkins
    namespace: jenkins
    ```

    Apply the service account:

    ```kubectl apply -f jenkins-service-account.yaml
    ```

Step 2: Create a ClusterRole and ClusterRoleBinding

   1. Create a ClusterRole:

        Define a YAML file for the ClusterRole (jenkins-clusterrole.yaml):

```apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
name: jenkins
rules:
- apiGroups: [""]
resources: ["pods", "services", "endpoints", "persistentvolumeclaims", "events", "configmaps", "secrets"]
verbs: ["get", "list", "watch", "create", "delete", "patch", "update"]
- apiGroups: ["apps"]
resources: ["deployments", "replicasets", "statefulsets"]
verbs: ["get", "list", "watch", "create", "delete", "patch", "update"]
- apiGroups: ["batch"]
resources: ["jobs", "cronjobs"]
verbs: ["get", "list", "watch", "create", "delete", "patch", "update"]
```

    2.Apply the ClusterRole:

    ```kubectl apply -f jenkins-clusterrole.yaml
```

    3. Create a ClusterRoleBinding:

        Define a YAML file for the ClusterRoleBinding (jenkins-clusterrolebinding.yaml):

```apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: jenkins
subjects:
- kind: ServiceAccount
  name: jenkins
  namespace: jenkins
roleRef:
  kind: ClusterRole
  name: jenkins
  apiGroup: rbac.authorization.k8s.io
```

```kubectl apply -f jenkins-clusterrolebinding.yaml```

Step 3: Retrieve the Service Account Token

    1. Get the Service Account Secret Name:

```kubectl get sa jenkins -n jenkins -o jsonpath="{.secrets[0].name}"
```

    2. Get the Token from the Secret:

```kubectl get secret <secret-name> -n jenkins -o jsonpath="{.data.token}" | base64 --decode
```

    Step 4:  Add Kubernetes Credentials to Jenkins & Configure Jenkins to Use the Kubernetes Credentials
