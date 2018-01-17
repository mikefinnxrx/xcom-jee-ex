# xcom-jee-ex : xerox.com barebones sample project for openshift

This is a sample project, stored in git, that can be built and deployed to OpenShift. It is based on the OpenShift project ("jee-ex"), found in OpenShift on github


## Deploying to OpenShift

Prerequisite: Working OpenShift/MiniShift command line environment, with openshift running

### Deploying existing app to OpenShift - CLI


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
   
   
### Deploying existing app to OpenShift - Web Console

1. Open web console in browser

2. Select the Project to which you want to add the application (drop-down to the right of "Projects").

   a. If you want to create a new project, select "View all projects" from the drop-down, and click on New Project
   
3. Click the "Add to project" drop-down and click "Browse catalog"

4. Click the "Java" tile

5. Select latest Wildfly from drop-down and click "Select"

6. Fill out the application info:

   a. Name: xcom-jee-ex
   
   b. Git Repository URL: https://github.com/mikefinnxrx/xcom-jee-ex

7. Click "Create"

8. You will see an "Application created" screen. Click "Continue to overview"

9. Check the status of the build. When it's finished, you will see an Application entry for it, a running pod (far right), and the access URL

10. Open URL in browser. There will be a default index.html. From there, you can see a few links to be used for demo purposes.

11. Bask in the glory, openshift stud!

### Deploying to OpenShift - Eclipse (Red Hat Developer Studio)


## Managing Changes


## Webhooks