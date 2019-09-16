import mill._
import mill.scalalib._
import mill.scalalib.publish._
import ammonite.ops._

object config extends config

class config extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.8"
  def millSourcePath = pwd / up / up / 'design / 'craft
  def publishVersion = "1.0"
  def pomSettings = PomSettings(
    description = "A Scala library for Context-Dependent Evironments",
    organization = "org.chipsalliance",
    url = "https://github.com/chipsalliance/api-config-chipsalliance",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("chipsalliance", "api-config-chipsalliance"),
    developers = Seq()
  )
}
