## Jenkins Prefix Message build sample ##

****************************************
 Build with message prefix style
****************************************

Nodejs WebProject
```
Require:
 Gradle >= 7.4
 NodeJs >= 16
 JDK >= 11
 npm
 webpack-cli

How to trigger automation build:  
   Commit message begin with:
    release: ... Create release package
    dev: ... Create dev package
    beta: ... Create beta package
    test: ... Create test package
     
```

Jenkins Config:

```
Trigger:
  https://example.com/generic-webhook-trigger/invoke?token=


Github Release 
Variable: mess
Expression Github: $.commits[0].message -> jsonpath 
Expression Bitbucket: $.push.changes[0].commits[0].message -> jsonpath 
  
Filter
Expression: ^(beta: |alpha: |dev: |release: |test: )
Text: $mess  

```

Bitbucket:
```
    Add user: ... (Jenkins Server) access for read/write package
```

**************************** Also use local with gradle binary *********************************

gradle Release
