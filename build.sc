import mill._
import scalalib._
import scalafmt._
import publish._
import $file.common

import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion

object v {
  val scala = "2.13.10"
  val utest = ivy"com.lihaoyi::utest:0.8.1"
}

object cde extends CDE

trait CDE
  extends common.CDEModule
    with ScalafmtModule
    with CDEPublishModule {
  override def scalaVersion = v.scala
}

object cdetest extends CDETest

trait CDETest
  extends common.CDETestModule
    with ScalafmtModule {

  override def scalaVersion = v.scala

  override def millSourcePath = cde.millSourcePath / "tests"

  def cdeModule = cde

  def utestIvy = v.utest
}

trait CDEPublishModule extends PublishModule {
  def publishVersion = de.tobiasroeser.mill.vcs.version.VcsVersion.vcsState().format()

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "org.chipsalliance",
    url = "https://www.github.com/chipsalliance/cde",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("chipsalliance", "cde"),
    developers = Seq(
      Developer("terpstra", "Wesley W. Terpstra", "https://github.com/terpstra")
    )
  )
  // TODO: wait Chisel has mill-based release flow, let's copy&paste from it.
}
