# sbt-spiewak

This plugin basically just exists to allow me to more conveniently setup my baseline SBT configuration, which has evolved somewhat over the years, and is also becoming quite unwieldy when solely represented in [giter8 format](https://github.com/djspiewak/base.g8). If you want to use this plugin with a *new* project, you should probably start from that template.

If you generally agree with my opinions on how projects should be set up, though, then this is probably a really excellent plugin to base on! Between this plugin and my giter8 template, you can get a new Scala project up and running and publishing to Sonatype within about five minutes. As an example, check out this quick screencast:

[![sbt-spiewak demo](https://img.youtube.com/vi/SjcMKHpY1WU/0.jpg)](https://www.youtube.com/watch?v=SjcMKHpY1WU)

TLDR, it's really really easy.

## Usage

Put one of the following into your `plugins.sbt`:

```sbt
// for stock functionality (no publication defaults)
addSbtPlugin("com.codecommit" % "sbt-spiewak" % "<version>")

// publishing to sonatype
addSbtPlugin("com.codecommit" % "sbt-spiewak-sonatype" % "<version>")
```

Then, in your `build.sbt`, make sure you set a value for `baseVersion`, `organization`, `publishGithubUser` and `publishFullName`:

```sbt
ThisBuild / organization := "com.codecommit"

ThisBuild / baseVersion  := "0.1"

ThisBuild / publishGithubUser := "djspiewak"
ThisBuild / publishFullName   := "Daniel Spiewak"
```

Or something like that.

If you have a multi-module build and need a subproject to *not* publish (as is commonly done with the `root` project), enable the `NoPublishPlugin`. For example:

```sbt
lazy val root = project
  .aggregate(core, sonatype)
  .in(file("."))
  .settings(name := "root")
  .enablePlugins(NoPublishPlugin)
```

## Release automatically with GitHub Actions CI

If you would like your releases to be published by your CI process in GitHub Actions, rather than locally, you will probably benefit from the `SonatypeCiReleasePlugin` plugin (part of sbt-spiewak-sonatype). This makes some assumptions about your secrets and general build configuration, applies the settings to the [sbt-github-actions](https://github.com/djspiewak/sbt-github-actions)-generated workflow, and generally takes care of everything for you.

To use, add the following to the root level of your build.sbt:

```sbt
enablePlugins(SonatypeCiReleasePlugin)
```

Then, generate the initial GitHub Actions in `.github/workflows` with the `githubWorkflowGenerate` SBT task (see [tasks for sbt-github-actions](https://github.com/djspiewak/sbt-github-actions#tasks)).

After that, configure the following encrypted secrets within GitHub Actions (go to _Settings_ -> _Secrets_ -> _New secret_; see [GitHub Encrypted Secrets documentation](https://docs.github.com/en/actions/reference/encrypted-secrets) for more detail):

- `SONATYPE_USERNAME`
- `SONATYPE_PASSWORD`
- `PGP_SECRET`

You can obtain the `PGP_SECRET` by running `gpg --export-secret-keys | base64`. Please make sure that this key is *not* password protected in the export (GitHub Actions itself will encrypt it).

Once this is done, decide whether you would like snapshots to be published on every master build. By default, releases will only be published on version tags. If you wish to override this, set the following:

```sbt
ThisBuild / spiewakCiReleaseSnapshots := true
```

If you do set the above to `true`, you should probably also make sure to set your primary branch as below:

```sbt
ThisBuild / spiewakMainBranches := Seq("main")
```

You can also simply add additional branches, if you're publishing snapshots on more than one at a time.

With all of these steps out of the way, you should have some nice, reliable, CI-driven releases going forward!

**Caveat:** If you *are* publishing snapshots on your primary branch, you will need to be careful to ensure that new tags are fully built on the primary branch *before* you push the tag to the upstream. The reason for this is the fact that tags are visible to the primary branch builds, meaning that if you push a new commit to master *and* a tag which points to it, that commit and tag will build *and release* under the same name simultaneously. The workaround is to push master, wait for the snapshot release to complete, and then push the tag. I'll get around to fixing this someday...

## Features

- Baseline plugin setup
  + coursier
  + sbt-travisci
  + sbt-git
    * With sane git versioning settings
    * Also with fixed `git-status` stuff
  + sbt-header
    * Assumes Apache 2.0 license
  + sbt-sonatype
    * With fixed snapshot publication URLs
  + sbt-gpg
  + sbt-mima
    * Infers previous versions by using git tags
    * Automatically runs on `ci` and `release`
  + sbt-explicit-dependencies
    * `unusedCompileDependenciesTest` runs on `ci`
    * disabled by `NoPublishPlugin`
    * filters Scala.js and Dotty standard libraries
- Sane scalac settings
  + Including `-Ybackend-parallelism` where supported
- SI-2712 fix across scala versions (dating back to 2.10)
- kind-projector (Scala 2 only)
- better-monadic-for (Scala 2 only)
- `release` and `ci` command aliases
  + Performs sonatype release steps
- `NowarnCompatPlugin`
  + Adds support for `@nowarn` to Scala 2.11, 2.12, and 2.13.1 via Silencer.
  + Adds scala-collection-compat to the classpath for versions that need Silencer. Opt out by configuring `nowarnCompatAnnotationProvider`.
- `SonatypeCiReleasePlugin`
  + Prescriptive defaults for projects which want CI releases

### Sonatype Requirements

You will need to define the following settings:

```sbt
ThisBuild / homepage := Some(url("https://github.com/djspiewak/sbt-spiewak")),

ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/djspiewak/sbt-spiewak"),
  "git@github.com:djspiewak/sbt-spiewak.git")))
```

## Defaults Which You May Wish to Override...

You may consider overriding any of the following keys, which are hard-coded to defaults that I believe are sane:

- `licenses` (defaults to Apache 2.0)
- `developers` (defaults to just yourself, using the `publishFullName` and `publishGithubUser`)
- `startYear` (defaults to 2020)
- `endYear` (defaults to empty; set this if you want a *range* of years for your copyright headers)
- `strictSemVer` (defaults to `true`)
  + When set to `true`, it disallows breaking binary compatibility in any release which does not increment the *major* component unless the major component is `0` (i.e. semantic versioning). Many Scala projects break binary compatibility in *minor* releases, such as Scala itself. This scheme is sometimes referred to as "scala ver". Setting `ThisBuild / strictSemVer := false` will relax the MiMa compatibility checks and allow you to perform such breakage if desired.
