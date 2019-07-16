# remembrance-agent
Java package for Remembrance Agents! Based on Rhodes/Starner

[![Build Status](https://travis-ci.org/glass-notes/remembrance-agent.svg?branch=master)](https://travis-ci.org/glass-notes/remembrance-agent)

![Logo](./img/logo-01.png)

## Commands

### Building

```bash
./gradlew build
```

### Installing as Launch Daemon (macOS)

```bash
sudo bash ./bin/install
```

Now you can run `ra-desktop` from any where. Use the flag `--use-cache` to save on redundant calls to time-expensive data stores like Google Drive.

### Running (cross platform)

```bash
./gradlew build
java -jar ./build/libs/remembrance-agent-v1.0-all.jar
```

## Screenshots

### RA client with menu open

![](./docs/img/ra-client-menu-open.png)

### RA client with suggestion

![](./docs/img/ra-client-with-suggestion.png)

### TextEdit opened suggestion

![](./docs/img/textedit-opened-suggestion.png)

---

Pramod Kotipalli  
@p13i  
http://p13i.io
