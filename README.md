# BbanIbanConverter

This is a plugin for IntelliJ IDEA used to validate and convert BBAN <-> IBAN. By selecting text and right-clicking, if the selected text is a valid BBAN or IBAN, a popup menu will show options to convert to IBAN or BBAN.

## Supported Banks
Currently, this plugin supports the following banks from the Netherlands:
1. Rabobank
2. ING
3. ABN AMRO

## Usage
To validate and convert BBAN to IBAN, this plugin uses the `java-iban` library from:
[https://github.com/barend/java-iban](https://github.com/barend/java-iban)

## Installation
1. Clone the repository:
    ```sh
    git clone https://github.com/ducloidao/BBAN2IBAN.git
    ```
2. Open the project in IntelliJ IDEA.
3. Build the project using Gradle.

## Building the Plugin
To build the plugin, run the following Gradle tasks:
```sh
./gradlew build
./gradlew patchPluginXml
./gradlew signPlugin
./gradlew publishPlugin