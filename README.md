## Jenkins Prefix Message build sample ##

****************************************
 Build with message prefix style
****************************************

Nodejs WebProject
```
Require:
 Gradle >= 7.3.3
 NodeJs >= 16
 JDK >= 11
 npm
 webpack-cli

How to trigger automation build:  
   Commit message begin with:
    release: ... Create release package
    dev: ... Create dev package
    beta: ... Create beta package
     
```

Jenkins Config:

```
Trigger:
  https://build.vnapps.com/generic-webhook-trigger/invoke?token=


Github Release 
Variable: mess
Expression: $.commits[0].message -> jsonpath 
  
Filter
Expression: ^(beta: |alpha: |dev: |release: )$
Text: $mess  

```

Bitbucket:
```
    Add user: ... (Jenkins Server) access for read/write package
```
