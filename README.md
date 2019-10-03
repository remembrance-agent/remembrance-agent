# remembrance-agent
Java package for Remembrance Agents! Based on Rhodes/Starner

[![Build Status](https://travis-ci.org/remembrance-agent/remembrance-agent.svg?branch=master)](https://travis-ci.org/remembrance-agent/remembrance-agent) [![Build Status](https://github.com/remembrance-agent/remembrance-agent/workflows/Java%20CI/badge.svg)](https://github.com/remembrance-agent/remembrance-agent/actions?workflow=Java+CI)

![Logo](./docs/img/logo.png)

## Commands

### Building

```bash
VERSION="2.0.0" ./gradlew build
```

### Installing as Launch Daemon (macOS)

```bash
sudo VERSION="2.0.0" bash ./bin/install
```

Now you can run the following from anywhere:
```bash
VERSION="2.0.0" ra --home $HOME
```

### Running (cross platform)

Adapt the file `./bin/ra` for your platform.

## Screenshots

### RA client with menu open

![](./docs/img/ra-client-menu-open.png)

### RA client with suggestion

![](./docs/img/ra-client-with-suggestion.png)

### Chrome opened suggestion

![](./docs/img/chrome-opened-suggestion.png)

## Developing

### Versioning

Increment the version numbers in this README.

---

Pramod Kotipalli  
@p13i  
http://p13i.io
