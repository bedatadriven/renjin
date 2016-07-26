

/**
 * Initializes a SVN repository in a subdirectory called ".upstream" of the current 
 * directory.
 *
 * @param upstreamPath the prefix of the repo to checkout. For example, "src/nmath"
 */
def initRepo(String upstreamPath) {
    
    def TRUNK = "https://svn.r-project.org/R/trunk/"
    
    File svnRoot = new File(".upstream")
    if (!svnRoot.exists()) {
        println("Checking out SVN")
        "svn co ${TRUNK}/${upstreamPath} ${svnRoot.name}".execute().waitFor()
    } else {
        println("Updating SVN")
        def exitCode = new ProcessBuilder()
                .command("svn", "up")
                .directory(svnRoot)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("svn up failed.")
        }
    }
    return svnRoot
}

/**
 * Queries all the revision numbers affecting the checked out repository
 *
 * @param svnRoot path to the svn repository
 * @return a list of revision numbers
 */
def queryRevisions(File svnRoot) {
    def output = new ProcessBuilder()
            .command("svn", "log")
            .directory(svnRoot)
            .start()
            .text


    def revPattern = /r(\d+) \| ([^|]+) \| ([^|(]+) \(.*\) \|.*/
    def sepPattern = /^-+$/
    def revisions = []
    def revision = null

    output.eachLine { line ->
        if (revision == null) {
            def matcher = (line =~ revPattern)
            if (matcher.matches()) {
                revision = [:]
                revision.author = matcher.group(2)
                revision.date = matcher.group(3)
                revision.number = Integer.parseInt(matcher.group(1))
                revision.message = ""
            }
        } else if (revision != null) {
            if (line.matches(sepPattern)) {
                revisions.add(revision)
                revision = null
            } else {
                revision.message += line + "\n"
            }
        }
    }
    revisions.reverse()
}

def readCurrentRevision() {
    
    def output = new ProcessBuilder()
            .command("git", "log", ".")
            .start()
            .text

    def lines = output.readLines()
    def pattern = /\s*svn-rev (\d+)\s*/
    
    for(def i = 0; i < lines.size(); ++i) {
        def matcher = (lines[i] =~ pattern);
        if(matcher.matches()) {
            return Integer.parseInt(matcher.group(1))
        }
    }
}


def executeGit(String... args) {
    
    def commandLine = []
    commandLine.add("git")
    commandLine.addAll(args)
    
    println commandLine.join(' ')
    
    def exitCode = new ProcessBuilder()
        .command(commandLine)
        .redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor();
        
    if(exitCode != 0) {
        throw new RuntimeException("FAILED: " + commandLine.join(' '))
    }
}

/**
 * Creates a patch file for a given SVN revision
 * @param svnRoot
 * @param previousRev
 * @param rev
 * @return
 */
def revisionPatch(File svnRoot, int rev) {
    File patchFile = new File("${rev}.patch")
    def exitCode = new ProcessBuilder()
            .command("svn", "diff", "-r${rev - 1}:${rev}")
            .redirectOutput(patchFile)
            .directory(svnRoot)
            .start()
            .waitFor();

    if (exitCode != 0) {
        throw new RuntimeException("Failed to create patch for r${rev}")
    }
    return patchFile;
}

def relativeToRepoRoot(File dir) {
    def absPath = dir.absolutePath;
    def repoStart = absPath.indexOf('/renjin/') + '/renjin/'.length();
    absPath.substring(repoStart)
}

def applyPatch(Map config, File patchFile, File dir) {
    
    List<String> args = ['git', 'am', '-p0']
    args.add("--directory=${relativeToRepoRoot(dir)}".toString())
    args.add('--ignore-whitespace')
    args.add('--ignore-space-change')
    
    if(config["exclude"]) {
        args.add("--exclude=${config['exclude']}".toString())
    }
    
    println args
    
    def exitCode = new ProcessBuilder(args)
            .redirectInput(patchFile)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor();

    return exitCode == 0

}

def gitAuthor(svnAuthor) {

    def AUTHORS = [

            "ihaka": "Ross Ihaka <r.ihaka@auckland.ac.nz>",
            "rgentlem": "Robert Gentleman <r.gentleman@auckland.ac.nz>",
            "maechler": "Martin Mächler <maechler@stat.math.ethz.ch>",
            "bates": "Douglas Bates <bates@stat.wisc.edu>",
            "hornik": "Kurt Hornik <Kurt.Hornik@wu.ac.at>",
            "thomas": "Thomas Lumley <t.lumley@auckland.ac.nz>",
            "tlumley": "Thomas Lumley <t.lumley@auckland.ac.nz>",
            "pd": "Peter Dalgraad <pd.mes@cbs.dk>",
            "ripley": "Brian Ripley <ripley@stats.ox.ac.uk>",
            "luke": "Luke Tierney <luke-tierney@uiowa.edu>",
            "martyn": "Martyn Plummer <martyn@r-project.org>",
            "plummer": "Martyn Plummer <martyn@r-project.org>",
            "murdoch": "Duncan Murdoch <murdoch.duncan@gmail.com>",
            "duncan": "Duncan Murdoch <murdoch.duncan@gmail.com>",
            "urbaneks": "Simon Urbanek <urbanek@research.att>",
    ]

    if(AUTHORS.containsKey(svnAuthor)) {
        return AUTHORS[svnAuthor];
    } else {
        throw new RuntimeException("No author entry for ${svnAuthor}")
    }
}

def applyRevision(Map config, File svnRoot, rev) {

    println "Applying revision r${rev.number}..."

    File localRoot = new File(config["localPrefix"])
    if(!localRoot.exists()) {
        localRoot.mkdirs()
    }

    File mailFile = new File("${rev.number}.msg")
    if(!mailFile.exists()) {

        println "...Creating patch..."
        File patchFile = revisionPatch(svnRoot, rev.number)

        def author = gitAuthor(rev.author)
        def messageLines = rev.message.trim().readLines()
        def subject = messageLines[0]
        def body = messageLines.subList(1, messageLines.size()).join("\n")

        mailFile.withWriter {
            it.append("From: ${author}\n")
            it.append("Date: ${rev.date}\n")
            it.append("Subject: ${subject}\n\n")
            it.append(body)
            it.append("\n\nsvn-rev ${rev.number}\n")
            it.append(patchFile.text)
        }
        patchFile.delete()
    }

    println "...Applying patch..."
    def success = applyPatch(config, mailFile, localRoot)


    if(!success) {
        System.exit(-1)
    }

    mailFile.delete()
}

/**
 *
 * @param upstreamPath
 * @param localPrefix
 */
return { config ->
    
    def svnRoot = initRepo(config["upstreamPrefix"])
    def revisions = queryRevisions(svnRoot)
    def currentRevision = readCurrentRevision()
    
    for(def rev in revisions) {
        gitAuthor(rev.author)
    }
    
    for(def rev in revisions) {
        
        if (rev.number > currentRevision) {
            applyRevision(config, svnRoot, rev)
        }
    }
}
