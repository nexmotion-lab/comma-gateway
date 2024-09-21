docker_build('sunwoo3856/gateway', '/docker')

k8s_yaml(kustomize('k8s/overlays/development'))

k8s_resource('gateway', port_forwards=['9000'])