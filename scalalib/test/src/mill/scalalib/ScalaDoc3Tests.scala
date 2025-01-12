package mill.scalalib

import mill._
import utest._
import utest.framework.TestPath
import mill.testkit.UnitTester
import mill.testkit.TestBaseModule

object ScalaDoc3Tests extends TestSuite {
  // a project with static docs
  object StaticDocsModule extends TestBaseModule {
    object static extends ScalaModule {
      def scalaVersion = "3.0.0-RC1"
    }
  }

  // a project without static docs (i.e. only api docs, no markdown files)
  object EmptyDocsModule extends TestBaseModule {
    object empty extends ScalaModule {
      def scalaVersion = "3.0.0-RC1"
    }
  }

  // a project with multiple static doc folders
  object MultiDocsModule extends TestBaseModule {
    object multidocs extends ScalaModule {
      def scalaVersion = "3.0.0-RC1"
      def docResources = T.sources(
        millSourcePath / "docs1",
        millSourcePath / "docs2"
      )
    }
  }

  val resourcePath = os.Path(sys.env("MILL_TEST_RESOURCE_FOLDER")) / "scaladoc3"

  def tests: Tests = Tests {
    test("static") {
      val eval = UnitTester(StaticDocsModule, resourcePath)
      val Right(_) = eval.apply(StaticDocsModule.static.docJar)
      val dest = eval.outPath / "static" / "docJar.dest"
      assert(
        os.exists(dest / "out.jar"), // final jar should exist
        // check if extra markdown files have been included and translated to html
        os.exists(dest / "javadoc" / "index.html"),
        os.exists(dest / "javadoc" / "nested" / "extra.html"),
        // also check that API docs have been generated
        os.exists(dest / "javadoc" / "api" / "pkg" / "SomeClass.html")
      )
    }
    test("empty") {
      val eval = UnitTester(EmptyDocsModule, resourcePath)
      val Right(_) = eval.apply(EmptyDocsModule.empty.docJar)
      val dest = eval.outPath / "empty" / "docJar.dest"
      assert(
        os.exists(dest / "out.jar"),
        os.exists(dest / "javadoc" / "api" / "pkg" / "SomeClass.html")
      )
    }
    test("multiple") {
      val eval = UnitTester(MultiDocsModule, resourcePath)
      val Right(_) = eval.apply(MultiDocsModule.multidocs.docJar)
      val dest = eval.outPath / "multidocs" / "docJar.dest"
      assert(
        os.exists(dest / "out.jar"), // final jar should exist
        os.exists(dest / "javadoc" / "api" / "pkg" / "SomeClass.html"),
        os.exists(dest / "javadoc" / "index.html"),
        os.exists(dest / "javadoc" / "docs" / "nested" / "original.html"),
        os.exists(dest / "javadoc" / "docs" / "nested" / "extra.html"),
        // check that later doc sources overwrite earlier ones
        os.read(dest / "javadoc" / "index.html").contains("overwritten")
      )
    }
  }

}
