### PreRequire

> Info: If you are using the Nginx ingress controller, you only need to update your ingress resource


> [!NOTE]
> EKS cluster is configured to use a sub-domain from the hosted zone you provided while ordering the cluster. If the name of the cluster you ordered is "my-k8s" and the domain name you provided during the order is "my-account.ec1.aws.aztec.cloud.allianz"
> The FQDN: It has format that *.**my-k8s.my-account.ec1.aws.aztec.cloud.allianz** (follow my-svc.my-namespace.svc.**cluster-domain.example**).  The FQDN should not exceed 64 characters to comply with x509 certificate limits for HTTPS.
> You will also need to obtain a PEM encoded x509 certificate and corresponding private key that matches your application FQDN to configure secure HTTPS

## Customn Domain:

- External customn domain: AWS will expose the Ingress to the LoadBalancer (ALB or NLB). If you would like to customn the external Domain for the organize like *.dci.dev.allianz.io. Need to create CNAME for the alias Loadbalancer from AWS.
- Internal customn domain: Also make the CNAME for the internal ingress eg. *.es-noprod.azs-eks-dt.ew3.aws.aztec.cloud.allianz
