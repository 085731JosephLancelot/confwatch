# confwatch

A daemon that monitors config file changes across services and triggers configurable webhooks or reload commands.

---

## Installation

```bash
git clone https://github.com/yourorg/confwatch.git
cd confwatch && ./mvnw clean package && java -jar target/confwatch.jar
```

---

## Usage

Define your watches in `confwatch.yml`:

```yaml
watches:
  - name: nginx-config
    path: /etc/nginx/nginx.conf
    on_change:
      webhook: https://hooks.example.com/notify
      command: "nginx -s reload"

  - name: app-properties
    path: /opt/myapp/application.properties
    on_change:
      command: "systemctl restart myapp"
```

Then start the daemon:

```bash
java -jar confwatch.jar --config confwatch.yml
```

confwatch will poll (or use filesystem events) for changes to each listed path and fire the configured webhook and/or run the reload command when a change is detected.

### CLI Options

| Flag | Default | Description |
|------|---------|-------------|
| `--config` | `confwatch.yml` | Path to the config file |
| `--interval` | `5000` | Poll interval in milliseconds |
| `--log-level` | `INFO` | Logging verbosity |

---

## Requirements

- Java 17+
- Maven 3.8+ (for building from source)

---

## License

This project is licensed under the [MIT License](LICENSE).