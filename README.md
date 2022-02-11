## Jenkin mutibranch build sample ##
Nodejs WebProject
```
Require:
 Gradle >= 7.3.3
 NodeJs >= 16
 JDK >= 11
 npm
 webpack-cli

How to dev?
    Checkout dev branch
    Change code
    
How to release?    
    Checkout release branch
    Merge code from dev
    Push
     
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
