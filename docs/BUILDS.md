# Builds

```powershell
mvn clean package
```

Deployable Maven JAR: `target/BannerBuilder-1.0-SNAPSHOT.jar` in the supplied source archive. The plugin reports version `1.0.0` in `plugin.yml`; align the Maven artifact version and plugin version before tagging a public release.

- [Releases](https://github.com/DB-ERRSA/BannerBuilder/releases)
- [Actions](https://github.com/DB-ERRSA/BannerBuilder/actions)

Attach the deployable JAR to a GitHub Release; repository ZIP downloads are source code, not plugin builds.
