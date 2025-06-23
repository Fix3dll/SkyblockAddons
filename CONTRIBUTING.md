Contributor's Guide
------

Please make sure to make your pull requests off the `main` branch if you're adding new features.
If there's an important bug fix, always make a PR, but off of the `stable` branch. Then that hotfix
can be rebased back into `main` afterward (with `checkout main`, & `rebase stable`).
```shell script
# Not recommended
git checkout -b development --track origin/development
```

### Requirements
- **JDK**
    - JDK 21
> [!NOTE]
> You can also look at the [Gradle Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html)
> to see the compatible Java version based on the currently used Gradle version instead of JDK 21.
- **IDE** (One of them)
    - [IntelliJ IDEA](https://www.jetbrains.com/idea/) **(Recommended)**
        - Our team uses IDEA. We may not be able to provide support for other IDEs.
    - [Eclipse](https://www.eclipse.org/)
- **(Plugin) Lombok** to help with things such as not having to manually create Getters and Setters.
    - IntelliJ IDEA (pre-installed)
    - [Eclipse](https://projectlombok.org/setup/eclipse)

### Getting started
> [!NOTE]
> You can use `gradle` instead of using gradle wrapper `./gradlew`.

1. Clone the repository
2. Wait for the gradle project to import.
3. Setup the development environment
    ```shell script
    ./gradlew configureClientLaunch
    ```
4. Integrate the development environment with your IDE
    - IntelliJ IDEA
        - Set the project SDK to Java 21 ([instructions](https://www.jetbrains.com/help/idea/sdk.html#change-project-sdk))
        - Set the gradle JVM to 21 ([instructions](https://www.jetbrains.com/help/idea/gradle-jvm-selection.html#jvm_settings))
    - Eclipse **(doesn't generate debug configuration)**
    - Change **Text File Encoding** from `Default` to `UTF-8`
        - Go to `Window` -> `Preferences` -> `General` -> `Workspace`
        - Change `Text File Encoding` from `Default` to `UTF-8`
5. **You are now ready to build the mod!**

### How to build
1. Build the mod
    ```shell script
    ./gradlew build
    ```

> [!NOTE]
> If your jar **build** is **failing** because the code is trying to access private methods or fields,
> this may be because someone added some new access transformers.
>
> You may want to re-run the gradle task
> ```shell script
> ./gradlew validateAccessWidener
> ```
> so the access transformers are applied to the source code!
>
> The build process produces these artifacts:
>- SkyblockAddons-2.0.0-for-MC-1.21.5.jar (mod code, resources, shaded libraries, remapped for obfuscated environment)

2. (Optional) Run the **Minecraft Forge** client
    - Using IntelliJ IDEA
        - Add the Java argument `-Dsba.data.online=true` if you want to use data files from the CDN.
        - Run the "Minecraft Client" debug configuration.
        - Click the link in the console to log in with [DevAuth](https://github.com/DJtheRedstoner/DevAuth).
    - See [DevAuth/README](https://github.com/DJtheRedstoner/DevAuth?tab=readme-ov-file#configuration) for more details
      about configuration.