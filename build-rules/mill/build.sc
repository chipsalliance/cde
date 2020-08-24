import mill._
import mill.scalalib._
import mill.scalalib.publish._

object config extends config

class config extends ScalaModule {
  def scalaVersion = "2.12.12"
  def millSourcePath = os.pwd / os.up / os.up / 'design / 'craft
}
