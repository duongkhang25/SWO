#!/usr/bin/env groovy

pipeline {
    agent {
        label "deployment-tools"
    }
    environment {
        CLUSTER_NAME = "adpk8s-name-prefix"
        NAMESPACE = "my-namespace"
        REGION = "eu-central-1"
        ACCOUNT = "123456789012"
        TU_CREDENTIALS_ID = "tu-aws-credentials"
        TU_IAM_ROLE = "my-automation-role"
    }
    stages {
        stage("Test") {
            steps {
                script {
                    withAWS(role: "${TU_IAM_ROLE}", roleAccount: "${ACCOUNT}",
                            region: "${REGION}", credentials: "${TU_CREDENTIALS_ID}") {
                        sh """
                        aws eks update-kubeconfig \
                        --region ${REGION} \
                        --name ${CLUSTER_NAME} \
                        --kubeconfig ./kube.conf
                        kubectl get pods --kubeconfig=kube.conf -n ${NAMESPACE}
                        """
                    }
                }
            }
        }
    }
}
