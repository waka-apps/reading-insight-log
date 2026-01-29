# Mincronautの初回インストールドキュメント

- macOSにjava入れるところから、Mincronautの起動までの手順

## インストール手順

- java入れる
  ```bash
  $ brew install openjdk
  $ sudo ln -sfn $(brew --prefix)/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
  $ java -version
  openjdk version "25.0.2" 2026-01-20
  OpenJDK Runtime Environment Homebrew (build 25.0.2)
  OpenJDK 64-Bit Server VM Homebrew (build 25.0.2, mixed mode, sharing)
  ```
- インストール方法(CLI)
  ```bash
  $ brew install micronaut
  $ mn --version
  Micronaut Version: 4.10.7
  ```
- プロジェクト作成
  - 注意点
    - create-appに続くプロジェクト名にはディレクトリ構造を入れない( ❌services/api)
    - jdkはマシンのjava —versionに合わせる
  ```bash
  $ mkdir services
  $ mn create-app api \
    --lang=kotlin \
    --build=gradle_kotlin \
    --jdk=25 \
    --features=serialization-jackson,validation
  ```
- `./gradlew run` でエラーになる

  ```bash
  $ ./gradlew run
  Kotlin does not yet support 25 JDK target, falling back to Kotlin JVM_21 JVM target
  Kotlin does not yet support 25 JDK target, falling back to Kotlin JVM_21 JVM target

  > Task :kspKotlin
  Kotlin does not yet support 25 JDK target, falling back to Kotlin JVM_21 JVM target

  > Task :kspKotlin FAILED

  [Incubating] Problems report is available at: file:///Users/wakabayashitoshiyuu/develop/study/reading-insight-log/frontend/services/api/build/reports/problems/problems-report.html

  FAILURE: Build failed with an exception.

  * What went wrong:
  Execution failed for task ':kspKotlin'.
  > Inconsistent JVM-target compatibility detected for tasks 'compileJava' (25) and 'kspKotlin' (21).

    Consider using JVM Toolchain: https://kotl.in/gradle/jvm/toolchain
    Learn more about JVM-target validation: https://kotl.in/gradle/jvm/target-validation

  * Try:
  > Run with --stacktrace option to get the stack trace.
  > Run with --info or --debug option to get more log output.
  > Run with --scan to generate a Build Scan (Powered by Develocity).
  > Get more help at https://help.gradle.org.

  Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

  You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

  For more on this, please refer to https://docs.gradle.org/9.1.0/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

  BUILD FAILED in 2m 17s
  2 actionable tasks: 2 executed
  ```

  - gradle.propertiesを見ると、 `kotlinVersion=1.9.25` になってる
  - java25対応しているのはkotliin2.3.0からなのでエラーになってる
  - バージョン上げて他でエラー出てもいやなので、エラーメッセージに従いjavaのバージョンを21に変更する

- java21入れる
  ```bash
  $ brew install openjdk@21
  $ sudo ln -sfn $(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
  $ java --version
  openjdk 21.0.10 2026-01-20
  OpenJDK Runtime Environment Homebrew (build 21.0.10)
  OpenJDK 64-Bit Server VM Homebrew (build 21.0.10, mixed mode, sharing)
  ```
- jdk=21でmicronautプロジェクトを作り直す
  ```bash
  $ mn create-app reading-insight-api \
    --lang=kotlin \
    --build=gradle_kotlin \
    --jdk=21 \
    --features=serialization-jackson,validation
  ```
- ./gradle run で起動
