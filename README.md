# Planetlib :rocket:

A lightweight Java library for Bukkit plugin development, designed to simplify common tasks including message files, data storage, command handling, GUIs, and more.

![GitHub](https://img.shields.io/github/license/jason31416/planetlib)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/jason31416/planetlib)

## Features

### Core Features
- **Data Storage**Simple utilities for permanent storage (YAML format only for now).
- **Inventory GUI** – Highly configurable inventory GUI creations.
- **Multi-Language Support** – Load language files and manage in-game messages.
- **Command System** – Simplified command registration with auto-completion.
- **Folia Support** – Multi-threaded task scheduling via [Folialib](https://github.com/TechnicallyCoded/FoliaLib) (included).
- **NBT Manipulation** – Easily modify NBT tags with [ItemNBTAPI](https://github.com/tr7zw/Item-NBT-API) (included).

### Optional Features (Can be turned on/off in the initialization of the lib)
- **Vault Hooks** – Optional integration with Vault for economy/permissions.
- **Map Plugin Hooks** *(WIP)* – Support for map rendering plugins.

---

## Installation (Maven)

Add the following repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.jason31416</groupId>
        <artifactId>planetlib</artifactId>
        <version>[RELEASE VERSION]</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

You should also shade the lib into another location to avoid conflicts via the maven-shade-plugin:
```xml
<relocations>
    <relocation>
        <pattern>cn.jason31416.planetlib</pattern>
        <shadedPattern>[YOUR OWN PATH].planetlib</shadedPattern>
    </relocation>
</relocations>
```

---

## Basic usages
