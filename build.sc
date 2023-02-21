import mill._
import scalalib._
import scalafmt._
import publish._
import $file.common

import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.3.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion

object ivys {
  val utest = ivy"com.lihaoyi::utest:0.8.1"
}

object cde extends mill.Cross[cde]("2.12.17", "2.13.10")

class cde(val crossScalaVersion: String) extends common.CDEModule with CrossScalaModule with ScalafmtModule with PublishModule {
  object tests extends Tests with TestModule.Utest {
    override def ivyDeps = Agg(ivys.utest)
  }

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

  def sonatypeUri: String = "https://s01.oss.sonatype.org/service/local"
  def sonatypeSnapshotUri: String = "https://s01.oss.sonatype.org/content/repositories/snapshots"
  def githubPublish = T {
    os.proc("gpg", "--import", "--no-tty", "--batch", "--yes").call(stdin = java.util.Base64.getDecoder.decode(sys.env("PGP_SECRET").replace("\n", "")))
    publish(
      sonatypeCreds = s"${sys.env("SONATYPE_USERNAME")}:${sys.env("SONATYPE_PASSWORD")}",
      signed = true,
      gpgArgs = Seq(
        s"--passphrase=${sys.env("PGP_PASSPHRASE")}",
        "--no-tty",
        "--pinentry-mode", "loopback",
        "--batch",
        "--yes",
        "-a",
        "-b"
     )
   )
    
  }
}
