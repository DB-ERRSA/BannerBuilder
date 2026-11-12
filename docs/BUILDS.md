# Builds and releases

Java 21 is required to build **BannerBuilder**.

## Development builds

GitHub Actions runs the `Build BannerBuilder` workflow on pushes and pull requests targeting `main` or `master`. Successful builds upload a JAR artifact under **Actions** for 30 days. These are development builds, not published releases.

## Stable releases

1. Make sure the source version matches the release tag (`1.0-SNAPSHOT` is the version in the supplied archive).
2. Push a tag such as `v1.0.0` to the intended commit.
3. The `Release BannerBuilder` workflow builds from that tag, creates a GitHub Release, and attaches the deployable JAR and `SHA256SUMS.txt`.
4. The workflow also supports manual runs that upload a release candidate to Actions without publishing a release.

The download location for published versions is [GitHub Releases](https://github.com/DB-ERRSA/BannerBuilder/releases).

## Local build

```bash
mvn -B --no-transfer-progress clean verify
```

The deployable artifact comes from `target/BannerBuilder-*.jar`. Do not distribute `original-*`, source, javadoc, or plain JARs in its place.
