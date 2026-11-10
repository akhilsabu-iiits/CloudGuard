# Hosting checklist for browser-only access

CloudGuard is a server-rendered single-origin web application: the browser loads the UI from Spring Boot, and the same server exposes `/api/*`. There is no separate frontend host to configure.

## Before exposing it to the internet

- Add authentication and authorization (the current starter does not include user accounts).
- Use HTTPS.
- Configure a persistent disk mounted at `/app/data` for the current H2 database.
- Restrict who can access the service while it is a research prototype.
- Set resource limits appropriate for CloudSim simulations; 500-task simulations and repeated suites consume server CPU/RAM.
- Consider moving to managed PostgreSQL before multiple users run experiments concurrently.

## Container deployment

The `Dockerfile` builds the app using Maven and Java 17, then runs it on a Java 17 JRE image. A container host can build the image directly from this repository. Configure the host to expose port 8080 (or inject its assigned `PORT`) and preserve `/app/data` between deploys.

For local maintainer smoke testing only, Docker Compose exposes the app at `http://localhost:8080`. End users of the hosted deployment do not need Docker or Java installed.
