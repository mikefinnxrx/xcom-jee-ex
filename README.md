# xcom-jee-ex : xerox.com barebones sample project for openshift

This is a sample project, stored in git, that can be built and deployed to OpenShift. It is based on the OpenShift project ("jee-ex"), found in OpenShift on github


## Deploying to OpenShift

### Deploying to OpenShift - CLI
Prerequisite: Working OpenShift/MiniShift command line environment, with openshift running

1. Navigate to correct project

   `$ oc project poc`
2. Create a new openshift app, using github repository as a basis

   `$ oc new-app https://github.com/mikefinnxrx/xcom-jee-ex  --image-stream="openshift/wildfly:latest" `
   
   a. Watch the build log via
   
   `$ oc logs -f bc/xcom-jee-ex`
   
   b. Check overall status
   
   `$ oc status`

3. Expose a route for the application (for the request inbound to openshift cluster to be routed to the correct service)

   `$ oc expose svc/xcom-jee/ex`
   
4. Open default web resource (index.html) in browser

   `$ ./minishift openshift service xcom-jee-ex --in-browser -n poc`
   
   
### Deploying to OpenShift - Web Console


### Deploying to OpenShift - Eclipse (Red Hat Developer Studio)


## Managing Changes
TBD