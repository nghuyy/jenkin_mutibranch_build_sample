/* Build script version 3 */
import java.lang.Integer.parseInt
import java.io.ByteArrayOutputStream
var RELEASE_GIT_URL = "git@bitbucket.org:huyndx/jenkin_mutibranch_build_sample.git"
var GIT_BRANCH = "master"
var PRE_VERSION = 7
var BUILD = 38
if(System.getenv("BUILD_NUMBER") != null){
    BUILD = parseInt(System.getenv("BUILD_NUMBER")) + PRE_VERSION
}

var BUILD_TIME = java.text.SimpleDateFormat("hh:mm aa dd/MM/yyyy").format(java.util.Date())
val BuildMess = getGitReleaseNote().replace(Regex("^(release: |beta: |alpha: |dev: )"),"")
val package_info = file("./package.json").takeIf { it.exists() }?.let {
    groovy.json.JsonSlurper().parseText(it.readText())
} as Map<*, *>?

var git_versioncode = getFromPackage()
println(git_versioncode)
println("-->  $BuildMess")
/***********************************************************/
task("Clean") {
    doLast {
        Clean()
    }
}
task("Release") {
    doLast {
        Clean()
        InitRelease()
        Build()
        writeReleaseNotes()
        CommitSource()
        Commit()
    }
}
task("Build") {
    doLast {
        Build()
    }
}
/***********************************************************/
fun Clean() { //clean project
    delete("dist")
    delete("docs")
    delete("public")
}

fun getGitReleaseNote():String{
    val outputText: String = ByteArrayOutputStream().use { outputStream ->
        project.exec {
            commandLine("git", "log", "-n", "1" ,"--pretty=format:%s%n%b")
            standardOutput = outputStream
        }
        outputStream.toString()
    }
    return outputText
}
fun InitRelease() {
        Clean()
        exec {
            commandLine = listOf("git", "clone", RELEASE_GIT_URL , "dist")
        }
        logger.info("Update npm source")
        delete ("dist/public","dist/views")
        delete ("dist/app.js","dist/package.json","dist/release_note.txt")
        exec {
            commandLine = listOf("npm.cmd", "install", "--yes")
        }
}




fun FrontEnd() {
    logger.info("Doing build frontend!")
    exec {
        commandLine = listOf("webpack.cmd", "--mode", "production", "--config","webpack.config.js")
    }
}

fun BackEnd() {
    logger.info("Doing build frontend!")
    exec {
        commandLine = listOf("webpack.cmd", "--mode", "production", "--config","webpack.backend.js")
    }
}

fun Build() {
    FrontEnd()
    BackEnd()
    createPackageInfo()
}

fun CommitSource() {
    exec {
        workingDir = File(".")
        commandLine = listOf("git", "add", ".")
    }
    exec {
        workingDir = File(".")
        commandLine = listOf("git", "commit", "-m", "\"${git_versioncode}: ${BuildMess}\"")
    }
    exec {
        workingDir = File(".")
        commandLine = listOf("git", "push", "-all", "-f", "origin")
    }
}

fun Commit() {
    logger.info("Deploy release binary!")
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "add", ".")
    }
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "commit", "-m", "\"${git_versioncode}: ${BuildMess}\"")
    }
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "tag", git_versioncode, "-m", "\"${BuildMess}\"")
    }
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "push", "-f", "origin", GIT_BRANCH, "--tags")
    }
}


fun createPackageInfo() {
    // Build package.json
    val json = package_info?.toMutableMap()!!
    json.run {
        replace("version", git_versioncode)
        put("build", "1")
        put("time", BUILD_TIME)
        put("release_note", BuildMess)
        remove("devDependencies")
        remove("repository")
        json
    }

    File("./dist/package.json").writeText(
            groovy.json.JsonBuilder(json).toPrettyString(),
            java.nio.charset.Charset.forName("utf-8"))

    // Build release.json
    val releaseNote = mutableMapOf<Any, Any>()
    releaseNote.run {
        put("version", git_versioncode)
        put("time", BUILD_TIME)
        put("release_note", "${BuildMess}")
        releaseNote
    }
    File("./dist/release.json").writeText(
            groovy.json.JsonBuilder(releaseNote).toPrettyString(),
            java.nio.charset.Charset.forName("utf-8"))
}

fun getVersionCode(): String {
    try {
        val code = java.io.ByteArrayOutputStream()
        exec {
            workingDir = File("./")
            commandLine = listOf("git", "describe", "--tags", "--abbrev=0")
            standardOutput = code
        }
        return code.toString().trim()
    } catch (ignored: java.lang.Exception) {
        return ""
    }
}

fun getFromPackage(): String {
    val version = package_info?.get("version").toString()
    var version_code = "${version}.${BUILD}"
    val json = package_info?.toMutableMap()!!
    if(json.containsKey("releases")) {
        val releases = json.get("releases") as MutableMap<Any,Any>
        if(BuildMess.equals("") || BuildMess.equals(" ") || BuildMess == null){}else {
            releases.values.removeIf{
                it == BuildMess
            }
            releases[version_code] = "$BuildMess"
            json["releases"] = releases
            var releaseDate = mutableMapOf<Any, Any>()
            if (json.containsKey("release_date")) {
                releaseDate = json.get("release_date") as MutableMap<Any, Any>
            }
            releaseDate.put(version_code, BUILD_TIME)
            json.put("release_date", releaseDate)
        }
    }else{
        if(BuildMess.equals("") || BuildMess.equals(" ") || BuildMess == null) {}else{
            val releases = mutableMapOf<Any, Any>()
            releases.put(version_code, BuildMess)
            json.put("releases", releases)
            val releaseDate = mutableMapOf<Any, Any>()
            releaseDate.put(version_code, BUILD_TIME)
            json.put("release_date", releaseDate)
        }
    }
   
   json.run {
            replace("build_number", BUILD)
            replace("build_code", version_code)
        }
   
   File("./package.json").writeText(
                groovy.json.JsonBuilder(json).toPrettyString(),
                java.nio.charset.Charset.forName("utf-8"))
    
    return version_code
}

fun writeReleaseNotes(){
    val json = package_info?.toMutableMap()!!
    var releaseNotes = ""
    if(json.containsKey("release_date") && json.containsKey("releases")) {
        val releases = json.get("releases") as Map<Any, Any>
        val releaseDate = json.get("release_date") as Map<Any, Any>
        val keys = releases.keys.reversed()
        for (k in keys){
            val v = releases[k]
            if (releaseDate.containsKey(k)) {
                val d = releaseDate.get(k)
                releaseNotes += "$k: $d\n$v\n"
            } else {
                releaseNotes += "$k: $v\n"
            }
        }
        File("./dist/release_note.txt").writeText(releaseNotes)
    }
}
