## Jenkin mutibranch build sample ##

****************************************
 
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

Jenkin Config:

```
Trigger:
  https://build.vnapps.com/generic-webhook-trigger/invoke?token=


Github Release 
Variable: ref
Expression: $.ref -> jsonpath 
  
Filter
Expression: ^(refs/heads/release)$
Text: $ref  

```

Bitbucket:
```
    Add user: huyndx@gmail.com(Jenkins Server) access for read/write package
```
