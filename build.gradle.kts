/* Build script version 3 */
import java.io.ByteArrayOutputStream
val CI = System.getenv("CI") !=null
var RELEASE_GIT_URL = "git@bitbucket.org:huyndx/jenkin_mutibranch_build_sample.git"
var BUILD_TIME = java.text.SimpleDateFormat("hh:mm aa dd/MM/yyyy").format(java.util.Date())
val BuildMess = getGitReleaseNote().replace(Regex("^(release: |beta: |alpha: |dev: )"), "")
val package_info = file("./package.json").takeIf { it.exists() }?.let {
    groovy.json.JsonSlurper().parseText(it.readText())
} as Map<*, *>?

var git_versioncode = getFromDisk()
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

fun getGitReleaseNote(): String {
    val outputText: String = ByteArrayOutputStream().use { outputStream ->
        project.exec {
            commandLine("git", "log", "-n", "1", "--pretty=format:%s%n%b")
            standardOutput = outputStream
        }
        outputStream.toString()
    }
    return outputText
}

fun InitRelease() {
    Clean()
    exec {
        commandLine = listOf("git", "clone", RELEASE_GIT_URL, "dist")
    }
    logger.info("Update npm source")
    delete("dist/public", "dist/views")
    delete("dist/app.js", "dist/package.json", "dist/release_note.txt")
    exec {
        commandLine = listOf("npm.cmd", "install", "--yes")
    }
}


fun FrontEnd() {
    logger.info("Doing build frontend!")
    exec {
        commandLine = listOf("webpack.cmd", "--mode", "production", "--config", "webpack.config.js")
    }
}

fun BackEnd() {
    logger.info("Doing build frontend!")
    exec {
        commandLine = listOf("webpack.cmd", "--mode", "production", "--config", "webpack.backend.js")
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
        commandLine = listOf("git", "commit", "-m", "\"[${if(CI)"CI" else "Manual"}:${git_versioncode}] ${BuildMess}\"")
    }
    exec {
        workingDir = File(".")
        commandLine = listOf("git", "push", "--all", "-f", "origin")
    }
}

fun Commit() {
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "add", ".")
    }
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "commit", "-m", "\"[${if(CI)"CI" else "Manual"}:${git_versioncode}] ${BuildMess}\"")
    }
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "tag", git_versioncode, "-m", "\"${BuildMess}\"")
    }
    exec {
        workingDir = File("./dist")
        commandLine = listOf("git", "push", "--all", "-f", "origin")
    }
}


fun createPackageInfo() {
    // Build package.json
    val json = package_info?.toMutableMap()!!
    val oldBuildNumber = (package_info?.get("build_number") ?: 1) as Int
    json.run {
        replace("version", git_versioncode)
        put("build_number", oldBuildNumber + 1)
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


fun getFromDisk(): String {
    val version = package_info?.get("version").toString()
    val oldBuildNumber = (package_info?.get("build_number") ?: 1) as Int
    var build = "${version}.${oldBuildNumber + 1}"
    val json = package_info?.toMutableMap()
    json?.let {
        if (it.containsKey("build_number")) {
            it.replace("build_number", oldBuildNumber + 1)
        } else {
            it.put("build_number", oldBuildNumber + 1)
        }
        if (it.containsKey("build")) {
            it.replace("build", build)
        } else {
            it.put("build", build)
        }
    }
    File("./package.json").writeText(
            groovy.json.JsonBuilder(json).toPrettyString(),
            java.nio.charset.Charset.forName("utf-8"))

    return build
}

fun writeReleaseNotes() {
    val json = package_info?.toMutableMap()!!
    var releaseNotes = ""
    if (json.containsKey("release_date") && json.containsKey("releases")) {
        val releases = json.get("releases") as Map<Any, Any>
        val releaseDate = json.get("release_date") as Map<Any, Any>
        val keys = releases.keys.reversed()
        for (k in keys) {
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
