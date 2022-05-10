import mill._
import scalalib._
import scalafmt._
import publish._

import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version_mill0.10:0.1.4`
import de.tobiasroeser.mill.vcs.version.VcsVersion

object ivys {
  val utest = ivy"com.lihaoyi::utest:0.7.10"
}

object cde extends mill.Cross[cde]("2.12.13", "2.13.6")

class cde(val crossScalaVersion: String) extends CrossScalaModule with ScalafmtModule with PublishModule {
  object tests extends Tests with TestModule.Utest {
    override def ivyDeps = Agg(ivys.utest)
  }

  def publishVersion = de.tobiasroeser.mill.vcs.version.VcsVersion.vcsState().format()

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "org.chipsalliance",
    url = "https://www.github.com/chipsalliance/api-config-chipsalliance",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("chipsalliance", "api-config-chipsalliance"),
    developers = Seq(
      Developer("terpstra", "Wesley W. Terpstra", "https://github.com/terpstra")
    )
  )

  def githubPublish = T {
    os.proc("gpg", "--import", "--no-tty", "--batch", "--yes").call(stdin = java.util.Base64.getDecoder.decode(sys.env("PGP_SECRET").replace("\n", "")))
    val PublishModule.PublishData(artifactInfo, artifacts) = publishArtifacts()
    new SonatypePublisher(
      sonatypeUri,
      sonatypeSnapshotUri,
      s"${sys.env("SONATYPE_USERNAME")}:${sys.env("SONATYPE_PASSWORD")}",
      true,
      Seq(
        s"--passphrase=${sys.env("PGP_PASSPHRASE")}",
        "--no-tty",
        "--pinentry-mode=loopback",
        "--batch",
        "--yes",
        "-a",
        "-b"
      ),
      60000,
      5000,
      T.log,
      120000,
      false
    ).publish(artifacts.map { case (a, b) => (a.path, b) }, artifactInfo, true)
  }
}
