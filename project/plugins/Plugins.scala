import sbt._;

/**
 * Project plugins definitions
 */
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

    /**
     * Import the SBT Intellij plugin so one can run
     * <b>sbt update idea</b> to regenerate the Intellij project files.
     */
    val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
    val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.2.0"
}