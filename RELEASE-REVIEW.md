# Repository preparation: BannerBuilder

The Maven POM still uses `1.0-SNAPSHOT`, while the reported plugin version may differ. Set a release version before tagging.

## Tracked-file cleanup

The `.gitignore` prevents new IDE and build files from being committed, but **does not remove already tracked files**. After merging the workflow files, run from the repository root:

```bash
git rm -r --cached --ignore-unmatch .idea target build .gradle
```

This untracks generated files while leaving the local copies in place. Commit the resulting staged deletions. Never remove the active `src/` tree.
